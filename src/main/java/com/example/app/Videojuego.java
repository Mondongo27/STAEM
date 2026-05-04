package com.example.app;

public class Videojuego {
    private int id;
    private int usuarioId;
    private String titulo;
    private String estado;
    private int valoracion;
    private String resena;

    public Videojuego(int id, int usuarioId, String titulo, String estado, int valoracion, String resena) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.estado = estado;
        this.valoracion = valoracion;
        this.resena = resena;
    }

    public String getTitulo() { return titulo; }
    public String getEstado() { return estado; }
    public int getValoracion() { return valoracion; }
    public String getResena() { return resena; }

    public int getId() {
        return id;
    }
}