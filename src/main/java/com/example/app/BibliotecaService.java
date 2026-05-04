package com.example.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.*;

public class BibliotecaService {

    public boolean añadirVideojuego(int usuarioId, String titulo, String estado) {
        String checkSql = "select count(*) from videojuegos where usuario_id = ? and titulo = ?";
        String insertSql = "insert into videojuegos (usuario_id, titulo, estado) values (?, ?, ?)";

        try (Connection conn = ConexionDB.conectar()) {

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

                checkStmt.setInt(1, usuarioId);
                checkStmt.setString(2, titulo);

                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

                insertStmt.setInt(1, usuarioId);
                insertStmt.setString(2, titulo);
                insertStmt.setString(3, estado);

                return insertStmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
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

            e.printStackTrace();
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
            return false;
        }
    }

    public Map<String, Object> obtenerEstadisticas(String username) {

        Map<String, Object> stats = new HashMap<>();

        String sql = "select count(*) as total, ifnull(avg(valoracion), 0) as media from videojuegos v " +
                "join usuarios u on v.usuario_id = u.id where u.username = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                stats.put("total", rs.getInt("total"));
                stats.put("media", rs.getDouble("media"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    public ObservableList<Videojuego> obtenerJuegosDeOtroUsuario(String username) {

        ObservableList<Videojuego> lista = FXCollections.observableArrayList();

        String sql = "select v.* from videojuegos v join usuarios u on v.usuario_id = u.id where u.username = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                lista.add(new Videojuego(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("titulo"),
                        rs.getString("estado"),
                        rs.getInt("valoracion"),
                        rs.getString("resena")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public ObservableList<String> buscarEnCatalogoGlobal(String busqueda) {

        ObservableList<String> resultados = FXCollections.observableArrayList();

        String sql = "select titulo from catalogo_juegos where titulo like ? limit 10";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + busqueda + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                resultados.add(rs.getString("titulo"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultados;
    }

    public boolean existeEnCatalogo(String titulo) {

        String sql = "select count(*) from catalogo_juegos where titulo = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, titulo);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean esAmigo(int usuarioId, String nombreAmigo) {

        String sql = "select count(*) from amigos where usuario_id = ? and amigo_nombre = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.setString(2, nombreAmigo);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean agregarAmigo(int usuarioId, String nombreAmigo) {

        String sql = "insert ignore into amigos (usuario_id, amigo_nombre) values (?, ?)";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.setString(2, nombreAmigo);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    public String obtenerNombrePorId(int id) {

        String sql = "select username from usuarios where id = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "";
    }

    // ===== ACTUALIZAR PERFIL =====
    public boolean actualizarDatosUsuario(int id, String nuevoNombre, String nuevoEmail, String nuevaPass) {

        try (Connection conn = ConexionDB.conectar()) {

            // Obtener datos actuales
            String selectSql = "SELECT username, email, password FROM usuarios WHERE id = ?";

            String usernameActual = "";
            String emailActual = "";
            String passwordActual = "";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

                selectStmt.setInt(1, id);

                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {

                    usernameActual = rs.getString("username");
                    emailActual = rs.getString("email");
                    passwordActual = rs.getString("password");
                }
            }

            // Mantener valores actuales si vienen vacíos
            if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
                nuevoNombre = usernameActual;
            }

            if (nuevoEmail == null || nuevoEmail.trim().isEmpty()) {
                nuevoEmail = emailActual;
            }

            if (nuevaPass == null || nuevaPass.trim().isEmpty()) {
                nuevaPass = passwordActual;
            }

            // UPDATE
            String updateSql = "UPDATE usuarios SET username = ?, email = ?, password = ? WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {

                stmt.setString(1, nuevoNombre);
                stmt.setString(2, nuevoEmail);
                stmt.setString(3, nuevaPass);
                stmt.setInt(4, id);

                return stmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    // DATOS DEL USUARIO
    public Map<String, String> obtenerDatosCompletosUsuario(int id) {

        Map<String, String> datos = new HashMap<>();

        String sql = "SELECT username, email, password FROM usuarios WHERE id = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                datos.put("username", rs.getString("username"));
                datos.put("email", rs.getString("email"));
                datos.put("password", rs.getString("password"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return datos;
    }

    public Map<String, Object> obtenerDetallesGlobalesJuego(String titulo) {

        Map<String, Object> detalles = new HashMap<>();

        String sql = "select valoracion, resena from videojuegos where titulo = ?";

        double sumaNotas = 0;
        int contador = 0;

        List<String> reseñas = new ArrayList<>();

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, titulo);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                int nota = rs.getInt("valoracion");

                if (nota > 0) {
                    sumaNotas += nota;
                    contador++;
                }

                String res = rs.getString("resena");

                if (res != null && !res.isEmpty()) {
                    reseñas.add(res);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        double notaUsuarios = (contador > 0) ? (sumaNotas / contador) : 0;

        double notaExpertos = 4.5;
        double notaPremium = 5.0;

        double notaGlobal =
                (notaUsuarios * 0.4) +
                        (notaExpertos * 0.4) +
                        (notaPremium * 0.2);

        detalles.put("notaGlobal", notaGlobal);
        detalles.put("resenas", reseñas);

        return detalles;
    }
}