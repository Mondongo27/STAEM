package com.example.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL = "jdbc:sqlite:staem.db";

    public static Connection conectar() {
        try {
            // En SQLite NO se pasan USER ni PASS, solo la URL
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Error al conectar a SQLite: " + e.getMessage());
            return null;
        }
    }

}