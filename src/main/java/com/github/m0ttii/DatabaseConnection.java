package com.github.m0ttii;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Utility class for defining and resolving the database credentials
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/mysql";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
