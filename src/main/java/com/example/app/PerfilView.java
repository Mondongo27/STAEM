package com.example.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Ventana para editar los datos del perfil del usuario (nombre, email, contraseña).
 * Los campos vienen prerellenados con los valores actuales.
 * Dejar un campo sin cambios → el valor se conserva.
 * Dejar la contraseña en blanco → la contraseña no cambia.
 */
public class PerfilView {

    public void start(int usuarioId) {

        Stage stage = new Stage();
        BibliotecaService service = new BibliotecaService();

        // Cargar datos actuales
        Map<String, String> datos = service.obtenerDatosCompletosUsuario(usuarioId);
        String usernameActual = datos.getOrDefault("username", "");
        String emailActual    = datos.getOrDefault("email",    "");

        // ─── ROOT ───
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #121212;");

        // ─── HEADER ───
        VBox headerBox = new VBox(6);
        headerBox.setPadding(new Insets(28, 30, 24, 30));
        headerBox.setStyle(
                "-fx-background-color: #1a1a1a; " +
                        "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0;"
        );

        // Avatar inicial
        StackPane avatar = new StackPane();
        avatar.setPrefSize(60, 60);
        avatar.setMaxSize(60, 60);
        avatar.setStyle("-fx-background-color: #0078d4; -fx-background-radius: 30;");
        Label avatarLabel = new Label(usernameActual.isEmpty() ? "?" : usernameActual.substring(0, 1).toUpperCase());
        avatarLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: white;");
        avatar.getChildren().add(avatarLabel);

        Label titulo = new Label("Editar Perfil");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: white;");
        Label subtitulo = new Label("Los cambios se aplican inmediatamente.");
        subtitulo.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.45);");

        HBox userRow = new HBox(16);
        userRow.setAlignment(Pos.CENTER_LEFT);
        userRow.getChildren().addAll(avatar, new VBox(4, titulo, subtitulo));
        headerBox.getChildren().add(userRow);

        // ─── FORMULARIO ───
        VBox formBox = new VBox(14);
        formBox.setPadding(new Insets(28, 30, 30, 30));

        // Usuario
        Label lblUser = createFormLabel("Nombre de usuario");
        TextField txtUser = createTextField();
        txtUser.setText(usernameActual);
        txtUser.setPromptText("Nombre de usuario");

        // Email
        Label lblEmail = createFormLabel("Correo electrónico");
        TextField txtEmail = createTextField();
        txtEmail.setText(emailActual);
        txtEmail.setPromptText("Correo electrónico");

        // Contraseña
        Label lblPass = createFormLabel("Nueva contraseña");
        Label hintPass = new Label("(Déjalo vacío para no cambiarla)");
        hintPass.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.3);");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Nueva contraseña");
        txtPass.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: white; " +
                        "-fx-prompt-text-fill: rgba(255,255,255,0.35); " +
                        "-fx-background-radius: 8; -fx-border-color: transparent; -fx-padding: 10 14;"
        );

        // Mensaje de estado
        Label lblMsg = new Label();
        lblMsg.setWrapText(true);
        lblMsg.setStyle("-fx-font-size: 13px;");

        // Botón guardar
        Button btnGuardar = new Button("💾 Guardar Cambios");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setDefaultButton(true);
        btnGuardar.setStyle(
                "-fx-background-color: #0078d4; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-font-weight: 700; " +
                        "-fx-background-radius: 8; -fx-padding: 12; -fx-cursor: hand;"
        );

        // Enter en campos navega al siguiente
        txtUser.setOnAction(e -> txtEmail.requestFocus());
        txtEmail.setOnAction(e -> txtPass.requestFocus());
        txtPass.setOnAction(e -> btnGuardar.fire());

        btnGuardar.setOnAction(e -> {
            String nuevoUser  = txtUser.getText().trim();
            String nuevoEmail = txtEmail.getText().trim();
            String nuevaPass  = txtPass.getText().trim();

            // Validaciones mínimas
            if (nuevoUser.isEmpty()) {
                setMsg(lblMsg, "⚠ El nombre de usuario no puede estar vacío.", "#ffb900");
                txtUser.requestFocus();
                return;
            }
            if (nuevoEmail.isEmpty() || !nuevoEmail.contains("@")) {
                setMsg(lblMsg, "⚠ Introduce un email válido.", "#ffb900");
                txtEmail.requestFocus();
                return;
            }
            if (!nuevaPass.isEmpty() && nuevaPass.length() < 4) {
                setMsg(lblMsg, "⚠ La contraseña debe tener al menos 4 caracteres.", "#ffb900");
                txtPass.requestFocus();
                return;
            }

            // null = no cambiar contraseña
            String passParam = nuevaPass.isEmpty() ? null : nuevaPass;

            btnGuardar.setDisable(true);
            btnGuardar.setText("Guardando...");

            boolean ok = service.actualizarDatosUsuario(usuarioId, nuevoUser, nuevoEmail, passParam);

            if (ok) {
                setMsg(lblMsg, "✅ Perfil actualizado correctamente.", "#6fcf97");
                txtPass.clear();
                // Actualizar avatar con la inicial del nuevo nombre
                avatarLabel.setText(nuevoUser.substring(0, 1).toUpperCase());
            } else {
                setMsg(lblMsg, "❌ No se pudo actualizar. El nombre o email podría estar en uso.", "#ff4c4c");
            }
            btnGuardar.setDisable(false);
            btnGuardar.setText("💾 Guardar Cambios");
        });

        formBox.getChildren().addAll(
                lblUser,  txtUser,
                lblEmail, txtEmail,
                new HBox(6, lblPass, hintPass),
                txtPass,
                btnGuardar,
                lblMsg
        );

        VBox.setVgrow(formBox, Priority.ALWAYS);
        root.getChildren().addAll(headerBox, formBox);

        // ─── SCENE ───
        Scene scene = new Scene(root, 440, 500);
        try {
            var css = getClass().getResource("/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
        } catch (Exception ignored) {}

        stage.setMinWidth(380);
        stage.setMinHeight(440);
        stage.setScene(scene);
        stage.setTitle("STAEM — Editar Perfil");
        stage.setResizable(false);
        try { stage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png"))); }
        catch (Exception ignored) {}
        stage.show();

        txtUser.requestFocus();
    }

    // ─── Helpers ───

    private Label createFormLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 12px; -fx-font-weight: 600;");
        return l;
    }

    private TextField createTextField() {
        TextField tf = new TextField();
        tf.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: white; " +
                        "-fx-prompt-text-fill: rgba(255,255,255,0.35); " +
                        "-fx-background-radius: 8; -fx-border-color: transparent; -fx-padding: 10 14;"
        );
        return tf;
    }

    private void setMsg(Label lbl, String text, String color) {
        lbl.setText(text);
        lbl.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 13px;", color));
    }
}