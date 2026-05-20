package com.example.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.*;

/**
 * Servicio de acceso a datos para la biblioteca de videojuegos.
 * Encapsula toda la lógica de negocio y consultas SQL.
 */
public class BibliotecaService {

    // ─────────────────────────────────────────
    // VIDEOJUEGOS
    // ─────────────────────────────────────────

    /** Añade un juego a la biblioteca del usuario. Devuelve false si ya existe. */
    public boolean añadirVideojuego(int usuarioId, String titulo, String estado) {
        if (titulo == null || titulo.isBlank()) return false;
        String checkSql  = "SELECT COUNT(*) FROM videojuegos WHERE usuario_id = ? AND titulo = ?";
        String insertSql = "INSERT INTO videojuegos (usuario_id, titulo, estado, valoracion, resena) VALUES (?, ?, ?, 0, '')";

        try (Connection conn = ConexionDB.conectar()) {
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setInt(1, usuarioId);
                check.setString(2, titulo);
                ResultSet rs = check.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return false;
            }
            try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                insert.setInt(1, usuarioId);
                insert.setString(2, titulo);
                insert.setString(3, estado);
                return insert.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error añadirVideojuego: " + e.getMessage());
            return false;
        }
    }

    /** Actualiza estado, nota y reseña de un juego por su id. */
    public boolean actualizarVideojuego(int id, String nuevoEstado, int nuevaNota, String nuevaResena) {
        if (nuevoEstado == null) return false;
        int nota = Math.max(0, Math.min(10, nuevaNota));
        String sql = "UPDATE videojuegos SET estado = ?, valoracion = ?, resena = ? WHERE id = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, nota);
            stmt.setString(3, nuevaResena != null ? nuevaResena : "");
            stmt.setInt(4, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error actualizarVideojuego: " + e.getMessage());
            return false;
        }
    }

    /** Elimina un juego de la biblioteca por su id. */
    public boolean eliminarVideojuego(int idJuego) {
        String sql = "DELETE FROM videojuegos WHERE id = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idJuego);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error eliminarVideojuego: " + e.getMessage());
            return false;
        }
    }

    /** Obtiene todos los juegos de un usuario dado su username. */
    public ObservableList<Videojuego> obtenerJuegosDeOtroUsuario(String username) {
        ObservableList<Videojuego> lista = FXCollections.observableArrayList();
        String sql = """
                SELECT v.* FROM videojuegos v
                JOIN usuarios u ON v.usuario_id = u.id
                WHERE u.username = ?
                ORDER BY v.titulo ASC
                """;
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapVideojuego(rs));
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error obtenerJuegosDeOtroUsuario: " + e.getMessage());
        }
        return lista;
    }

    // ─────────────────────────────────────────
    // CATÁLOGO
    // ─────────────────────────────────────────

    /** Busca juegos en el catálogo global (máx. 10 resultados). */
    public ObservableList<String> buscarEnCatalogoGlobal(String busqueda) {
        ObservableList<String> resultados = FXCollections.observableArrayList();
        if (busqueda == null || busqueda.isBlank()) return resultados;
        String sql = "SELECT titulo FROM catalogo_juegos WHERE titulo LIKE ? ORDER BY titulo LIMIT 10";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + busqueda.trim() + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) resultados.add(rs.getString("titulo"));
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error buscarEnCatalogoGlobal: " + e.getMessage());
        }
        return resultados;
    }

    /** Comprueba si un título existe en el catálogo global. */
    public boolean existeEnCatalogo(String titulo) {
        if (titulo == null || titulo.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM catalogo_juegos WHERE titulo = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, titulo.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error existeEnCatalogo: " + e.getMessage());
        }
        return false;
    }

    // ─────────────────────────────────────────
    // ESTADÍSTICAS
    // ─────────────────────────────────────────

    /** Devuelve total de juegos y media de valoración de un usuario. */
    public Map<String, Object> obtenerEstadisticas(String username) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", 0);
        stats.put("media", 0.0);
        String sql = """
                SELECT COUNT(*) AS total, IFNULL(AVG(valoracion), 0) AS media
                FROM videojuegos v
                JOIN usuarios u ON v.usuario_id = u.id
                WHERE u.username = ?
                """;
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
                stats.put("media", rs.getDouble("media"));
            }
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error obtenerEstadisticas: " + e.getMessage());
        }
        return stats;
    }

    /**
     * Calcula la nota global de un juego y recoge las reseñas de la comunidad.
     *
     * FIX: notaUsuarios estaba en escala 0-10 pero la fórmula la usaba como si fuera 0-5,
     * produciendo notas globales superiores a 5. Ahora se normaliza a 0-5 antes de combinar.
     * FIX: las reseñas ahora incluyen el nombre del usuario que las escribió.
     */
    public Map<String, Object> obtenerDetallesGlobalesJuego(String titulo) {
        Map<String, Object> detalles = new HashMap<>();

        // FIX: JOIN con usuarios para obtener el username del autor de cada reseña
        String sql = """
                SELECT v.valoracion, v.resena, u.username
                FROM videojuegos v
                JOIN usuarios u ON v.usuario_id = u.id
                WHERE v.titulo = ?
                """;

        double sumaNotas = 0;
        int contador = 0;
        List<String> reseñas = new ArrayList<>();

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, titulo != null ? titulo.trim() : "");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int nota = rs.getInt("valoracion");
                if (nota > 0) { sumaNotas += nota; contador++; }
                String res      = rs.getString("resena");
                String username = rs.getString("username");
                // FIX: incluir el nombre del autor para que la reseña sea identificable
                if (res != null && !res.isBlank()) {
                    reseñas.add(username + ": " + res.trim());
                }
            }
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error obtenerDetallesGlobalesJuego: " + e.getMessage());
        }

        // Nota media de usuarios en escala 0-10
        double notaUsuarios = contador > 0 ? (double) sumaNotas / contador : 0.0;

        double notaGlobal;
        if (contador == 0) {
            notaGlobal = 0.0;
        } else {
            // FIX: normalizar notaUsuarios a escala 0-5 antes de combinar con los
            // pesos de prensa (4.5/5) y popularidad (5/5), así el resultado es 0-5.
            double notaUsuarios05 = notaUsuarios / 2.0;
            notaGlobal = (notaUsuarios05 * 0.4) + (4.5 * 0.4) + (5.0 * 0.2);
        }

        detalles.put("notaGlobal",   Math.round(notaGlobal   * 10.0) / 10.0);
        detalles.put("notaUsuarios", Math.round(notaUsuarios * 10.0) / 10.0);
        detalles.put("numVotos",     contador);
        detalles.put("resenas",      reseñas);

        return detalles;
    }

    // ─────────────────────────────────────────
    // AMIGOS
    // ─────────────────────────────────────────

    /** Comprueba si 'nombreAmigo' ya está en la lista de amigos del usuario. */
    public boolean esAmigo(int usuarioId, String nombreAmigo) {
        String sql = "SELECT COUNT(*) FROM amigos WHERE usuario_id = ? AND amigo_nombre = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, nombreAmigo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error esAmigo: " + e.getMessage());
        }
        return false;
    }

    /**
     * Agrega un amigo al usuario. Devuelve false si:
     * - el nombre de amigo es el propio usuario,
     * - el usuario a añadir no existe, o
     * - ya eran amigos.
     */
    public boolean agregarAmigo(int usuarioId, String nombreAmigo) {
        if (nombreAmigo == null || nombreAmigo.isBlank()) return false;
        String miNombre = obtenerNombrePorId(usuarioId);
        if (miNombre != null && miNombre.equalsIgnoreCase(nombreAmigo.trim())) return false;
        if (!existeUsuario(nombreAmigo.trim())) return false;

        String sql = "INSERT OR IGNORE INTO amigos (usuario_id, amigo_nombre) VALUES (?, ?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, nombreAmigo.trim());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error agregarAmigo: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────
    // USUARIOS
    // ─────────────────────────────────────────

    /** Devuelve el username de un usuario dado su id, o null si no existe. */
    public String obtenerNombrePorId(int id) {
        String sql = "SELECT username FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error obtenerNombrePorId: " + e.getMessage());
        }
        return null;
    }

    /** Comprueba si existe un usuario con el username dado. */
    public boolean existeUsuario(String username) {
        if (username == null || username.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error existeUsuario: " + e.getMessage());
        }
        return false;
    }

    /**
     * Actualiza nombre, email y/o contraseña de un usuario.
     * Los campos null o vacíos conservan el valor actual de la BD.
     */
    public boolean actualizarDatosUsuario(int id, String nuevoNombre, String nuevoEmail, String nuevaPass) {
        try (Connection conn = ConexionDB.conectar()) {
            Map<String, String> actuales = obtenerDatosCompletosUsuario(id);
            if (actuales.isEmpty()) return false;

            String nombre = (nuevoNombre != null && !nuevoNombre.isBlank()) ? nuevoNombre.trim() : actuales.get("username");
            String email  = (nuevoEmail  != null && !nuevoEmail.isBlank())  ? nuevoEmail.trim()  : actuales.get("email");
            String pass   = (nuevaPass   != null && !nuevaPass.isBlank())   ? nuevaPass.trim()   : actuales.get("password");

            String sql = "UPDATE usuarios SET username = ?, email = ?, password = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nombre);
                stmt.setString(2, email);
                stmt.setString(3, pass);
                stmt.setInt(4, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error actualizarDatosUsuario: " + e.getMessage());
            return false;
        }
    }

    /** Devuelve username, email y password de un usuario dado su id. */
    public Map<String, String> obtenerDatosCompletosUsuario(int id) {
        Map<String, String> datos = new HashMap<>();
        String sql = "SELECT username, email, password FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                datos.put("username", rs.getString("username"));
                datos.put("email",    rs.getString("email"));
                datos.put("password", rs.getString("password"));
            }
        } catch (SQLException e) {
            System.err.println("[BibliotecaService] Error obtenerDatosCompletosUsuario: " + e.getMessage());
        }
        return datos;
    }

    // ─────────────────────────────────────────
    // HELPERS INTERNOS
    // ─────────────────────────────────────────

    private Videojuego mapVideojuego(ResultSet rs) throws SQLException {
        return new Videojuego(
                rs.getInt("id"),
                rs.getInt("usuario_id"),
                rs.getString("titulo"),
                rs.getString("estado"),
                rs.getInt("valoracion"),
                rs.getString("resena")
        );
    }
}