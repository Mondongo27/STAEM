package com.mycompany;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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

    public boolean actualizarVideojuego(int id, String nuevoEstado, int nuevaNota, String nuevaResena) {
        String sql = "update videojuegos set estado = ?, valoracion = ?, resena = ? where id = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, nuevaNota);
            stmt.setString(3, nuevaResena);
            stmt.setInt(4, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminarVideojuego(int idJuego) {
        String sql = "delete from videojuegos where id = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idJuego);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("error al eliminar: " + e.getMessage());
            return false;
        }
    }

    public double obtenerMediaPublico(String tituloJuego) {
        String sql = "select avg(valoracion) as media from videojuegos where titulo = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tituloJuego);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("media");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // --- MÉTODOS ACTUALIZADOS PARA EL CATÁLOGO MASIVO ---

    public ObservableList<String> obtenerNombresCategorias() {
        ObservableList<String> lista = FXCollections.observableArrayList();
        // Obtenemos los géneros únicos directamente del catálogo importado
        String sql = "select distinct categoria from catalogo_juegos order by categoria asc";
        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                if (rs.getString("categoria") != null) {
                    lista.add(rs.getString("categoria"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public ObservableList<String> obtenerNombresConsolas() {
        ObservableList<String> lista = FXCollections.observableArrayList();
        // Obtenemos las consolas únicas directamente del catálogo importado
        String sql = "select distinct consola from catalogo_juegos order by consola asc";
        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                if (rs.getString("consola") != null) {
                    lista.add(rs.getString("consola"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public ObservableList<String> obtenerJuegosFiltrados(String cat, String con) {
        ObservableList<String> lista = FXCollections.observableArrayList();
        // Filtramos directamente por los campos de texto 'categoria' y 'consola'
        String sql = "select titulo from catalogo_juegos where categoria = ? and consola = ? order by titulo asc";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cat);
            stmt.setString(2, con);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(rs.getString("titulo"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public ObservableList<Videojuego> obtenerJuegosDeOtroUsuario(String username) {
        ObservableList<Videojuego> lista = FXCollections.observableArrayList();
        // Buscamos los juegos uniendo con la tabla usuarios por nombre
        String sql = "select v.* from videojuegos v " +
                "join usuarios u on v.usuario_id = u.id " +
                "where u.username = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new Videojuego(rs.getInt("id"), rs.getInt("usuario_id"), rs.getString("titulo"),
                        rs.getString("estado"), rs.getInt("valoracion"), rs.getString("resena")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<String> buscarUsuarios(String filtro) {
        List<String> usuarios = new ArrayList<>();
        String sql = "select username from usuarios where username like ? limit 10";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + filtro + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) usuarios.add(rs.getString("username"));
        } catch (SQLException e) { e.printStackTrace(); }
        return usuarios;
    }
}