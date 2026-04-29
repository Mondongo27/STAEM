package com.mycompany;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;
import java.util.Map;

public class GameProfileView {

    public void start(String titulo) {
        Stage stage = new Stage();
        BibliotecaService service = new BibliotecaService();
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Map<String, Object> detalles = service.obtenerDetallesGlobalesJuego(titulo);
        double notaGlobal = (double) detalles.get("notaGlobal");
        List<String> resenas = (List<String>) detalles.get("resenas");

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2F52A2;");

        Label lblNota = new Label(String.format("Valoración Global: %.2f / 5.0 ⭐", notaGlobal));
        lblNota.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Desglose de la nota (opcional, para transparencia)
        Label lblInfo = new Label("(40% Usuarios | 40% Expertos | 20% Premium)");
        lblInfo.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        ListView<String> lvResenas = new ListView<>();
        lvResenas.getItems().addAll(resenas);
        if (resenas.isEmpty()) lvResenas.getItems().add("No hay reseñas aún para este juego.");

        root.getChildren().addAll(
                lblTitulo,
                new Separator(),
                lblNota,
                lblInfo,
                new Label("Reseñas de la comunidad:"),
                lvResenas
        );

        stage.setScene(new Scene(root, 500, 400));
        stage.setTitle("Detalles: " + titulo);
        stage.show();
    }
}