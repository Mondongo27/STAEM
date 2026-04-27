package com.mycompany;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

public class BibliotecaView {

    public void start(int usuarioId) {
        Stage stage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label titulo = new Label("MI BIBLIOTECA DE JUEGOS");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Creamos la Tabla
        TableView<Videojuego> tabla = new TableView<>();

        // Columna Título
        TableColumn<Videojuego, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));

        // Columna Estado
        TableColumn<Videojuego, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Columna Valoración
        TableColumn<Videojuego, Integer> colNota = new TableColumn<>("Nota");
        colNota.setCellValueFactory(new PropertyValueFactory<>("valoracion"));

        tabla.getColumns().addAll(colTitulo, colEstado, colNota);

        // Cargar los datos de la base de datos
        tabla.setItems(cargarDatos(usuarioId));

        layout.getChildren().addAll(titulo, tabla);
        Scene scene = new Scene(layout, 400, 500);
        stage.setTitle("Staem - Biblioteca");
        stage.setScene(scene);
        stage.show();
    }

    private ObservableList<Videojuego> cargarDatos(int usuarioId) {
        ObservableList<Videojuego> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM videojuegos WHERE usuario_id = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
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
}