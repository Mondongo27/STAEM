package com.mycompany;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    // método para registrar un usuario nuevo
    public boolean registrarUsuario(String nombre, String clave) {
        String consulta = "insert into usuarios (username, password) values (?, ?)";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(consulta)) {

            stmt.setString(1, nombre);
            stmt.setString(2, clave);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("error al registrar: " + e.getMessage());
            return false;
        }
    }

    // método para validar el inicio de sesión
    public int login(String nombre, String clave) {
        String consulta = "select id from usuarios where username = ? and password = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(consulta)) {

            stmt.setString(1, nombre);
            stmt.setString(2, clave);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // devuelve el id del usuario encontrado
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("error en el login: " + e.getMessage());
        }
        // si no coincide o hay error, devuelve -1
        return -1;
    }
}