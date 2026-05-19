package com.example.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PerfilView {

    private static final Logger LOGGER = Logger.getLogger(PerfilView.class.getName());
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private BibliotecaService service = new BibliotecaService();

    public void start(int usuarioId) {
        Stage stage = new Stage();

        // ===== ROOT =====
        VBox root = new VBox(22);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("panel");
        root.setStyle("-fx-background-color: #1a1a1a;");

        // ===== HEADER =====
        Label titulo = new Label("⚙ EDITAR PERFIL");
        titulo.getStyleClass().add("title");
        titulo.setStyle("-fx-text-fill: white;");

        Label subtitulo = new Label("Personaliza tu cuenta de STAEM");
        subtitulo.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 14px;");

        // ===== INPUTS CON VALIDACIÓN =====
        TextField txtUser = new TextField();
        txtUser.setPromptText("Nuevo nombre de usuario");
        txtUser.getStyleClass().add("text-field");
        txtUser.setPrefHeight(40);
        // En lugar de: txtUser.setMaxLength(20);
// Usa un listener que trunca el texto:
        txtUser.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 20) {
                txtUser.setText(newVal.substring(0, 20));
                txtUser.positionCaret(20); // Mantiene el cursor al final
            }
        });

        // Validación en tiempo real para usuario
        txtUser.textProperty().addListener((o, ov, nv) -> {
            if (nv != null && nv.length() > 0 && nv.length() < 3) {
                txtUser.setStyle("-fx-border-color: #ffb900; -fx-border-width: 2px;");
            } else {
                txtUser.setStyle("");
            }
        });

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Nuevo correo electrónico");
        txtEmail.getStyleClass().add("text-field");
        txtEmail.setPrefHeight(40);

        // Validación de email en tiempo real
        txtEmail.textProperty().addListener((o, ov, nv) -> {
            if (nv != null && !nv.isEmpty() && !EMAIL_PATTERN.matcher(nv).matches()) {
                txtEmail.setStyle("-fx-border-color: #ffb900; -fx-border-width: 2px;");
            } else {
                txtEmail.setStyle("");
            }
        });

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Nueva contraseña (opcional, mín. 6 caracteres)");
        txtPass.getStyleClass().add("password-field");
        txtPass.setPrefHeight(40);

        // Indicador de fortaleza de contraseña
        Label lblPassStrength = new Label();
        lblPassStrength.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.5);");
        txtPass.textProperty().addListener((o, ov, nv) -> {
            if (nv != null && !nv.isEmpty()) {
                if (nv.length() < 6) {
                    lblPassStrength.setText("⚠️ Muy débil (mín. 6 caracteres)");
                    lblPassStrength.setStyle("-fx-text-fill: #ff4c4c;");
                    txtPass.setStyle("-fx-border-color: #ff4c4c; -fx-border-width: 2px;");
                } else if (nv.length() < 10) {
                    lblPassStrength.setText("✓ Aceptable");
                    lblPassStrength.setStyle("-fx-text-fill: #ffb900;");
                    txtPass.setStyle("");
                } else {
                    lblPassStrength.setText("✓ Fuerte");
                    lblPassStrength.setStyle("-fx-text-fill: #107c10;");
                    txtPass.setStyle("");
                }
            } else {
                lblPassStrength.setText("");
                txtPass.setStyle("");
            }
        });

        // ===== BOTONES =====
        Button btnGuardar = new Button("💾 Guardar Cambios");
        btnGuardar.getStyleClass().add("btn-primary");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setPrefHeight(45);
        btnGuardar.setDefaultButton(true); // Enter ejecuta guardar

        Button btnCancelar = new Button("✕ Cancelar");
        btnCancelar.getStyleClass().add("btn-secondary");
        btnCancelar.setMaxWidth(Double.MAX_VALUE);
        btnCancelar.setPrefHeight(40);
        btnCancelar.setOnAction(e -> stage.close());

        // ===== LABEL DE MENSAJES =====
        Label lblMensaje = new Label();
        lblMensaje.setWrapText(true);
        lblMensaje.setAlignment(Pos.CENTER);
        lblMensaje.setMaxWidth(Double.MAX_VALUE);
        lblMensaje.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 13px;");

        // ===== CARGAR DATOS ACTUALES =====
        Map<String, String> datos = service.obtenerDatosCompletosUsuario(usuarioId);
        String usuarioActual = datos.getOrDefault("username", "");
        String emailActual = datos.getOrDefault("email", "");

        // Mostrar valores actuales como placeholder + label informativo
        txtUser.setPromptText("Actual: " + usuarioActual);
        txtEmail.setPromptText("Actual: " + emailActual);

        Label lblInfo = new Label("💡 Deja vacío para mantener el valor actual");
        lblInfo.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 12px; -fx-font-style: italic;");

        // ===== EVENTO GUARDAR =====
        btnGuardar.setOnAction(e -> guardarCambios(usuarioId, txtUser, txtEmail, txtPass, lblMensaje, stage));

        // ===== LAYOUT =====
        VBox formBox = new VBox(14);
        formBox.setMaxWidth(380);
        formBox.getChildren().addAll(
                new Label("👤 Nombre de usuario"), txtUser,
                new Label("📧 Correo electrónico"), txtEmail,
                new Label("🔐 Nueva contraseña"), txtPass, lblPassStrength,
                lblInfo
        );

        root.getChildren().addAll(
                titulo, subtitulo, new Separator(),
                formBox,
                btnGuardar, btnCancelar,
                lblMensaje
        );

        // ===== SCENE =====
        Scene scene = new Scene(root, 450, 620);
        cargarCSS(scene);

        stage.setTitle("⚙ Editar Perfil | STAEM");
        cargarIcono(stage);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Foco inicial
        txtUser.requestFocus();
    }

    // ===== MÉTODO DE GUARDADO ASÍNCRONO =====
    private void guardarCambios(int usuarioId, TextField txtUser, TextField txtEmail,
                                PasswordField txtPass, Label lblMensaje, Stage stage) {

        // Obtener valores con lógica de "vacío = mantener actual"
        String nuevoUser = txtUser.getText().trim();
        String nuevoEmail = txtEmail.getText().trim();
        String nuevaPass = txtPass.getText().trim();

        // Validaciones
        if (!nuevoUser.isEmpty() && nuevoUser.length() < 3) {
            mostrarError(lblMensaje, "El usuario debe tener al menos 3 caracteres", txtUser);
            return;
        }
        if (!nuevoEmail.isEmpty() && !EMAIL_PATTERN.matcher(nuevoEmail).matches()) {
            mostrarError(lblMensaje, "Ingresa un correo electrónico válido", txtEmail);
            return;
        }
        if (!nuevaPass.isEmpty() && nuevaPass.length() < 6) {
            mostrarError(lblMensaje, "La contraseña debe tener al menos 6 caracteres", txtPass);
            return;
        }

        // Deshabilitar UI durante guardado
        setSavingState(true, txtUser, txtEmail, txtPass, lblMensaje);
        lblMensaje.setText("🔄 Guardando cambios...");
        lblMensaje.setStyle("-fx-text-fill: rgba(255,255,255,0.7);");

        // Task asíncrono
        Task<Boolean> saveTask = new Task<>() {
            @Override protected Boolean call() {
                // Si está vacío, pasar null para que el service mantenga el valor actual
                String userParam = nuevoUser.isEmpty() ? null : nuevoUser;
                String emailParam = nuevoEmail.isEmpty() ? null : nuevoEmail;
                String passParam = nuevaPass.isEmpty() ? null : nuevaPass;

                return service.actualizarDatosUsuario(usuarioId, userParam, emailParam, passParam);
            }
        };

        saveTask.setOnSucceeded(e -> {
            if (saveTask.getValue()) {
                lblMensaje.setText("✅ Perfil actualizado correctamente");
                lblMensaje.setStyle("-fx-text-fill: #107c10; -fx-font-weight: 600;");

                // Cerrar después de breve delay para que el usuario vea el mensaje
                javafx.animation.PauseTransition wait = new javafx.animation.PauseTransition(javafx.util.Duration.millis(800));
                wait.setOnFinished(ev -> stage.close());
                wait.play();
            } else {
                mostrarError(lblMensaje, "❌ No se pudo actualizar. Verifica que el usuario/email no estén en uso", null);
                setSavingState(false, txtUser, txtEmail, txtPass, lblMensaje);
            }
        });

        saveTask.setOnFailed(evt -> {
            mostrarError(lblMensaje, "⚠️ Error de conexión. Intenta de nuevo.", null);
            LOGGER.log(Level.SEVERE, "Error al actualizar perfil", saveTask.getException());
            setSavingState(false, txtUser, txtEmail, txtPass, lblMensaje);
        });

        new Thread(saveTask).start();
    }

    // ===== HELPERS =====
    private void mostrarError(Label lbl, String mensaje, Control campo) {
        lbl.setText(mensaje);
        lbl.setStyle("-fx-text-fill: #ff4c4c; -fx-font-weight: 500;");
        if (campo != null) {
            campo.setStyle("-fx-border-color: #ff4c4c; -fx-border-width: 2px;");
            campo.requestFocus();
            // Quitar borde después de 3 segundos
            javafx.animation.PauseTransition wait = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
            wait.setOnFinished(e -> campo.setStyle(""));
            wait.play();
        }
    }

    private void setSavingState(boolean saving, TextField txtUser, TextField txtEmail,
                                PasswordField txtPass, Label lblMensaje) {
        txtUser.setDisable(saving);
        txtEmail.setDisable(saving);
        txtPass.setDisable(saving);
        lblMensaje.setDisable(saving);
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