package com.example.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.datatransfer.Clipboard;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DetalleJuegoView {

    private static final Logger LOGGER = Logger.getLogger(DetalleJuegoView.class.getName());
    private BibliotecaService service = new BibliotecaService();

    /**
     * Muestra la ficha técnica de un juego del catálogo global
     * @param titulo Nombre del juego a mostrar
     * @param loggedInUserId ID del usuario logueado (para personalizar vista)
     */
    public void start(String titulo, int loggedInUserId) {
        Stage stage = new Stage();

        // ===== ROOT =====
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("panel");
        root.setStyle("-fx-background-color: #1a1a1a;");

        // ===== HEADER =====
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Button btnBack = new Button("← Volver");
        btnBack.getStyleClass().add("btn-secondary");
        btnBack.setOnAction(e -> stage.close());

        Label lblTitulo = new Label("🎮 " + titulo.toUpperCase());
        lblTitulo.getStyleClass().add("title");
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 22px;");

        header.getChildren().addAll(btnBack, new Region(), lblTitulo);
        HBox.setHgrow(new Region(), Priority.ALWAYS);

        // ===== CONTENIDO PRINCIPAL =====
        GridPane content = new GridPane();
        content.setHgap(30);
        content.setVgap(20);
        content.setPadding(new Insets(20, 10, 10, 10));

        // Columna izquierda: Info del juego
        VBox infoBox = new VBox(15);
        infoBox.setPrefWidth(300);

        // Imagen placeholder (puedes reemplazar con imagen real)
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(200);
        imageContainer.setStyle("-fx-background-color: linear-gradient(135deg, #2a2a2a, #1a1a1a); -fx-background-radius: 12;");
        Label gameIcon = new Label("🎮");
        gameIcon.setStyle("-fx-font-size: 64px;");
        imageContainer.getChildren().add(gameIcon);
        StackPane.setAlignment(gameIcon, Pos.CENTER);

        Label lblDescripcion = new Label(
                "Información oficial del catálogo de STAEM.\n\n" +
                        "Este juego forma parte de nuestra biblioteca global. " +
                        "Los usuarios de la comunidad pueden añadirlo a su biblioteca personal, " +
                        "valorarlo y compartir reseñas."
        );
        lblDescripcion.setWrapText(true);
        lblDescripcion.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 14px; -fx-line-spacing: 1.4;");

        infoBox.getChildren().addAll(imageContainer, lblDescripcion);

        // Columna derecha: Estadísticas y acciones
        VBox statsBox = new VBox(20);
        statsBox.setPrefWidth(250);

        // Nota global calculada
        Map<String, Object> detalles = service.obtenerDetallesGlobalesJuego(titulo);
        double notaGlobal = (Double) detalles.getOrDefault("notaGlobal", 0.0);

        // Widget de puntuación
        VBox ratingWidget = new VBox(10);
        ratingWidget.setAlignment(Pos.CENTER);
        ratingWidget.setStyle("-fx-background-color: #252525; -fx-background-radius: 12; -fx-padding: 20;");

        Label lblNota = new Label(String.format("%.1f", notaGlobal));
        lblNota.setStyle("-fx-font-size: 42px; -fx-font-weight: 800; -fx-text-fill: #ffd700;");

        Label lblEstrellas = new Label(generarEstrellas(notaGlobal));
        lblEstrellas.setStyle("-fx-font-size: 24px;");

        Label lblVotos = new Label("Puntuación global STAEM");
        lblVotos.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 12px;");

        ratingWidget.getChildren().addAll(lblNota, lblEstrellas, lblVotos);

        // Botones de acción
        VBox actionsBox = new VBox(12);

        Button btnAñadir = new Button("➕ Añadir a mi biblioteca");
        btnAñadir.setMaxWidth(Double.MAX_VALUE);
        btnAñadir.getStyleClass().add("btn-primary");
        btnAñadir.setOnAction(e -> {
            if (service.existeEnCatalogo(titulo)) {
                if (service.añadirVideojuego(loggedInUserId, titulo, "pendiente")) {
                    mostrarAlert(stage, "✅ ¡Añadido!", "El juego se ha añadido a tu biblioteca.");
                } else {
                    mostrarAlert(stage, "⚠️ Ya lo tienes", "Este juego ya está en tu biblioteca.");
                }
            } else {
                mostrarAlert(stage, "❌ Error", "El juego no está disponible en el catálogo.");
            }
        });

        // En lugar de usar Clipboard, muestra el texto para que el usuario lo copie:
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Compartir juego");
        alert.setHeaderText("Copia este enlace:");
        String enlace = "🎮 " + titulo + "\n🔗 staem.app/game/" + titulo.toLowerCase().replace(" ", "-");
        alert.setContentText(enlace);

// Opcional: agregar un TextArea seleccionable para facilitar la copia
        TextArea ta = new TextArea(enlace);
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setMaxWidth(Double.MAX_VALUE);
        alert.getDialogPane().setContent(ta);

        alert.showAndWait();

        statsBox.getChildren().addAll(ratingWidget, new Separator(), actionsBox);

        content.add(infoBox, 0, 0);
        content.add(statsBox, 1, 0);
        GridPane.setColumnIndex(statsBox, 1);

        // ===== RESEÑAS DE LA COMUNIDAD =====
        @SuppressWarnings("unchecked")
        List<String> reseñas = (List<String>) detalles.getOrDefault("resenas", List.of());

        if (!reseñas.isEmpty()) {
            Label lblReseñasTitulo = new Label("💬 Reseñas de la comunidad");
            lblReseñasTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 600; -fx-padding: 10 0 5 0;");

            VBox reseñasBox = new VBox(10);
            reseñasBox.setStyle("-fx-background-color: #252525; -fx-background-radius: 12; -fx-padding: 15;");

            for (String resena : reseñas) {
                Label lblResena = new Label("• " + resena);
                lblResena.setWrapText(true);
                lblResena.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 13px;");
                reseñasBox.getChildren().add(lblResena);
            }

            content.add(lblReseñasTitulo, 0, 1, 2, 1);
            content.add(reseñasBox, 0, 2, 2, 1);
        }

        // ===== LAYOUT FINAL =====
        root.getChildren().addAll(header, new Separator(), content);

        // ===== SCENE =====
        Scene scene = new Scene(root, 750, 550);
        cargarCSS(scene);

        stage.setTitle("🎮 " + titulo + " | STAEM");
        cargarIcono(stage);
        stage.setScene(scene);
        stage.setMinWidth(650);
        stage.setMinHeight(500);
        stage.show();
    }

    // ===== MÉTODOS AUXILIARES =====

    private String generarEstrellas(double nota) {
        int llenas = (int) Math.round(nota / 1.0);
        return "★".repeat(llenas) + "☆".repeat(5 - llenas);
    }

    private void mostrarAlert(Stage parent, String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(parent);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        estilizarAlert(alert);
        alert.showAndWait();
    }

    private void estilizarAlert(Alert alert) {
        alert.getDialogPane().setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;");
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("custom-alert");
    }

    private void cargarCSS(Scene scene) {
        try {
            String css = getClass().getResource("/style.css").toExternalForm();
            if (css != null) scene.getStylesheets().add(css);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar style.css", e);
        }
    }

    private void cargarIcono(Stage stage) {
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar el icono", e);
        }
    }
}