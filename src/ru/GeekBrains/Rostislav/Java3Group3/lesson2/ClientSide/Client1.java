// Это ДЗ к 3 уроку.
package ru.GeekBrains.Rostislav.Java3Group3.lesson2.ClientSide;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client1 extends JFrame {

    JTextArea chatArea = new JTextArea(10, 10);
    JTextField message = new JTextField();

    Socket socket = null;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean isAuthorized;

    private final int showLastMessage = 100;
    private final String pathHistory1 = "C:\\Users\\Ростислав\\IdeaProjects\\GB\\src\\ru\\GeekBrains\\Rostislav\\Java3Group3\\lesson2\\ClientSide\\history_login1.txt";
    private File history1 = null;
    private BufferedWriter writer;
    private BufferedReader reader;

    public Client1() {
        setTitle("Chat");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(300, 100, 500, 400);

        JPanel talk = new JPanel();
        JPanel myMessage = new JPanel();
        myMessage.setPreferredSize(new Dimension(Client1.WIDTH, 50));

        Font font = new Font("monospaced", Font.PLAIN, 30);
        chatArea.setFont(font);
        chatArea.setBackground(new Color(220, 250, 220));
        chatArea.setEditable(false);  // запрет ввода напрямую в основное окно чата

        JScrollPane jsp = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        talk.setLayout(new BorderLayout());
        talk.add(jsp, BorderLayout.CENTER);
        add(talk, BorderLayout.CENTER);

        myMessage.setLayout(new BorderLayout());
        JButton send = new JButton("Send");
        send.setBackground(new Color(200, 200, 250));
        myMessage.add(send, BorderLayout.EAST);
        message.setFont(font);
        myMessage.add(message, BorderLayout.CENTER);
        add(myMessage, BorderLayout.SOUTH);

        send.addActionListener(e -> send());
        message.addActionListener(e -> send());

        try {
            history1.exists();
        } catch (NullPointerException e) {
            history1 = new File(pathHistory1); // создаем файл записи истории чата при условии, что он еще не создан
        }
        String s = "";
        try {
            writer = new BufferedWriter(new FileWriter(history1, true));
            reader = new BufferedReader(new FileReader(history1));
            int count = countLines(history1);           // подсчет числа строк в файле
            int skip = count - showLastMessage - 1;

            for (int i = 0; i < count; i++) {
                if (skip > 0) {
                    for (int j = 0; j < skip; j++) {
                        reader.readLine();              // пропускаем первые skip строк
                    }
                }
                if((s = reader.readLine()) != null) {
                    chatArea.append(s + "\n");          // вывод истории чата в окно клиента
                    skip = -1;
                }
            }
           reader.close();
        } catch (IOException e) {
            System.out.println("File for chat logging not found.");
        }
        setVisible(true);
    }

    public int countLines(File input) throws IOException {
        try (InputStream is = new FileInputStream(input)) {
            int count = 1;
            for (int aChar = 0; aChar != -1; aChar = is.read())
                count += aChar == '\n' ? 1 : 0;
            return count;
        }
    }
    public void start() {
        try {
            final String[] myNick = new String[1];
            isAuthorized = false;
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String strFromServer = in.readUTF();
                        if (strFromServer.startsWith("/authok")) {
                            isAuthorized = true;
                            myNick[0] = strFromServer.split("\\s")[1];
                            chatArea.append(strFromServer + "\n");
                            break;
                        }
                        chatArea.append(strFromServer + "\n");
                    }
                    while (true) {
                        String strFromServer = in.readUTF();
                        try {
                            writer.write(strFromServer + "\n");  // запись окна чата в файл
                            writer.flush();
                        } catch (IOException e) {
                            System.out.println("Error of write log file.");
                        }
                        if (strFromServer.equalsIgnoreCase("/end")) {
                            writer.close();             // закрытие потока записи при выходе
                            break;
                        }
                        chatArea.append(strFromServer);
                        chatArea.append("\n");
                    }
                } catch (Exception ignored) {
                } finally {
                    try {
                        isAuthorized = false;
                        socket.close();
                        myNick[0] = "";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            System.out.println("Подключение к серверу не удалось.");
            e.printStackTrace();
        }
    }
    public void onAuthClick() {
        if(socket == null || socket.isClosed()) {
            start();
        }
        try {
            out.writeUTF("/auth " + message.getText());
            message.setText("");
            } catch (Exception ignored) { }
    }
    public void send() {
        if(message.getText() != null && !message.getText().trim().isEmpty() && isAuthorized) {
            try {
                out.writeUTF(message.getText()); // отправка сообщений на сервер
                message.setText("");
            } catch (IOException ignored) { }
        }
        if(!isAuthorized) {
            onAuthClick();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client1::new);
    }
}