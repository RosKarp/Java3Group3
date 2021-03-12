package ru.GeekBrains.Rostislav.Java3Group3.lesson2.ServerSide;

public interface AuthService {
    void start();
    String getNickByLoginPass(String login, String pass);
    void stop();
}
///