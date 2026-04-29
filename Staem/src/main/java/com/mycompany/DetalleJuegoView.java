package com.mycompany;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DetalleJuegoView {
    public void mostrar(String titulo) {
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        Label lblTitulo = new Label(titulo.toUpperCase());
        lblTitulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2F52A2;");

        // Aquí podrías añadir más labels como: "Año Lanzamiento", "Publisher", etc.
        // sacándolos de la base de datos con un SELECT a catalogo_juegos.

        Label info = new Label("Información del catálogo oficial de MyGameList.");
        info.setWrapText(true);

        root.getChildren().addAll(lblTitulo, info);
        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("Ficha Técnica: " + titulo);
        stage.show();
    }
}