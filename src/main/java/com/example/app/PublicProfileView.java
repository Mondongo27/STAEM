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
import javafx.concurrent.Task;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PublicProfileView {

    private static final Logger LOGGER = Logger.getLogger(PublicProfileView.class.getName());
    private BibliotecaService service = new BibliotecaService();

    public Stage start(String targetUsername, int loggedInUserId) {
        Stage stage = new Stage();

        // ===== ROOT =====
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("panel");
        root.setStyle("-fx-background-color: #1a1a1a;");

        // ===== HEADER CON CARGA ASÍNCRONA =====
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Button btnBack = new Button("← Volver");
        btnBack.getStyleClass().add("btn-secondary");
        btnBack.setOnAction(e -> stage.close());

        Label lblUser = new Label("👤 " + targetUsername);
        lblUser.getStyleClass().add("title");
        lblUser.setStyle("-fx-text-fill: white;");

        Button btnSeguir = new Button("➕ Seguir");
        btnSeguir.getStyleClass().add("btn-primary");

        header.getChildren().addAll(btnBack, new Region(), lblUser, btnSeguir);
        HBox.setHgrow(new Region(), Priority.ALWAYS);

        // ===== LABEL DE ESTADÍSTICAS (con placeholder de carga) =====
        Label lblStats = new Label("🔄 Cargando estadísticas...");
        lblStats.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 14px;");
        lblStats.getStyleClass().add("subtitle");

        // ===== TABLA DE JUEGOS =====
        TableView<Videojuego> tabla = new TableView<>();
        tabla.getStyleClass().add("table-view-custom");
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("🎮 Este usuario aún no tiene juegos públicos"));

        // Columnas con renderizado mejorado
        TableColumn<Videojuego, String> colTit = new TableColumn<>("🎮 Juego");
        colTit.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colTit.setPrefWidth(200);

        TableColumn<Videojuego, String> colEst = new TableColumn<>("Estado");
        colEst.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEst.setPrefWidth(120);
        colEst.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item.toUpperCase());
                    String color = switch (item.toLowerCase()) {
                        case "jugando" -> "#107c10";
                        case "jugado" -> "#0078d4";
                        default -> "#ffb900";
                    };
                    setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: 600;", color));
                }
            }
        });

        TableColumn<Videojuego, Integer> colNot = new TableColumn<>("⭐ Nota");
        colNot.setCellValueFactory(new PropertyValueFactory<>("valoracion"));
        colNot.setPrefWidth(80);
        colNot.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) { setText("-"); setStyle(""); }
                else {
                    setText(item + "/5");
                    setStyle("-fx-text-fill: #ffd700; -fx-font-weight: 600;");
                }
            }
        });

        TableColumn<Videojuego, String> colRes = new TableColumn<>("📝 Reseña");
        colRes.setCellValueFactory(new PropertyValueFactory<>("resena"));
        colRes.setPrefWidth(250);
        colRes.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.trim().isEmpty()) { setText("Sin reseña"); setStyle("-fx-text-fill: rgba(255,255,255,0.4);"); }
                else { setText(item.length() > 40 ? item.substring(0, 40) + "..." : item); setStyle("-fx-text-fill: rgba(255,255,255,0.85);"); }
            }
        });

        tabla.getColumns().addAll(colTit, colEst, colNot, colRes);

        // ===== LAYOUT PRINCIPAL =====
        root.getChildren().addAll(header, lblStats, new Separator(), tabla);

        // ===== SCENE =====
        Scene scene = new Scene(root, 850, 600);
        cargarCSS(scene);

        stage.setTitle("👤 Perfil de " + targetUsername + " | STAEM");
        cargarIcono(stage);
        stage.setScene(scene);
        stage.setMinWidth(750);
        stage.setMinHeight(550);
        stage.show();

        // ===== CARGA ASÍNCRONA DE DATOS =====
        cargarDatosPerfil(targetUsername, loggedInUserId, lblStats, btnSeguir, tabla, stage);

        return stage;
    }

    // ===== MÉTODO PRINCIPAL DE CARGA ASÍNCRONA =====
    private void cargarDatosPerfil(String targetUsername, int loggedInUserId,
                                   Label lblStats, Button btnSeguir,
                                   TableView<Videojuego> tabla, Stage stage) {

        Task<PerfilData> task = new Task<>() {
            @Override protected PerfilData call() {
                Map<String, Object> stats = service.obtenerEstadisticas(targetUsername);
                String miNombre = service.obtenerNombrePorId(loggedInUserId);
                boolean yaEsAmigo = service.esAmigo(loggedInUserId, targetUsername);
                var juegos = service.obtenerJuegosDeOtroUsuario(targetUsername);
                return new PerfilData(stats, miNombre, yaEsAmigo, juegos);
            }
        };

        task.setOnSucceeded(e -> {
            PerfilData data = task.getValue();

            // Actualizar estadísticas
            lblStats.setText(String.format(
                    "🎮 Juegos: %s   |   ⭐ Media: %.1f",
                    data.stats.getOrDefault("total", 0),
                    data.stats.getOrDefault("media", 0.0)
            ));
            lblStats.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 14px;");

            // Configurar botón de seguir
            configurarBotonSeguir(btnSeguir, targetUsername, loggedInUserId, data.miNombre, data.yaEsAmigo, stage);

            // Cargar juegos en tabla
            tabla.setItems(data.juegos);

            // Mensaje si no hay juegos
            if (data.juegos.isEmpty()) {
                lblStats.setText(lblStats.getText() + "   |   📭 Sin juegos públicos");
            }
        });

        task.setOnFailed(evt -> {
            lblStats.setText("⚠️ Error al cargar datos");
            lblStats.setStyle("-fx-text-fill: #ff4c4c;");
            LOGGER.log(Level.SEVERE, "Error al cargar perfil de: " + targetUsername, task.getException());
        });

        new Thread(task).start();
    }

    // ===== CONFIGURACIÓN DEL BOTÓN SEGUIR =====
    private void configurarBotonSeguir(Button btnSeguir, String targetUsername, int loggedInUserId,
                                       String miNombre, boolean yaEsAmigo, Stage stage) {

        // Si es mi propio perfil
        if (miNombre != null && targetUsername.equalsIgnoreCase(miNombre)) {
            btnSeguir.setVisible(false);

            Button btnEditar = new Button("⚙ Editar Perfil");
            btnEditar.getStyleClass().add("btn-secondary");
            btnEditar.setOnAction(e -> {
                stage.close();
                new PerfilView().start(loggedInUserId);
            });
            ((HBox) btnSeguir.getParent()).getChildren().add(btnEditar);
            return;
        }

        // Si ya es amigo
        if (yaEsAmigo) {
            btnSeguir.setDisable(true);
            btnSeguir.setText("✅ Siguiendo");
            btnSeguir.getStyleClass().removeAll("btn-primary");
            btnSeguir.getStyleClass().add("btn-success");
            return;
        }

        // Acción de seguir
        btnSeguir.setOnAction(e -> {
            btnSeguir.setDisable(true);
            btnSeguir.setText("🔄 Procesando...");

            Task<Boolean> followTask = new Task<>() {
                @Override protected Boolean call() {
                    return service.agregarAmigo(loggedInUserId, targetUsername);
                }
            };

            followTask.setOnSucceeded(ev -> {
                if (followTask.getValue()) {
                    btnSeguir.setText("✅ Siguiendo");
                    btnSeguir.getStyleClass().removeAll("btn-primary");
                    btnSeguir.getStyleClass().add("btn-success");
                    mostrarAlert(stage, "¡Amigo añadido!", "Ahora ves la actividad de " + targetUsername);
                } else {
                    btnSeguir.setDisable(false);
                    btnSeguir.setText("➕ Seguir");
                    mostrarAlert(stage, "⚠️ No se pudo añadir", "Verifica que el usuario existe");
                }
            });

            followTask.setOnFailed(ev -> {
                btnSeguir.setDisable(false);
                btnSeguir.setText("➕ Seguir");
                LOGGER.log(Level.WARNING, "Error al seguir usuario", followTask.getException());
            });

            new Thread(followTask).start();
        });
    }

    // ===== DOBLE-CLICK EN TABLA =====
    private void configurarDobleClick(TableView<Videojuego> tabla, Stage stage) {
        tabla.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                Videojuego sel = tabla.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    try {
                        // Usar DetalleJuegoView en lugar de GameProfileView (que puede no existir)
                        new DetalleJuegoView().start(sel.getTitulo(), 0); // 0 = sin usuario logueado para vista pública
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Error al abrir detalles", ex);
                        mostrarAlert(stage, "⚠️ Detalles no disponibles", "La vista de detalles no está implementada aún");
                    }
                }
            }
        });
    }

    // ===== CLASE AUXILIAR PARA DATOS DEL PERFIL =====
    private static class PerfilData {
        final Map<String, Object> stats;
        final String miNombre;
        final boolean yaEsAmigo;
        final javafx.collections.ObservableList<Videojuego> juegos;

        PerfilData(Map<String, Object> stats, String miNombre, boolean yaEsAmigo,
                   javafx.collections.ObservableList<Videojuego> juegos) {
            this.stats = stats;
            this.miNombre = miNombre;
            this.yaEsAmigo = yaEsAmigo;
            this.juegos = juegos;
        }
    }

    // ===== UTILITIES =====
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
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {}
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