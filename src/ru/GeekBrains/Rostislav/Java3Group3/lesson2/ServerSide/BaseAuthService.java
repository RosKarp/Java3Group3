// В этом классе решение п.1 ДЗ 2 урока

package ru.GeekBrains.Rostislav.Java3Group3.lesson2.ServerSide;

import java.sql.*;

public class BaseAuthService implements AuthService{

    public static Connection con;
    private PreparedStatement preparedStatement;

    @Override
    public void start() {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/java3lesson2", "root", "root");    // подключение к MySQL
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к БД.");
        }
        System.out.println("Сервис аутентификации запущен");
    }

    @Override
    public void stop() {
        try {
            con.close();                // отключение от БД
        } catch (SQLException e) {
            System.out.println("Ошибка закрытия подключения к БД.");
        }
        System.out.println("Сервис аутентификации остановлен");
    }

    @Override
    public String getNickByLoginPass (String login1, String pass) {             // реализация через запрос в БД
        try {
            preparedStatement = con.prepareStatement("SELECT * FROM members where login = ?");
            preparedStatement.setString(1, login1);
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            if ((rs.getString("password")).equals(pass)) {
                return rs.getString("nick");
            } else return null;
        } catch (SQLException e) {
            System.out.println("Ошибка запроса в БД.");
            return null;
        }
    }
}