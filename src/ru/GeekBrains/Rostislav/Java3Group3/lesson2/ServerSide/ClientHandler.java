// В этом классе решение п.2 ДЗ

package ru.GeekBrains.Rostislav.Java3Group3.lesson2.ServerSide;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientHandler {
        private final MyServer myServer;
        private final Socket socket;
        private final DataInputStream in;
        private final DataOutputStream out;
        private long clientConnectionTime = 0;
        private long clientAuthenticationTime = 0;

        private String name;
        public String getName() {
            return name;
        }
        private static final long authorizationBlockingTime = 180000; // 3 минуты блокировки при неправильном логине или пароле
        private String strFromClient = "";

        public ClientHandler(MyServer myServer, Socket socket) {
            try {
                clientConnectionTime = System.currentTimeMillis();
                this.myServer = myServer;
                this.socket = socket;
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                name = "";

                Exchanger<String> ex = new Exchanger<>();        // обменник для передачи name после авторизации
                Exchanger<String> ex2 = new Exchanger<>();      // обменник для передачи сообщения после авторизации

                new Thread(new PutThread(ex, ex2)).start();          // поток для авторизации
                new Thread(new GetThread(ex, ex2)).start();       // поток для отключения по таймауту

            } catch (IOException e) {
                throw new RuntimeException("Проблемы при создании обработчика клиента");
            }
        }

    class PutThread implements Runnable {
        Exchanger<String> exchanger;
        Exchanger<String> exchanger2;
        PutThread(Exchanger<String> ex, Exchanger<String> ex2) {
            this.exchanger = ex;
            this.exchanger2 = ex2;
        }
        @Override
        public void run() {
            try {
                authentication();
                clientAuthenticationTime = System.currentTimeMillis();
                exchanger.exchange(name);
                readMessages(exchanger2);
            } catch (IOException | InterruptedException ignored) { }
            finally {
                closeConnection();
            }
        }
    }

    class GetThread implements Runnable {
            Exchanger<String> exchanger;
            Exchanger<String> exchanger2;
            String message = "";
            GetThread(Exchanger<String> ex, Exchanger<String> ex2) {
                this.exchanger = ex;
                this.exchanger2 = ex2;
            }
        @Override
        public void run() {
            while (message.equals("")) {
                try {
                    if(System.currentTimeMillis() - clientConnectionTime > 120000) {    // 2 минуты на авторизацию
                        sendMsg("Время ожидания авторизации истекло, сервер разорвал соединение.");
                        closeConnection();
                        break;
                    }
                    message = exchanger.exchange("", 500, TimeUnit.MILLISECONDS);      // в цикле ожидаем получения name только полсекунды
                } catch (TimeoutException | InterruptedException ignored) {}
            }

            message = "";

            while (message.equals("")) {
                try {
                    if(System.currentTimeMillis() - clientAuthenticationTime > 180000) {    // 3 минуты на отправку сообщения после авторизации
                        closeConnection();
                        break;
                    }
                    message = exchanger2.exchange("", 500, TimeUnit.MILLISECONDS);      // в цикле ожидаем получения сообщения только полсекунды
                } catch (TimeoutException | InterruptedException ignored) {}
            }
        }
    }

        public void authentication() throws IOException, InterruptedException {
            int countOfFault = 1;
            while (true) {
                String str = in.readUTF();
                if (str.startsWith("/auth")) {
                    String[] parts = str.split("\\s");
                    if(parts.length == 3) {
                        String nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                        if (nick != null) {
                            if (!myServer.isNickBusy(nick)) {
                                sendMsg("/authok " + nick);
                                name = nick;
                                myServer.broadcastMsg(name + " зашел в чат");
                                myServer.subscribe(this);
                                return;
                            } else {
                                sendMsg("Учетная запись уже используется");
                                countOfFault++;
                            }
                        } else {
                            sendMsg("Неверные логин/пароль");
                            countOfFault++;
                        }
                        if(countOfFault == 6) {                                                                      // 3 попытки до временной блокировки
                            sendMsg("Ввод логина/пароля заблокирован на " + authorizationBlockingTime/1000 + " cek");
                            sendMsg("Ожидайте разрешения на ввод логина/пароля");
                            Thread.sleep(authorizationBlockingTime);
                            sendMsg("Введите верный пароль");
                        }
                    }
                    countOfFault++;
                    if(countOfFault == 6) {
                        sendMsg("Ввод логина/пароля заблокирован на " + authorizationBlockingTime/1000 + " cek");
                        sendMsg("Ожидайте разрешения на ввод логина/пароля");
                        Thread.sleep(authorizationBlockingTime);
                        sendMsg("Введите верный пароль");
                    }
                }
            }
        }

        public void readMessages(Exchanger<String> stringExchanger) throws IOException, InterruptedException {

            int j = 0;
            while (true) {
                strFromClient = in.readUTF();
                System.out.println("от " + name + ": " + strFromClient);
                if (strFromClient.equals("/end")) {
                    return;
                }
                if (strFromClient.startsWith("/changenick")) {                          // реализация возможности смены nick через обновление БД (команда: /changenick [новый ник] )
                    String newNick = strFromClient.substring(12, strFromClient.length());
                    try (PreparedStatement ps = BaseAuthService.con.prepareStatement("UPDATE members SET nick = ? WHERE nick = ?"))
                    {
                        ps.setString(1, newNick);
                        ps.setString(2, name);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        System.out.println("Ошибка при смене ника.");
                    }
                }
                if(strFromClient.startsWith("/w ")) {
                    String[] toOne = strFromClient.split("\\s");
                    String nick = toOne[1];
                    StringBuilder builder = new StringBuilder();
                    for (int i = 2; i < toOne.length; i++) {
                        builder.append(toOne[i]).append(" ");
                    }
                    String message = builder.toString();
                    myServer.toOneMsg(this, nick, message);                        // персональное сообщение
                } else {
                    myServer.broadcastMsg(name + ": " + strFromClient);
                }
                if(j == 0) {
                    stringExchanger.exchange(strFromClient);
                    j++;
                }
            }
        }

        public void sendMsg(String msg) {
            try {
                out.writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void closeConnection() {
            myServer.unsubscribe(this);
            myServer.broadcastMsg(name + " вышел из чата");
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }