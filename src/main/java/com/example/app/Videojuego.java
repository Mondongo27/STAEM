package com.example.app;

/**
 * Modelo de datos para un videojuego en la biblioteca del usuario.
 * Representa un juego añadido por un usuario a su colección personal en Staem.
 *
 * ✅ Compatible con:
 * - JavaFX TableView (PropertyValueFactory)
 * - JavaFX bindings y observables
 * - Serialización básica
 */
public class Videojuego {

    // 🔹 Atributos privados (encapsulamiento)
    private int id;
    private int usuarioId;
    private String titulo;
    private String estado;        // Valores: "pendiente", "jugando", "jugado"
    private int valoracion;       // Rango: 0-10 (0 = sin valorar)
    private String resena;        // Texto opcional del usuario

    // 🔹 Constructor vacío (OBLIGATORIO para JavaFX PropertyValueFactory)
    // JavaFX lo usa para crear instancias reflejadas al llenar TableView
    public Videojuego() {
        this.resena = "";  // Valor por defecto seguro
        this.estado = "pendiente";
        this.valoracion = 0;
    }

    // 🔹 Constructor completo (para crear objetos con todos los datos)
    public Videojuego(int id, int usuarioId, String titulo, String estado, int valoracion, String resena) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.estado = estado != null ? estado : "pendiente";
        this.valoracion = valoracion;
        this.resena = resena != null ? resena : "";
    }

    // ========================================
    // 🔹 GETTERS (lectura de propiedades)
    // ========================================

    public int getId() {
        return id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getEstado() {
        return estado;
    }

    public int getValoracion() {
        return valoracion;
    }

    public String getResena() {
        return resena;
    }

    // ========================================
    // 🔹 SETTERS (escritura de propiedades)
    // ========================================

    public void setId(int id) {
        this.id = id;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Establece el estado del juego.
     * @param estado Debe ser: "pendiente", "jugando" o "jugado"
     */
    public void setEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            this.estado = "pendiente";
        } else {
            this.estado = estado.trim().toLowerCase();
        }
    }

    /**
     * Establece la valoración del juego (0-10).
     * FIX: el límite superior era 5 cuando el sistema usa escala 0-10.
     * @param valoracion Número entre 0 y 10. Valores fuera de rango se ajustan automáticamente.
     */
    public void setValoracion(int valoracion) {
        if (valoracion < 0) {
            this.valoracion = 0;
        } else if (valoracion > 10) {
            this.valoracion = 10;
        } else {
            this.valoracion = valoracion;
        }
    }

    public void setResena(String resena) {
        this.resena = resena != null ? resena : "";
    }

    // ========================================
    // 🔹 MÉTODOS UTILITARIOS (opcionales pero recomendados)
    // ========================================

    /**
     * Devuelve una representación legible del objeto (útil para debugging y logs).
     */
    @Override
    public String toString() {
        return String.format("Videojuego{id=%d, titulo='%s', estado='%s', valoración=%d/10}",
                id, titulo, estado, valoracion);
    }

    /**
     * Compara dos Videojuego por su ID único.
     * Necesario para operaciones en colecciones (contains, remove, etc.).
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Videojuego that = (Videojuego) obj;
        return id == that.id;
    }

    /**
     * Genera hash basado en el ID.
     * Debe coincidir con equals() para funcionar en HashMap/HashSet.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // ========================================
    // 🔹 MÉTODOS ADICIONALES ÚTILES PARA STAEM
    // ========================================

    /**
     * Verifica si el juego tiene una reseña escrita por el usuario.
     * @return true si la reseña no está vacía
     */
    public boolean tieneResena() {
        return resena != null && !resena.trim().isEmpty();
    }

    /**
     * Verifica si el juego ha sido valorado por el usuario.
     * @return true si la valoración es mayor a 0
     */
    public boolean estaValorado() {
        return valoracion > 0;
    }

    /**
     * Obtiene el estado formateado para mostrar en UI (primera letra mayúscula).
     * @return Ej: "Pendiente", "Jugando", "Jugado"
     */
    public String getEstadoFormateado() {
        if (estado == null || estado.isEmpty()) return "Pendiente";
        return estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();
    }

    /**
     * Obtiene una representación visual de la valoración con estrellas (escala 0-10).
     * Se muestran 5 estrellas donde cada estrella equivale a 2 puntos.
     * @return Ej: "★★★★☆" para valoración 8
     */
    public String getValoracionConEstrellas() {
        if (valoracion <= 0) return "☆".repeat(5);
        int estrellaLlenas = (int) Math.round(valoracion / 2.0);
        estrellaLlenas = Math.min(estrellaLlenas, 5);
        return "★".repeat(estrellaLlenas) + "☆".repeat(5 - estrellaLlenas);
    }
}