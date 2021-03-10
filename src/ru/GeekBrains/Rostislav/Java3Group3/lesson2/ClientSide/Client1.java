package ru.GeekBrains.Rostislav.Java3Group3.lesson2.ClientSide;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client1 extends JFrame {

    JTextArea chatArea = new JTextArea(10, 10);
    JTextField message = new JTextField();

    Socket socket = null;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean isAuthorized;

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

        setVisible(true);
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
                        if (strFromServer.equalsIgnoreCase("/end")) {
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