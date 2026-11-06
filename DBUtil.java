package com.shop.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens database connections. Change the three constants below for your MySQL. */
public final class DBUtil {
    private static final String URL =
            "jdbc:mysql://localhost:3306/shopdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "your_mysql_password";   // <-- change this

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL driver not found: " + e.getMessage());
        }
    }

    private DBUtil() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
