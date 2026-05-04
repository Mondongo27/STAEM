package com.example.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;

public class PerfilView {

    public void start(int usuarioId) {

        Stage stage = new Stage();
        BibliotecaService service = new BibliotecaService();

        VBox root = new VBox(18);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);

        // PANEL
        root.getStyleClass().add("panel");

        // TITULO
        Label titulo = new Label("⚙ EDITAR PERFIL");
        titulo.getStyleClass().add("title");

        Label subtitulo = new Label("Personaliza tu cuenta de STAEM");
        subtitulo.getStyleClass().add("subtitle");

        // INPUTS
        TextField txtUser = new TextField();
        txtUser.setPromptText("Nuevo nombre de usuario");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Nuevo correo electrónico");

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Nueva contraseña");

        // BOTÓN
        Button btnGuardar = new Button("💾 Guardar Cambios");
        btnGuardar.getStyleClass().add("success-button");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);

        // DATOS ACTUALES
        Map<String, String> datos = service.obtenerDatosCompletosUsuario(usuarioId);

        String usuarioActual = datos.getOrDefault("username", "");
        String emailActual = datos.getOrDefault("email", "");

        // EVENTO
        btnGuardar.setOnAction(e -> {

            // Si está vacío -> mantener el actual
            String nuevoUser = txtUser.getText().trim().isEmpty()
                    ? usuarioActual
                    : txtUser.getText().trim();

            String nuevoEmail = txtEmail.getText().trim().isEmpty()
                    ? emailActual
                    : txtEmail.getText().trim();

            // Si contraseña vacía -> NO cambiar contraseña
            String nuevaPass = txtPass.getText().trim().isEmpty()
                    ? null
                    : txtPass.getText().trim();

            boolean actualizado = service.actualizarDatosUsuario(
                    usuarioId,
                    nuevoUser,
                    nuevoEmail,
                    nuevaPass
            );

            if (actualizado) {

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Perfil");
                alert.setHeaderText(null);
                alert.setContentText("Perfil actualizado correctamente.");

                Scene alertScene = alert.getDialogPane().getScene();

                alertScene.getStylesheets().add(
                        getClass().getResource("/style.css").toExternalForm()
                );

                alert.showAndWait();

                stage.close();
            }
        });

        // LAYOUT
        root.getChildren().addAll(
                titulo,
                subtitulo,

                new Separator(),

                new Label("Usuario"),
                txtUser,

                new Label("Email"),
                txtEmail,

                new Label("Nueva contraseña"),
                txtPass,

                btnGuardar
        );

        // SCENE
        Scene scene = new Scene(root, 420, 520);

        var css = getClass().getResource("/style.css");

        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setMinWidth(420);
        stage.setMinHeight(520);

        stage.setScene(scene);
        stage.setTitle("MyGameList - Perfil");
        stage.show();
    }
}