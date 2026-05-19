package com.example.app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginApp extends Application {

    @Override
    public void start(Stage stage) {

        AuthService auth = new AuthService();

        // ===== UI =====
        Label titulo = new Label("🎮 STAEM");
        titulo.setStyle("-fx-font-size: 36px; -fx-font-weight: 800; -fx-text-fill: white;");

        Label subtitulo = new Label("Tu plataforma de videojuegos");
        subtitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.5);");

        TextField txtUser = new TextField();
        txtUser.setPromptText("Nombre de usuario");
        txtUser.setMaxWidth(320);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Contraseña");
        txtPass.setMaxWidth(320);

        Button btnLogin = new Button("Entrar");
        btnLogin.setMaxWidth(320);
        btnLogin.setDefaultButton(true); // Enter activa este botón siempre
        btnLogin.setStyle(
                "-fx-background-color: #0078d4; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-font-weight: 700; -fx-background-radius: 8; " +
                        "-fx-padding: 12 0; -fx-cursor: hand;"
        );

        Button btnRegistro = new Button("Crear cuenta nueva");
        btnRegistro.setMaxWidth(320);
        btnRegistro.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: rgba(255,255,255,0.85); " +
                        "-fx-font-size: 13px; -fx-font-weight: 500; -fx-background-radius: 8; " +
                        "-fx-padding: 10 0; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.15); " +
                        "-fx-border-radius: 8;"
        );

        Label lblMensaje = new Label();
        lblMensaje.setStyle("-fx-font-size: 13px;");

        // ===== Hover effects =====
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle(
                "-fx-background-color: #106ebe; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-font-weight: 700; -fx-background-radius: 8; " +
                        "-fx-padding: 12 0; -fx-cursor: hand;"
        ));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle(
                "-fx-background-color: #0078d4; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-font-weight: 700; -fx-background-radius: 8; " +
                        "-fx-padding: 12 0; -fx-cursor: hand;"
        ));

        // ===== Lógica de login (reutilizable) =====
        Runnable doLogin = () -> {
            String user = txtUser.getText().trim();
            String pass = txtPass.getText();

            if (user.isEmpty() || pass.isEmpty()) {
                lblMensaje.setText("⚠ Rellena todos los campos.");
                lblMensaje.setStyle("-fx-text-fill: #ffb900; -fx-font-size: 13px;");
                return;
            }

            btnLogin.setDisable(true);
            btnLogin.setText("Entrando...");

            int id = auth.login(user, pass);

            if (id != -1) {
                lblMensaje.setText("¡Bienvenido, " + user + "!");
                lblMensaje.setStyle("-fx-text-fill: #6fcf97; -fx-font-size: 13px;");
                stage.close();
                BibliotecaView biblioteca = new BibliotecaView();
                biblioteca.start(id);
            } else {
                lblMensaje.setText("❌ Usuario o contraseña incorrectos.");
                lblMensaje.setStyle("-fx-text-fill: #ff4c4c; -fx-font-size: 13px;");
                txtPass.clear();
                txtPass.requestFocus();
                btnLogin.setDisable(false);
                btnLogin.setText("Entrar");
            }
        };

        // Enter en cualquier campo activa el login
        btnLogin.setOnAction(e -> doLogin.run());
        txtUser.setOnAction(e -> txtPass.requestFocus()); // Enter en usuario → salta a contraseña
        txtPass.setOnAction(e -> doLogin.run());           // Enter en contraseña → hace login

        // ===== REGISTRO =====
        btnRegistro.setOnAction(e -> mostrarVentanaRegistro());

        // ===== LAYOUT =====
        VBox formBox = new VBox(14);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(320);
        formBox.getChildren().addAll(txtUser, txtPass, btnLogin, btnRegistro, lblMensaje);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(60, 40, 40, 40));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #121212;");
        layout.getChildren().addAll(titulo, subtitulo, new Region(), formBox);
        VBox.setVgrow(layout.getChildren().get(2), Priority.ALWAYS);

        // ===== SCENE =====
        Scene scene = new Scene(layout, 480, 520);

        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ignored) {}

        stage.setTitle("Staem — Inicio de sesión");
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));
        } catch (Exception ignored) {}
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Foco inicial en el campo de usuario
        txtUser.requestFocus();
    }

    // =========================
    // REGISTRO
    // =========================
    private void mostrarVentanaRegistro() {

        Stage regStage = new Stage();

        Label regTitulo = new Label("Crear cuenta");
        regTitulo.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: white;");

        Label regSub = new Label("Es gratis y solo tarda un momento");
        regSub.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.5);");

        TextField nuevoUser = new TextField();
        nuevoUser.setPromptText("Nombre de usuario");
        nuevoUser.setMaxWidth(300);

        TextField nuevoEmail = new TextField();
        nuevoEmail.setPromptText("Correo electrónico");
        nuevoEmail.setMaxWidth(300);

        PasswordField nuevaPass = new PasswordField();
        nuevaPass.setPromptText("Contraseña (mín. 4 caracteres)");
        nuevaPass.setMaxWidth(300);

        Label lblMsg = new Label();
        lblMsg.setStyle("-fx-font-size: 13px;");
        lblMsg.setWrapText(true);

        Button btnCrear = new Button("Registrarme");
        btnCrear.setMaxWidth(300);
        btnCrear.setDefaultButton(true);
        btnCrear.setStyle(
                "-fx-background-color: #0078d4; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-font-weight: 700; -fx-background-radius: 8; " +
                        "-fx-padding: 12 0; -fx-cursor: hand;"
        );

        Runnable doRegistro = () -> {
            String user  = nuevoUser.getText().trim();
            String email = nuevoEmail.getText().trim();
            String pass  = nuevaPass.getText();

            if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                lblMsg.setText("⚠ Rellena todos los campos.");
                lblMsg.setStyle("-fx-text-fill: #ffb900; -fx-font-size: 13px;");
                return;
            }

            if (pass.length() < 4) {
                lblMsg.setText("⚠ La contraseña debe tener al menos 4 caracteres.");
                lblMsg.setStyle("-fx-text-fill: #ffb900; -fx-font-size: 13px;");
                return;
            }

            if (!email.contains("@") || !email.contains(".")) {
                lblMsg.setText("⚠ El correo no parece válido.");
                lblMsg.setStyle("-fx-text-fill: #ffb900; -fx-font-size: 13px;");
                return;
            }

            AuthService auth = new AuthService();
            boolean ok = auth.registrarUsuario(user, pass, email);

            if (ok) {
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("STAEM");
                alerta.setHeaderText("✅ Registro completado");
                alerta.setContentText("¡Cuenta creada con éxito! Ya puedes iniciar sesión.");
                DialogPane dp = alerta.getDialogPane();
                try { dp.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}
                dp.getStyleClass().add("custom-alert");
                alerta.showAndWait();
                regStage.close();
            } else {
                lblMsg.setText("❌ El nombre de usuario o email ya existe.");
                lblMsg.setStyle("-fx-text-fill: #ff4c4c; -fx-font-size: 13px;");
            }
        };

        btnCrear.setOnAction(e -> doRegistro.run());
        nuevoUser.setOnAction(e -> nuevoEmail.requestFocus());
        nuevoEmail.setOnAction(e -> nuevaPass.requestFocus());
        nuevaPass.setOnAction(e -> doRegistro.run());

        VBox root = new VBox(14);
        root.setPadding(new Insets(40, 50, 40, 50));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #121212;");
        root.getChildren().addAll(
                regTitulo, regSub, new Separator(),
                nuevoUser, nuevoEmail, nuevaPass,
                btnCrear, lblMsg
        );

        Scene scene = new Scene(root, 420, 440);
        try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}

        regStage.setTitle("Staem — Registro");
        try { regStage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png"))); } catch (Exception ignored) {}
        regStage.setScene(scene);
        regStage.setResizable(false);
        regStage.show();

        nuevoUser.requestFocus();
    }

    public static void main(String[] args) {
        launch();
    }
}