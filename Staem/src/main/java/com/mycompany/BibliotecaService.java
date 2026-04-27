package com.mycompany;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BibliotecaService {

    public boolean añadirVideojuego(int usuarioId, String titulo, String estado) {
        String consulta = "insert into videojuegos (usuario_id, titulo, estado) values (?, ?, ?)";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(consulta)) {

            stmt.setInt(1, usuarioId);
            stmt.setString(2, titulo);
            stmt.setString(3, estado);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("error al añadir juego: " + e.getMessage());
            return false;
        }
    }

    public void mostrarBiblioteca(int usuarioId) {
        String consulta = "select titulo, estado, valoracion from videojuegos where usuario_id = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(consulta)) {

            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- tu biblioteca ---");
            while (rs.next()) {
                System.out.println("- " + rs.getString("titulo") + " [" + rs.getString("estado") + "] - Valoración: " + rs.getInt("valoracion") + "/5");
            }
        } catch (SQLException e) {
            System.out.println("error al consultar: " + e.getMessage());
        }
    }

    public boolean valorarJuego(int usuarioId, String titulo, int nota, String resena) {
        String consulta = "update videojuegos set valoracion = ?, resena = ? where usuario_id = ? and titulo = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(consulta)) {

            stmt.setInt(1, nota);
            stmt.setString(2, resena);
            stmt.setInt(3, usuarioId);
            stmt.setString(4, titulo);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("error al valorar: " + e.getMessage());
            return false;
        }
    }

    public void verValoracionGlobal(String tituloJuego) {
        // calculamos la media (avg) y contamos cuánta gente ha votado (count)
        String consulta = "select avg(valoracion) as media, count(valoracion) as total from videojuegos where titulo = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(consulta)) {

            stmt.setString(1, tituloJuego);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double media = rs.getDouble("media");
                int total = rs.getInt("total");

                if (total > 0) {
                    System.out.println("\n--- valoraciones de la comunidad ---");
                    System.out.println("juego: " + tituloJuego);
                    System.out.println("nota media: " + String.format("%.2f", media) + " / 5");
                    System.out.println("basado en " + total + " valoraciones.");
                } else {
                    System.out.println("nadie ha valorado este juego todavía.");
                }
            }
        } catch (SQLException e) {
            System.out.println("error al consultar globales: " + e.getMessage());
        }
    }
}