package com.example.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/**
 * Ficha de detalle de un juego: muestra nota global, votos y reseñas de la comunidad.
 */
public class DetalleJuegoView {

    public void mostrar(String titulo) {

        BibliotecaService service = new BibliotecaService();
        Map<String, Object> detalles = service.obtenerDetallesGlobalesJuego(titulo);

        double notaGlobal   = (double)  detalles.getOrDefault("notaGlobal",   0.0);
        double notaUsuarios = (double)  detalles.getOrDefault("notaUsuarios", 0.0);
        int    numVotos     = (int)     detalles.getOrDefault("numVotos",     0);
        @SuppressWarnings("unchecked")
        List<String> reseñas = (List<String>) detalles.getOrDefault("resenas", List.of());

        Stage stage = new Stage();

        // ─── ROOT ───
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #121212;");

        // ─── HEADER ───
        VBox headerBox = new VBox(6);
        headerBox.setPadding(new Insets(30, 30, 24, 30));
        headerBox.setStyle(
                "-fx-background-color: #1a1a1a; " +
                        "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0;"
        );

        Label icon = new Label("🎮");
        icon.setStyle("-fx-font-size: 36px;");

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: white;");
        lblTitulo.setWrapText(true);

        Label lblSub = new Label("Información del catálogo de STAEM");
        lblSub.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.45);");

        headerBox.getChildren().addAll(icon, lblTitulo, lblSub);

        // ─── STATS ROW ───
        HBox statsRow = new HBox(16);
        statsRow.setPadding(new Insets(20, 30, 20, 30));
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.setStyle(
                "-fx-background-color: #1a1a1a; " +
                        "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0;"
        );

        statsRow.getChildren().addAll(
                createStatCard("⭐ Nota STAEM",   String.format("%.1f / 5", notaGlobal),  "#0078d4"),
                createStatCard("👥 Nota usuarios", String.format("%.1f / 10", notaUsuarios), "#107c10"),
                createStatCard("🗳 Votos",          String.valueOf(numVotos),               "#ffb900")
        );

        // ─── CONTENIDO SCROLL ───
        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(24, 30, 30, 30));

        Label descLabel = new Label(
                "Bienvenido a la ficha de \"" + titulo + "\".\n\n" +
                        "Aquí encontrarás las valoraciones y reseñas que la comunidad de STAEM " +
                        "ha dejado sobre este título. ¡Añádelo a tu biblioteca para dejar la tuya!"
        );
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.7); -fx-line-spacing: 4;");

        Label reseñasTitulo = new Label("💬 Reseñas de la comunidad");
        reseñasTitulo.setStyle("-fx-font-size: 17px; -fx-font-weight: 700; -fx-text-fill: white;");

        VBox reseñasBox = new VBox(10);

        if (reseñas.isEmpty()) {
            Label noReseñas = new Label("Todavía no hay reseñas para este juego. ¡Sé el primero!");
            noReseñas.setStyle(
                    "-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 13px; " +
                            "-fx-font-style: italic; -fx-padding: 12 0;"
            );
            reseñasBox.getChildren().add(noReseñas);
        } else {
            for (String reseña : reseñas) {
                VBox card = new VBox(6);
                card.setPadding(new Insets(14, 16, 14, 16));
                card.setStyle(
                        "-fx-background-color: #1e1e1e; -fx-background-radius: 10; " +
                                "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1; -fx-border-radius: 10;"
                );
                Label rLabel = new Label(reseña);
                rLabel.setWrapText(true);
                rLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 13px; -fx-line-spacing: 3;");
                card.getChildren().add(rLabel);
                reseñasBox.getChildren().add(card);
            }
        }

        contentBox.getChildren().addAll(descLabel, new Separator(), reseñasTitulo, reseñasBox);

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // ─── BOTÓN CERRAR ───
        HBox footer = new HBox();
        footer.setPadding(new Insets(12, 20, 12, 20));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle(
                "-fx-background-color: #1a1a1a; " +
                        "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1 0 0 0;"
        );
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: white; " +
                        "-fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 9 24; -fx-cursor: hand;"
        );
        btnCerrar.setOnAction(e -> stage.close());
        footer.getChildren().add(btnCerrar);

        root.getChildren().addAll(headerBox, statsRow, scroll, footer);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // ─── SCENE ───
        Scene scene = new Scene(root, 600, 520);
        try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); }
        catch (Exception ignored) {}

        stage.setMinWidth(500);
        stage.setMinHeight(420);
        stage.setScene(scene);
        stage.setTitle("STAEM — " + titulo);
        try { stage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png"))); }
        catch (Exception ignored) {}
        stage.show();
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(14, 20, 14, 20));
        card.setStyle(String.format(
                "-fx-background-color: rgba(255,255,255,0.04); " +
                        "-fx-background-radius: 10; -fx-border-color: %s; " +
                        "-fx-border-width: 0 0 0 3; -fx-border-radius: 0;", color
        ));
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.5); -fx-font-weight: 600;");
        Label val = new Label(value);
        val.setStyle(String.format("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: %s;", color));
        card.getChildren().addAll(lbl, val);
        return card;
    }
}