package com.example.app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.event.EventHandler;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(LoginApp.class.getName());

    @Override
    public void start(Stage stage) {
        AuthService auth = new AuthService();

        // ===== UI ELEMENTS =====
        Label titulo = new Label("STAEM - INICIO DE SESIÓN");
        titulo.getStyleClass().add("title");
        titulo.setAlignment(Pos.CENTER);
        titulo.setMaxWidth(Double.MAX_VALUE);

        TextField txtUser = new TextField();
        txtUser.setPromptText("Usuario");
        txtUser.getStyleClass().add("text-field");
        txtUser.setPrefHeight(40);
        // ✅ Alternativa compatible a setMaxLength(20)
        txtUser.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 20) {
                txtUser.setText(newVal.substring(0, 20));
                txtUser.positionCaret(20);
            }
        });

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Contraseña");
        txtPass.getStyleClass().add("password-field");
        txtPass.setPrefHeight(40);

        Button btnLogin = new Button("🔐 Entrar");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.getStyleClass().add("btn-primary");
        btnLogin.setPrefHeight(45);
        btnLogin.setDefaultButton(true); // ✅ Enter ejecuta este botón

        Button btnRegistro = new Button("✨ Crear cuenta nueva");
        btnRegistro.setMaxWidth(Double.MAX_VALUE);
        btnRegistro.getStyleClass().add("btn-secondary");
        btnRegistro.setPrefHeight(40);

        Label lblMensaje = new Label();
        lblMensaje.setWrapText(true);
        lblMensaje.setAlignment(Pos.CENTER);
        lblMensaje.setMaxWidth(Double.MAX_VALUE);
        lblMensaje.setStyle("-fx-text-fill: #ff4c4c; -fx-font-size: 13px;");

        // ===== UX: Foco automático =====
        txtUser.requestFocus();

        // ===== LOGIN CON ENTER FUNCIONAL (versión compatible) =====

        // txtUser: Enter → si hay pass, login; si no, foco a pass
        txtUser.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    event.consume();
                    if (txtPass.getText() != null && !txtPass.getText().isEmpty()) {
                        ejecutarLogin(auth, txtUser, txtPass, lblMensaje, stage);
                    } else {
                        txtPass.requestFocus();
                    }
                }
            }
        });

        // txtPass: Enter → ejecutar login directamente
        txtPass.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    event.consume();
                    ejecutarLogin(auth, txtUser, txtPass, lblMensaje, stage);
                }
            }
        });

        // Botón login
        btnLogin.setOnAction(e -> ejecutarLogin(auth, txtUser, txtPass, lblMensaje, stage));

        // Botón registro
        btnRegistro.setOnAction(e -> {
            stage.hide();
            mostrarVentanaRegistro(stage);
        });

        // ===== LAYOUT =====
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(40, 50, 40, 50));
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("login-container");

        VBox formFields = new VBox(12);
        formFields.setMaxWidth(350);
        formFields.getChildren().addAll(txtUser, txtPass);

        layout.getChildren().addAll(
                titulo,
                formFields,
                btnLogin,
                btnRegistro,
                lblMensaje
        );

        // ===== SCENE =====
        Scene scene = new Scene(layout, 540, 520);
        cargarCSS(scene);

        stage.setTitle("Staem Manager v1.0");
        cargarIcono(stage);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        txtUser.requestFocus();
    }

    // ===== MÉTODO CENTRALIZADO DE LOGIN =====
    private void ejecutarLogin(AuthService auth, TextField txtUser, PasswordField txtPass,
                               Label lblMensaje, Stage stage) {

        String user = txtUser.getText().trim();
        String pass = txtPass.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMensaje.setText("⚠️ Por favor, completa usuario y contraseña");
            lblMensaje.setStyle("-fx-text-fill: #ffb900; -fx-font-weight: 500;");
            txtUser.setStyle(user.isEmpty() ? "-fx-border-color: #ffb900; -fx-border-width: 2px;" : "");
            txtPass.setStyle(pass.isEmpty() ? "-fx-border-color: #ffb900; -fx-border-width: 2px;" : "");
            return;
        }

        txtUser.setStyle("");
        txtPass.setStyle("");
        lblMensaje.setStyle("-fx-text-fill: rgba(255,255,255,0.7);");
        setLoginLoadingState(true, txtUser, txtPass, lblMensaje);

        // Task asíncrono para no bloquear UI
        javafx.concurrent.Task<Integer> loginTask = new javafx.concurrent.Task<>() {
            @Override
            protected Integer call() {
                return auth.login(user, pass);
            }
        };

        loginTask.setOnSucceeded(event -> {
            int id = loginTask.getValue();
            if (id != -1) {
                lblMensaje.setText("✅ ¡Bienvenido, " + user + "!");
                lblMensaje.setStyle("-fx-text-fill: #107c10; -fx-font-weight: 600;");

                // ✅ Delay correcto con PauseTransition (play() + setOnFinished)
                PauseTransition wait = new PauseTransition(Duration.millis(300));
                wait.setOnFinished(e -> {
                    stage.close();
                    try {
                        BibliotecaView biblioteca = new BibliotecaView();
                        biblioteca.start(id);
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error al abrir biblioteca", ex);
                        mostrarErrorCritico("No se pudo cargar la biblioteca.");
                    }
                });
                wait.play(); // ← ✅ Método correcto
            } else {
                lblMensaje.setText("❌ Usuario o contraseña incorrectos");
                lblMensaje.setStyle("-fx-text-fill: #ff4c4c; -fx-font-weight: 500;");
                txtUser.setStyle("-fx-border-color: #ff4c4c; -fx-border-width: 2px;");
                txtPass.setStyle("-fx-border-color: #ff4c4c; -fx-border-width: 2px;");
                txtPass.clear();
                txtPass.requestFocus();
            }
            setLoginLoadingState(false, txtUser, txtPass, lblMensaje);
        });

        loginTask.setOnFailed(event -> {
            lblMensaje.setText("⚠️ Error de conexión. Intenta de nuevo.");
            LOGGER.log(Level.SEVERE, "Error en tarea de login", loginTask.getException());
            setLoginLoadingState(false, txtUser, txtPass, lblMensaje);
        });

        new Thread(loginTask).start();
    }

    // ===== Helper: Estado de carga =====
    private void setLoginLoadingState(boolean loading, TextField txtUser, PasswordField txtPass, Label lblMensaje) {
        txtUser.setDisable(loading);
        txtPass.setDisable(loading);
        lblMensaje.setDisable(loading);
        if (loading) {
            lblMensaje.setText("🔄 Verificando credenciales...");
            lblMensaje.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-style: italic;");
        }
    }

    // ===== Helper: Error crítico =====
    private void mostrarErrorCritico(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Crítico");
        alert.setHeaderText("⚠️ No se pudo continuar");
        alert.setContentText(mensaje + "\n\nRevisa la consola para más detalles.");
        estilizarAlert(alert);
        alert.showAndWait();
        try {
            new LoginApp().start(new Stage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al reiniciar login", e);
        }
    }

    // ===== Helper: Estilizar Alert =====
    private void estilizarAlert(Alert alert) {
        alert.getDialogPane().setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;");
        cargarCSS(alert.getDialogPane().getScene());
        alert.getDialogPane().getStyleClass().add("custom-alert");
    }

    // ===== Helper: Cargar CSS =====
    private void cargarCSS(Scene scene) {
        if (scene == null) return;
        try {
            String css = getClass().getResource("/style.css").toExternalForm();
            if (css != null) scene.getStylesheets().add(css);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar style.css", e);
        }
    }

    // ===== Helper: Cargar icono =====
    private void cargarIcono(Stage stage) {
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar el icono", e);
        }
    }

    // =========================
    // VENTANA DE REGISTRO (Corregida)
    // =========================
    private void mostrarVentanaRegistro(Stage parentStage) {
        Stage regStage = new Stage();
        regStage.initOwner(parentStage);

        Label regTitulo = new Label("✨ CREAR CUENTA STAEM");
        regTitulo.getStyleClass().add("title");
        regTitulo.setAlignment(Pos.CENTER);
        regTitulo.setMaxWidth(Double.MAX_VALUE);

        TextField nuevoUser = new TextField();
        nuevoUser.setPromptText("Usuario (3-20 caracteres)");
        nuevoUser.getStyleClass().add("text-field");
        nuevoUser.setPrefHeight(40);
        // ✅ Alternativa a setMaxLength
        nuevoUser.textProperty().addListener((o, ov, nv) -> {
            if (nv != null && nv.length() > 20) {
                nuevoUser.setText(nv.substring(0, 20));
                nuevoUser.positionCaret(20);
            }
        });

        TextField nuevoEmail = new TextField();
        nuevoEmail.setPromptText("Correo electrónico");
        nuevoEmail.getStyleClass().add("text-field");
        nuevoEmail.setPrefHeight(40);

        PasswordField nuevaPass = new PasswordField();
        nuevaPass.setPromptText("Contraseña (mín. 6 caracteres)");
        nuevaPass.getStyleClass().add("password-field");
        nuevaPass.setPrefHeight(40);
        // ✅ Validación manual en lugar de setMinLength
        nuevaPass.textProperty().addListener((o, ov, nv) -> {
            if (nv != null && nv.length() > 0 && nv.length() < 6) {
                nuevaPass.setStyle("-fx-border-color: #ffb900; -fx-border-width: 2px;");
            } else {
                nuevaPass.setStyle("");
            }
        });

        // Enter navega entre campos
        nuevoUser.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override public void handle(KeyEvent e) {
                if (e.getCode() == KeyCode.ENTER) { nuevoEmail.requestFocus(); }
            }
        });
        nuevoEmail.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override public void handle(KeyEvent e) {
                if (e.getCode() == KeyCode.ENTER) { nuevaPass.requestFocus(); }
            }
        });
        nuevaPass.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override public void handle(KeyEvent e) {
                if (e.getCode() == KeyCode.ENTER) { btnCrearFire(nuevoUser, nuevoEmail, nuevaPass, regStage); }
            }
        });

        Button btnCrear = new Button("✅ Registrar ahora");
        btnCrear.setMaxWidth(Double.MAX_VALUE);
        btnCrear.getStyleClass().add("btn-primary");
        btnCrear.setPrefHeight(45);
        btnCrear.setDefaultButton(true);

        Label lblRegistroMsg = new Label();
        lblRegistroMsg.setWrapText(true);
        lblRegistroMsg.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px;");

        btnCrear.setOnAction(e -> btnCrearFire(nuevoUser, nuevoEmail, nuevaPass, regStage));

        Button btnVolver = new Button("← Volver al login");
        btnVolver.setMaxWidth(Double.MAX_VALUE);
        btnVolver.getStyleClass().add("btn-secondary");
        btnVolver.setOnAction(e -> {
            regStage.close();
            parentStage.show();
            parentStage.toFront();
        });

        VBox root = new VBox(18);
        root.setPadding(new Insets(35, 45, 35, 45));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("login-container");

        VBox formRegistro = new VBox(12);
        formRegistro.setMaxWidth(380);
        formRegistro.getChildren().addAll(nuevoUser, nuevoEmail, nuevaPass);

        root.getChildren().addAll(regTitulo, formRegistro, btnCrear, btnVolver, lblRegistroMsg);

        Scene scene = new Scene(root, 480, 480);
        cargarCSS(scene);

        regStage.setTitle("Staem - Registro");
        cargarIcono(regStage);
        regStage.setScene(scene);
        regStage.setResizable(false);
        regStage.show();

        nuevoUser.requestFocus();
    }

    // ===== Método de registro (corregido) =====
    private void btnCrearFire(TextField user, TextField email, PasswordField pass, Stage stage) {
        String u = user.getText().trim();
        String e = email.getText().trim();
        String p = pass.getText();

        if (u.isEmpty() || e.isEmpty() || p.isEmpty()) {
            mostrarMensajeTemporal("⚠️ Completa todos los campos", user, email, pass);
            return;
        }
        if (u.length() < 3) {
            user.setStyle("-fx-border-color: #ff4c4c; -fx-border-width: 2px;");
            mostrarMensajeTemporal("El usuario debe tener 3+ caracteres", user, null, null);
            return;
        }
        if (!e.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            email.setStyle("-fx-border-color: #ff4c4c; -fx-border-width: 2px;");
            mostrarMensajeTemporal("Ingresa un email válido", null, email, null);
            return;
        }
        if (p.length() < 6) {
            pass.setStyle("-fx-border-color: #ff4c4c; -fx-border-width: 2px;");
            mostrarMensajeTemporal("La contraseña debe tener 6+ caracteres", null, null, pass);
            return;
        }

        user.setStyle(""); email.setStyle(""); pass.setStyle("");

        AuthService auth = new AuthService();

        javafx.concurrent.Task<Boolean> regTask = new javafx.concurrent.Task<>() {
            @Override protected Boolean call() {
                return auth.registrarUsuario(u, p, e);
            }
        };

        regTask.setOnSucceeded(event -> {
            if (regTask.getValue()) {
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("STAEM");
                alerta.setHeaderText("✅ ¡Registro exitoso!");
                alerta.setContentText("Bienvenido a Staem, " + u + " 🎮\n\nYa puedes iniciar sesión.");
                estilizarAlert(alerta);
                alerta.showAndWait();

                // ✅ Delay correcto para cerrar registro
                PauseTransition delay = new PauseTransition(Duration.millis(200));
                delay.setOnFinished(ev -> {
                    stage.close();
                    // Opcional: volver al login con usuario pre-llenado
                });
                delay.play();
            } else {
                mostrarMensajeTemporal("❌ Usuario o email ya registrado", user, email, null);
                user.setStyle("-fx-border-color: #ff4c4c; -fx-border-width: 2px;");
                email.setStyle("-fx-border-color: #ff4c4c; -fx-border-width: 2px;");
            }
        });

        regTask.setOnFailed(event -> {
            mostrarMensajeTemporal("⚠️ Error de conexión", user, email, pass);
            LOGGER.log(Level.SEVERE, "Error en registro", regTask.getException());
        });

        new Thread(regTask).start();
    }

    // ===== Helper: Mensaje temporal con delay correcto =====
    private void mostrarMensajeTemporal(String msg, TextField f1, TextField f2, PasswordField f3) {
        System.out.println("[Registro] " + msg);
        if (f1 != null) f1.setStyle("-fx-border-color: #ff4c4c; -fx-border-width: 2px;");
        if (f2 != null) f2.setStyle("-fx-border-color: #ff4c4c; -fx-border-width: 2px;");
        if (f3 != null) f3.setStyle("-fx-border-color: #ff4c4c; -fx-border-width: 2px;");

        // ✅ Delay correcto para quitar borde
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            if (f1 != null) f1.setStyle("");
            if (f2 != null) f2.setStyle("");
            if (f3 != null) f3.setStyle("");
        });
        delay.play();
    }

    public static void main(String[] args) {
        launch();
    }
}