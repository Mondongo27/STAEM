package com.mycompany;

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
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Map<String, Object> stats = service.obtenerEstadisticas(targetUsername);
        Label lblUser = new Label("Perfil de: " + targetUsername);
        lblUser.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2F52A2;");

        Label lblStats = new Label(String.format("Juegos: %s  |  Media: %.1f ⭐",
                stats.getOrDefault("total", 0), stats.getOrDefault("media", 0.0)));

        // --- LÓGICA DEL BOTÓN SEGUIR ---
        Button btnSeguir = new Button("➕ Seguir Usuario");
        btnSeguir.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        // 1. Obtener mi nombre para comparar (Lo movemos arriba para que las comprobaciones funcionen)
        String miNombre = service.obtenerNombrePorId(loggedInUserId);

        // Definimos el header aquí para poder añadirle cosas después
        HBox header = new HBox(20, lblUser, btnSeguir);

        // 2. Validaciones: Si soy yo, ocultar Seguir y mostrar Editar. Si ya somos amigos, desactivar.
        if (targetUsername.equalsIgnoreCase(miNombre)) {
            btnSeguir.setVisible(false); // No puedes seguirte a ti mismo

            // BOTÓN EDITAR (Solo visible en tu propio perfil)
            Button btnEditar = new Button("⚙️ Editar Perfil");
            btnEditar.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");
            btnEditar.setOnAction(e -> abrirDialogoEdicion(loggedInUserId, stage));
            header.getChildren().add(btnEditar);
        } else if (service.esAmigo(loggedInUserId, targetUsername)) {
            btnSeguir.setDisable(true);
            btnSeguir.setText("✅ Siguiendo");
        }

        btnSeguir.setOnAction(e -> {
            if (service.agregarAmigo(loggedInUserId, targetUsername)) {
                btnSeguir.setDisable(true);
                btnSeguir.setText("✅ Siguiendo");
            }
        });

        TableView<Videojuego> tabla = new TableView<>();
        TableColumn<Videojuego, String> colTit = new TableColumn<>("Título");
        colTit.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        TableColumn<Videojuego, String> colEst = new TableColumn<>("Estado");
        colEst.setCellValueFactory(new PropertyValueFactory<>("estado"));
        TableColumn<Videojuego, Integer> colNot = new TableColumn<>("Nota");
        colNot.setCellValueFactory(new PropertyValueFactory<>("valoracion"));
        TableColumn<Videojuego, String> colRes = new TableColumn<>("Reseña");
        colRes.setCellValueFactory(new PropertyValueFactory<>("resena"));

        tabla.getColumns().addAll(colTit, colEst, colNot, colRes);
        tabla.setItems(service.obtenerJuegosDeOtroUsuario(targetUsername));

        // Dentro del método start de PublicProfileView, busca la tabla y añade:
        tabla.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                Videojuego seleccionado = tabla.getSelectionModel().getSelectedItem();
                if (seleccionado != null) {
                    new GameProfileView().start(seleccionado.getTitulo());
                }
            }
        });

        root.getChildren().addAll(header, lblStats, new Separator(), tabla);
        stage.setScene(new Scene(root, 700, 500));
        stage.setTitle("Perfil de " + targetUsername);

        stage.show();
        return stage;
    }

    private void abrirDialogoEdicion(int userId, Stage parentStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Editar mis datos");
        BibliotecaService service = new BibliotecaService();
        Map<String, String> datos = service.obtenerDatosCompletosUsuario(userId);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        TextField txtUser = new TextField(datos.getOrDefault("username", ""));
        TextField txtEmail = new TextField(datos.getOrDefault("email", ""));
        PasswordField txtPass = new PasswordField();
        txtPass.setText(datos.getOrDefault("password", ""));

        Button btnGuardar = new Button("Guardar Cambios");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> {
            if (service.actualizarDatosUsuario(userId, txtUser.getText(), txtEmail.getText(), txtPass.getText())) {
                new Alert(Alert.AlertType.INFORMATION, "Datos actualizados. Reinicia sesión para ver los cambios.").show();
                dialog.close();
                parentStage.close();
            }
        });

        layout.getChildren().addAll(
                new Label("Nombre de usuario:"), txtUser,
                new Label("Email:"), txtEmail,
                new Label("Nueva Contraseña:"), txtPass,
                btnGuardar
        );

        dialog.setScene(new Scene(layout, 300, 350));
        dialog.show();
    }
}