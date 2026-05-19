package com.example.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BibliotecaService {

    private static final Logger LOGGER = Logger.getLogger(BibliotecaService.class.getName());

    public boolean añadirVideojuego(int usuarioId, String titulo, String estado) {
        String checkSql = "SELECT count(*) FROM videojuegos WHERE usuario_id = ? AND titulo = ?";
        String insertSql = "INSERT INTO videojuegos (usuario_id, titulo, estado, valoracion, resena) VALUES (?, ?, ?, 0, '')";
        try (Connection conn = ConexionDB.conectar()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, usuarioId);
                checkStmt.setString(2, titulo);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return false;
            }
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, usuarioId);
                insertStmt.setString(2, titulo);
                insertStmt.setString(3, estado);
                return insertStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al añadir videojuego: " + titulo, e);
            return false;
        }
    }

    public boolean actualizarVideojuego(int id, String nuevoEstado, int nuevaNota, String nuevaResena) {
        String sql = "UPDATE videojuegos SET estado = ?, valoracion = ?, resena = ? WHERE id = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, nuevaNota);
            stmt.setString(3, nuevaResena);
            stmt.setInt(4, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar videojuego ID: " + id, e);
            return false;
        }
    }

    public boolean eliminarVideojuego(int idJuego) {
        String sql = "DELETE FROM videojuegos WHERE id = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idJuego);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar videojuego ID: " + idJuego, e);
            return false;
        }
    }

    public Map<String, Object> obtenerEstadisticas(String username) {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT COUNT(*) as total, IFNULL(AVG(valoracion), 0) as media FROM videojuegos v JOIN usuarios u ON v.usuario_id = u.id WHERE u.username = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
                stats.put("media", rs.getDouble("media"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error al obtener estadísticas para: " + username, e);
        }
        return stats;
    }

    public ObservableList<Videojuego> obtenerJuegosDeOtroUsuario(String username) {
        ObservableList<Videojuego> lista = FXCollections.observableArrayList();
        String sql = "SELECT v.* FROM videojuegos v JOIN usuarios u ON v.usuario_id = u.id WHERE u.username = ?";
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
            LOGGER.log(Level.WARNING, "Error al obtener juegos de: " + username, e);
        }
        return lista;
    }

    public ObservableList<String> buscarEnCatalogoGlobal(String busqueda) {
        ObservableList<String> resultados = FXCollections.observableArrayList();
        if (busqueda == null || busqueda.trim().length() < 3) return resultados;

        String sql = "SELECT titulo FROM catalogo_juegos WHERE titulo LIKE ? LIMIT 10";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + busqueda.trim() + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) resultados.add(rs.getString("titulo"));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error en búsqueda de catálogo: " + busqueda, e);
        }
        return resultados;
    }

    public boolean existeEnCatalogo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM catalogo_juegos WHERE titulo = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, titulo.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error verificando catálogo: " + titulo, e);
        }
        return false;
    }

    public boolean esAmigo(int usuarioId, String nombreAmigo) {
        if (nombreAmigo == null || nombreAmigo.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM amigos WHERE usuario_id = ? AND amigo_nombre = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, nombreAmigo.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error verificando amistad", e);
        }
        return false;
    }

    public boolean agregarAmigo(int usuarioId, String nombreAmigo) {
        if (nombreAmigo == null || nombreAmigo.trim().isEmpty()) return false;
        String sql = "INSERT OR IGNORE INTO amigos (usuario_id, amigo_nombre) VALUES (?, ?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, nombreAmigo.trim());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al agregar amigo: " + nombreAmigo, e);
            return false;
        }
    }

    public String obtenerNombrePorId(int id) {
        String sql = "SELECT username FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error al obtener nombre por ID: " + id, e);
        }
        return "";
    }

    public boolean actualizarDatosUsuario(int id, String nuevoNombre, String nuevoEmail, String nuevaPass) {
        try (Connection conn = ConexionDB.conectar()) {
            String selectSql = "SELECT username, email, password FROM usuarios WHERE id = ?";
            String usernameActual = "", emailActual = "", passwordActual = "";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, id);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    usernameActual = rs.getString("username");
                    emailActual = rs.getString("email");
                    passwordActual = rs.getString("password");
                }
            }
            if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) nuevoNombre = usernameActual;
            if (nuevoEmail == null || nuevoEmail.trim().isEmpty()) nuevoEmail = emailActual;
            if (nuevaPass == null || nuevaPass.trim().isEmpty()) nuevaPass = passwordActual;

            String updateSql = "UPDATE usuarios SET username = ?, email = ?, password = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, nuevoNombre);
                stmt.setString(2, nuevoEmail);
                stmt.setString(3, nuevaPass);
                stmt.setInt(4, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar datos de usuario ID: " + id, e);
            return false;
        }
    }

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
            LOGGER.log(Level.WARNING, "Error al obtener datos de usuario ID: " + id, e);
        }
        return datos;
    }

    public Map<String, Object> obtenerDetallesGlobalesJuego(String titulo) {
        Map<String, Object> detalles = new HashMap<>();
        String sql = "SELECT valoracion, resena FROM videojuegos WHERE titulo = ?";
        double sumaNotas = 0;
        int contador = 0;
        List<String> reseñas = new ArrayList<>();

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, titulo);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int nota = rs.getInt("valoracion");
                if (nota > 0) { sumaNotas += nota; contador++; }
                String res = rs.getString("resena");
                if (res != null && !res.isEmpty()) reseñas.add(res);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error al obtener detalles de: " + titulo, e);
        }

        double notaUsuarios = (contador > 0) ? (sumaNotas / contador) : 0;
        double notaGlobal = (notaUsuarios * 0.4) + (4.5 * 0.4) + (5.0 * 0.2);
        detalles.put("notaGlobal", notaGlobal);
        detalles.put("resenas", reseñas);
        return detalles;
    }

    public boolean existeUsuario(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error verificando existencia de usuario: " + username, e);
        }
        return false;
    }

    // ✅ HELPER: Búsqueda en memoria (evita consulta DB innecesaria desde la UI)
    public Videojuego buscarJuegoEnLista(ObservableList<Videojuego> lista, String titulo) {
        if (lista == null || titulo == null || titulo.trim().isEmpty()) return null;
        return lista.stream()
                .filter(j -> j.getTitulo() != null && j.getTitulo().equalsIgnoreCase(titulo.trim()))
                .findFirst()
                .orElse(null);
    }
}