package com.example.app;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DetalleJuegoView {

    public void mostrar(String titulo) {

        Stage stage = new Stage();

        VBox root = new VBox(18);
        root.setPadding(new Insets(25));

        root.getStyleClass().add("panel");

        // ===== TITULO =====
        Label lblTitulo = new Label("🎮 " + titulo.toUpperCase());
        lblTitulo.getStyleClass().add("title");

        // ===== INFO =====
        Label info = new Label(
                "Información oficial del catálogo de STAEM.\n\n" +
                        "Aquí podrás ver datos técnicos, puntuaciones, " +
                        "reseñas y actividad de la comunidad."
        );

        info.setWrapText(true);

        // ===== LAYOUT =====
        root.getChildren().addAll(
                lblTitulo,
                new Separator(),
                info
        );

        // ===== SCENE =====
        Scene scene = new Scene(root, 520, 340);

        scene.getStylesheets().add(
                getClass().getResource("/style.css")
                        .toExternalForm()
        );

        stage.setMinWidth(520);
        stage.setMinHeight(340);

        stage.setScene(scene);
        stage.setTitle("Ficha Técnica");
        stage.show();
    }
}