package com.github.m0ttii;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection con = DriverManager
                .getConnection("jdbc:mysql://localhost:3307/mysql", "root", "root")) {
            try (Statement stmt = con.createStatement()) {
                String tableSql = "CREATE TABLE IF NOT EXISTS employees"
                        + "(emp_id int PRIMARY KEY AUTO_INCREMENT, name varchar(30),"
                        + "position varchar(30), salary double)";
                stmt.execute(tableSql);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}