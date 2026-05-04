package com.example.app;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class GameProfileView {

    public void start(String titulo) {

        Stage stage = new Stage();
        BibliotecaService service = new BibliotecaService();

        VBox root = new VBox(18);
        root.setPadding(new Insets(25));

        root.getStyleClass().add("panel");

        // ===== DATOS =====
        Map<String, Object> detalles =
                service.obtenerDetallesGlobalesJuego(titulo);

        double notaGlobal =
                (double) detalles.get("notaGlobal");

        List<String> resenas =
                (List<String>) detalles.get("resenas");

        // ===== TITULO =====
        Label lblTitulo = new Label("🎮 " + titulo);
        lblTitulo.getStyleClass().add("title");

        Label lblNota = new Label(
                String.format(
                        "⭐ Valoración Global: %.2f / 5.0",
                        notaGlobal
                )
        );

        lblNota.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold;"
        );

        Label lblInfo = new Label(
                "40% Usuarios  •  40% Expertos  •  20% Premium"
        );

        lblInfo.getStyleClass().add("subtitle");

        Label lblResenas = new Label("📝 Reseñas");
        lblResenas.getStyleClass().add("title");

        // ===== LISTA =====
        ListView<String> lvResenas = new ListView<>();

        lvResenas.setPrefHeight(260);

        if (resenas != null && !resenas.isEmpty()) {

            lvResenas.getItems().addAll(resenas);

        } else {

            lvResenas.getItems().add(
                    "Todavía no hay reseñas para este juego."
            );
        }

        // ===== LAYOUT =====
        root.getChildren().addAll(
                lblTitulo,
                new Separator(),
                lblNota,
                lblInfo,
                new Separator(),
                lblResenas,
                lvResenas
        );

        // ===== SCENE =====
        Scene scene = new Scene(root, 620, 500);

        scene.getStylesheets().add(
                getClass().getResource("/style.css")
                        .toExternalForm()
        );

        stage.setMinWidth(620);
        stage.setMinHeight(500);

        stage.setScene(scene);
        stage.setTitle("Game Profile");
        stage.show();
    }
}