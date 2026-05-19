package com.example.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class GameProfileView {

    public void start(String gameTitle) {
        BibliotecaService service = new BibliotecaService();
        Stage stage = new Stage();
        stage.setTitle("STAEM - " + gameTitle);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #121212;");

        // HEADER
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0;");

        Button btnBack = new Button("← Volver");
        btnBack.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white !important; -fx-font-size: 13px; -fx-font-weight: 500; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnBack.setOnMouseEntered(e -> btnBack.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white !important; -fx-font-size: 13px; -fx-font-weight: 500; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;"));
        btnBack.setOnMouseExited(e -> btnBack.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white !important; -fx-font-size: 13px; -fx-font-weight: 500; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;"));

        Label titleLabel = new Label(gameTitle);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: white !important;");
        header.getChildren().addAll(btnBack, titleLabel);

        // CONTENT
        VBox content = new VBox(30);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);

        VBox infoCard = new VBox(20);
        infoCard.setPadding(new Insets(40));
        infoCard.setMaxWidth(900);
        infoCard.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 1; -fx-border-radius: 20;");

        Label gameIcon = new Label("🎮");
        gameIcon.setStyle("-fx-font-size: 100px; -fx-text-fill: white !important;");
        gameIcon.setAlignment(Pos.CENTER); gameIcon.setMaxWidth(Double.MAX_VALUE);

        Label gameTitleLabel = new Label(gameTitle);
        gameTitleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 700; -fx-text-fill: white !important;");
        gameTitleLabel.setAlignment(Pos.CENTER); gameTitleLabel.setMaxWidth(Double.MAX_VALUE);

        // Obtener datos
        Map<String, Object> detalles = service.obtenerDetallesGlobalesJuego(gameTitle);
        double notaGlobal = (double) detalles.getOrDefault("notaGlobal", 0.0);
        @SuppressWarnings("unchecked")
        List<String> resenas = (List<String>) detalles.getOrDefault("resenas", new java.util.ArrayList<>());

        // Rating
        HBox ratingBox = new HBox(10);
        ratingBox.setAlignment(Pos.CENTER); ratingBox.setPadding(new Insets(10, 0, 20, 0));
        String stars = getStars(notaGlobal);
        Label starsLabel = new Label(stars);
        starsLabel.setStyle("-fx-text-fill: #ffd60a !important; -fx-font-size: 28px;");
        Label ratingText = new Label(String.format("%.1f / 5.0", notaGlobal));
        ratingText.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: white !important;");
        ratingBox.getChildren().addAll(starsLabel, ratingText);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.1);");

        Label reviewsTitle = new Label("Reseñas de la Comunidad");
        reviewsTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 600; -fx-text-fill: white !important;");

        VBox reviewsContainer = new VBox(12);
        reviewsContainer.setStyle("-fx-padding: 10 0;"); reviewsContainer.setMaxWidth(Double.MAX_VALUE);

        if (resenas.isEmpty()) {
            Label noReviews = new Label("No hay reseñas disponibles todavía");
            noReviews.setStyle("-fx-text-fill: rgba(255,255,255,0.5) !important; -fx-font-size: 15px; -fx-padding: 20;");
            noReviews.setAlignment(Pos.CENTER); noReviews.setMaxWidth(Double.MAX_VALUE);
            reviewsContainer.getChildren().add(noReviews);
        } else {
            for (String resena : resenas) {
                VBox reviewBox = new VBox(5);
                reviewBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 12; -fx-padding: 15; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1; -fx-border-radius: 12;");
                Label reviewLabel = new Label(resena);
                reviewLabel.setWrapText(true);
                reviewLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9) !important; -fx-font-size: 14px; -fx-line-spacing: 4;");
                reviewLabel.setMaxWidth(Double.MAX_VALUE);
                reviewBox.getChildren().add(reviewLabel);
                reviewsContainer.getChildren().add(reviewBox);
            }
        }

        infoCard.getChildren().addAll(gameIcon, gameTitleLabel, ratingBox, separator, reviewsTitle, reviewsContainer);
        content.getChildren().add(infoCard);
        root.setTop(header); root.setCenter(content);

        Scene scene = new Scene(root, 1100, 750);
        try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception e) {}
        stage.setScene(scene); stage.show();
        btnBack.setOnAction(e -> stage.close());
    }

    private String getStars(double rating) {
        int full = (int) Math.floor(rating);
        int half = (rating % 1 >= 0.5) ? 1 : 0;
        int empty = 5 - full - half;
        return "★".repeat(full) + (half > 0 ? "½" : "") + "☆".repeat(empty);
    }
}