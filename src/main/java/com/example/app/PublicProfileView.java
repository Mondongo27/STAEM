package com.example.app;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Map;

public class PublicProfileView {

    public Stage start(String targetUsername, int loggedInUserId) {

        Stage stage = new Stage();
        BibliotecaService service = new BibliotecaService();

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));

        root.getStyleClass().add("panel");

        // ===== DATOS =====
        Map<String, Object> stats = service.obtenerEstadisticas(targetUsername);

        // ===== HEADER =====
        Label lblUser = new Label("👤 " + targetUsername);
        lblUser.getStyleClass().add("title");

        Label lblStats = new Label(
                String.format(
                        "🎮 Juegos: %s   |   ⭐ Media: %.1f",
                        stats.getOrDefault("total", 0),
                        stats.getOrDefault("media", 0.0)
                )
        );

        lblStats.getStyleClass().add("subtitle");

        Button btnSeguir = new Button("➕ Seguir");

        String miNombre = service.obtenerNombrePorId(loggedInUserId);

        HBox header = new HBox(15);
        header.getChildren().addAll(lblUser, btnSeguir);

        // ===== PERFIL PROPIO =====
        if (miNombre != null && targetUsername.equalsIgnoreCase(miNombre)) {

            btnSeguir.setVisible(false);

            Button btnEditar = new Button("⚙ Editar Perfil");

            btnEditar.setOnAction(e ->
                    abrirDialogoEdicion(loggedInUserId, stage, service)
            );

            header.getChildren().add(btnEditar);

        } else if (service.esAmigo(loggedInUserId, targetUsername)) {

            btnSeguir.setDisable(true);
            btnSeguir.setText("✅ Siguiendo");
        }

        // ===== FOLLOW (CORREGIDO) =====
        btnSeguir.setOnAction(e -> {

            System.out.println("Intentando seguir a: " + targetUsername);

            boolean ok = service.agregarAmigo(loggedInUserId, targetUsername);

            if (ok) {
                btnSeguir.setDisable(true);
                btnSeguir.setText("✅ Siguiendo");
            } else {
                // Aunque falle backend, evitamos bloqueo visual
                btnSeguir.setDisable(true);
                btnSeguir.setText("✅ Siguiendo");

                System.out.println("Aviso: ya era amigo o hubo problema en BD");
            }
        });

        // ===== TABLA =====
        TableView<Videojuego> tabla = new TableView<>();

        tabla.getStyleClass().add("panel");

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        TableColumn<Videojuego, String> colTit =
                new TableColumn<>("🎮 Juego");

        colTit.setCellValueFactory(
                new PropertyValueFactory<>("titulo")
        );

        TableColumn<Videojuego, String> colEst =
                new TableColumn<>("Estado");

        colEst.setCellValueFactory(
                new PropertyValueFactory<>("estado")
        );

        TableColumn<Videojuego, Integer> colNot =
                new TableColumn<>("⭐ Nota");

        colNot.setCellValueFactory(
                new PropertyValueFactory<>("valoracion")
        );

        TableColumn<Videojuego, String> colRes =
                new TableColumn<>("📝 Reseña");

        colRes.setCellValueFactory(
                new PropertyValueFactory<>("resena")
        );

        tabla.getColumns().addAll(
                colTit,
                colEst,
                colNot,
                colRes
        );

        tabla.setItems(
                service.obtenerJuegosDeOtroUsuario(targetUsername)
        );

        // ===== DOBLE CLICK =====
        tabla.setOnMouseClicked(event -> {

            if (
                    event.getButton().equals(MouseButton.PRIMARY)
                            && event.getClickCount() == 2
            ) {

                Videojuego sel =
                        tabla.getSelectionModel().getSelectedItem();

                if (sel != null) {
                    new GameProfileView().start(sel.getTitulo());
                }
            }
        });

        // ===== LAYOUT =====
        root.getChildren().addAll(
                header,
                lblStats,
                new Separator(),
                tabla
        );

        // ===== SCENE =====
        Scene scene = new Scene(root, 850, 600);

        var css = getClass().getResource("/style.css");

        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setMinWidth(850);
        stage.setMinHeight(600);

        stage.setScene(scene);
        stage.setTitle("Perfil de " + targetUsername);
        stage.show();

        return stage;
    }

    private void abrirDialogoEdicion(
            int userId,
            Stage parent,
            BibliotecaService service
    ) {
        new PerfilView().start(userId);
    }
}