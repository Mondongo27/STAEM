package com.example.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Perfil público de un usuario: muestra sus estadísticas, biblioteca y permite
 * seguirle / editar el propio perfil.
 */
public class PublicProfileView {

    public Stage start(String targetUsername, int loggedInUserId) {

        Stage stage = new Stage();
        BibliotecaService service = new BibliotecaService();

        Map<String, Object> stats = service.obtenerEstadisticas(targetUsername);
        int    totalJuegos = (int)    stats.getOrDefault("total", 0);
        double mediaNotas  = (double) stats.getOrDefault("media", 0.0);

        String miNombre  = service.obtenerNombrePorId(loggedInUserId);
        boolean esPropioP = miNombre != null && miNombre.equalsIgnoreCase(targetUsername);
        boolean yaAmigo   = !esPropioP && service.esAmigo(loggedInUserId, targetUsername);

        // ─── ROOT ───
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #121212;");

        // ─── HEADER / BANNER ───
        VBox headerBox = new VBox(12);
        headerBox.setPadding(new Insets(32, 30, 28, 30));
        headerBox.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1e2a3a, #121212); " +
                        "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0;"
        );

        // Avatar
        StackPane avatar = new StackPane();
        avatar.setPrefSize(72, 72);
        avatar.setMaxSize(72, 72);
        avatar.setStyle(
                "-fx-background-color: #0078d4; -fx-background-radius: 36;"
        );
        Label avatarLabel = new Label(targetUsername.substring(0, 1).toUpperCase());
        avatarLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: 800; -fx-text-fill: white;");
        avatar.getChildren().add(avatarLabel);

        Label lblUser = new Label(targetUsername);
        lblUser.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: white;");

        Label lblStats = new Label(String.format(
                "🎮 %d juegos   •   ⭐ %.1f media", totalJuegos, mediaNotas
        ));
        lblStats.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.55);");

        // Botones de acción
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        if (esPropioP) {
            Button btnEditar = createPrimaryButton("⚙ Editar Perfil");
            btnEditar.setOnAction(e -> new PerfilView().start(loggedInUserId));
            actions.getChildren().add(btnEditar);
        } else {
            Button btnSeguir = new Button(yaAmigo ? "✅ Siguiendo" : "➕ Seguir");
            btnSeguir.setStyle(yaAmigo
                    ? "-fx-background-color: rgba(16,124,16,0.2); -fx-text-fill: #6fcf97; -fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;"
                    : "-fx-background-color: #0078d4; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;"
            );
            btnSeguir.setDisable(yaAmigo);

            btnSeguir.setOnAction(e -> {
                boolean ok = service.agregarAmigo(loggedInUserId, targetUsername);
                // Actualizamos UI independientemente del resultado (ya podría estar registrado)
                btnSeguir.setText("✅ Siguiendo");
                btnSeguir.setStyle(
                        "-fx-background-color: rgba(16,124,16,0.2); -fx-text-fill: #6fcf97; " +
                                "-fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;"
                );
                btnSeguir.setDisable(true);
                if (!ok) System.out.println("[PublicProfileView] Nota: ya era amigo o error en BD.");
            });

            actions.getChildren().add(btnSeguir);
        }

        HBox userRow = new HBox(16);
        userRow.setAlignment(Pos.CENTER_LEFT);
        userRow.getChildren().addAll(avatar, new VBox(4, lblUser, lblStats));

        headerBox.getChildren().addAll(userRow, actions);

        // ─── STATS CARDS ───
        HBox statsRow = new HBox(16);
        statsRow.setPadding(new Insets(20, 30, 20, 30));
        statsRow.setStyle(
                "-fx-background-color: #1a1a1a; " +
                        "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0;"
        );
        statsRow.getChildren().addAll(
                createStatCard("🎮 Total juegos", String.valueOf(totalJuegos), "#0078d4"),
                createStatCard("⭐ Media",         String.format("%.1f / 10", mediaNotas), "#ffb900")
        );

        // ─── TABLA DE JUEGOS ───
        VBox tableSection = new VBox(12);
        tableSection.setPadding(new Insets(24, 30, 30, 30));

        Label tableTitle = new Label(esPropioP ? "Mi Biblioteca" : "Biblioteca de " + targetUsername);
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: white;");

        TableView<Videojuego> tabla = new TableView<>();
        tabla.setStyle(
                "-fx-background-color: #1a1a1a; -fx-border-color: rgba(255,255,255,0.07); " +
                        "-fx-border-radius: 10; -fx-background-radius: 10;"
        );
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("Esta biblioteca está vacía."));

        TableColumn<Videojuego, String>  colTit  = new TableColumn<>("🎮 Juego");
        TableColumn<Videojuego, String>  colEst  = new TableColumn<>("Estado");
        TableColumn<Videojuego, Integer> colNot  = new TableColumn<>("⭐ Nota");
        TableColumn<Videojuego, String>  colRes  = new TableColumn<>("📝 Reseña");

        colTit.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colEst.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colNot.setCellValueFactory(new PropertyValueFactory<>("valoracion"));
        colRes.setCellValueFactory(new PropertyValueFactory<>("resena"));

        colNot.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null || item == 0 ? "—" : item + "/10");
            }
        });

        colEst.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                String color = switch (item.toLowerCase()) {
                    case "jugando" -> "#107c10";
                    case "jugado"  -> "#0078d4";
                    default        -> "#ffb900";
                };
                setText(item.toUpperCase());
                setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: 700;", color));
            }
        });

        tabla.getColumns().addAll(colTit, colEst, colNot, colRes);
        tabla.setItems(service.obtenerJuegosDeOtroUsuario(targetUsername));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        // Doble clic → ver ficha del juego
        tabla.setOnMouseClicked(ev -> {
            if (ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 2) {
                Videojuego sel = tabla.getSelectionModel().getSelectedItem();
                if (sel != null) new GameProfileView().start(sel.getTitulo());
            }
        });

        tableSection.getChildren().addAll(tableTitle, tabla);
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        root.getChildren().addAll(headerBox, statsRow, tableSection);
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        // ─── SCENE ───
        Scene scene = new Scene(root, 900, 650);
        try {
            var css = getClass().getResource("/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
        } catch (Exception ignored) {}

        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.setScene(scene);
        stage.setTitle("STAEM — Perfil de " + targetUsername);
        try { stage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png"))); }
        catch (Exception ignored) {}
        stage.show();

        return stage;
    }

    private Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: white; " +
                        "-fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;"
        );
        return btn;
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(14, 22, 14, 22));
        card.setStyle(String.format(
                "-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 10; " +
                        "-fx-border-color: %s; -fx-border-width: 0 0 0 3;", color
        ));
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.5); -fx-font-weight: 600;");
        Label val = new Label(value);
        val.setStyle(String.format("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: %s;", color));
        card.getChildren().addAll(lbl, val);
        return card;
    }
}