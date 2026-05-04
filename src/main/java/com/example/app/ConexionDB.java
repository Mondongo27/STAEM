package com.example.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL = "jdbc:mysql://localhost:3306/foro_videojuegos";
    private static final String USER = "root";
    private static final String PASS = "usuario";

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("error al conectar: " + e.getMessage());
            return null;
        }
    }

}