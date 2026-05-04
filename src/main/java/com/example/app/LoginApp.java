package com.example.app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginApp extends Application {

    @Override
    public void start(Stage stage) {

        AuthService auth = new AuthService();

        // ===== UI =====
        Label titulo = new Label("STAEM - INICIO DE SESIÓN");
        titulo.getStyleClass().add("title");

        TextField txtUser = new TextField();
        txtUser.setPromptText("usuario");

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("contraseña");

        Button btnLogin = new Button("Entrar");
        btnLogin.setMaxWidth(Double.MAX_VALUE);

        Button btnRegistro = new Button("Crear cuenta nueva");
        btnRegistro.setMaxWidth(Double.MAX_VALUE);

        Label lblMensaje = new Label();

        // ===== LOGIN =====
        btnLogin.setOnAction(e -> {
            int id = auth.login(txtUser.getText(), txtPass.getText());

            if (id != -1) {
                lblMensaje.setText("¡Bienvenido!");
                stage.close();

                BibliotecaView biblioteca = new BibliotecaView();
                biblioteca.start(id);

            } else {
                lblMensaje.setText("Error: Usuario o clave incorrectos");
                lblMensaje.setStyle("-fx-text-fill: #ff4c4c;");
            }
        });

        // ===== REGISTRO =====
        btnRegistro.setOnAction(e -> mostrarVentanaRegistro());

        // ===== LAYOUT =====
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(
                titulo,
                txtUser,
                txtPass,
                btnLogin,
                btnRegistro,
                lblMensaje
        );

        // ===== SCENE =====
        Scene scene = new Scene(layout, 350, 400);

        // 🔥 CSS (IMPORTANTE: ruta correcta)
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        stage.setTitle("Staem Manager v1.0");
        stage.setScene(scene);
        stage.show();
    }

    // =========================
    // REGISTRO
    // =========================
    private void mostrarVentanaRegistro() {

        Stage regStage = new Stage();

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label regTitulo = new Label("REGISTRO DE USUARIO");
        regTitulo.getStyleClass().add("title");

        TextField nuevoUser = new TextField();
        nuevoUser.setPromptText("Nombre de usuario");

        TextField nuevoEmail = new TextField();
        nuevoEmail.setPromptText("Correo electrónico");

        PasswordField nuevaPass = new PasswordField();
        nuevaPass.setPromptText("Contraseña");

        Button btnCrear = new Button("Registrar ahora");
        btnCrear.setMaxWidth(Double.MAX_VALUE);

        btnCrear.setOnAction(ev -> {

            AuthService auth = new AuthService();

            if (nuevoUser.getText().isEmpty()
                    || nuevoEmail.getText().isEmpty()
                    || nuevaPass.getText().isEmpty()) {

                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setContentText("Por favor, rellena todos los campos.");
                alerta.show();
                return;
            }

            if (auth.registrarUsuario(
                    nuevoUser.getText(),
                    nuevaPass.getText(),
                    nuevoEmail.getText()
            )) {

                Alert alerta = new Alert(Alert.AlertType.INFORMATION);

                alerta.setTitle("STAEM");
                alerta.setHeaderText("✅ Registro completado");
                alerta.setContentText("¡Cuenta creada con éxito!");

                DialogPane dialogPane = alerta.getDialogPane();

                /* CARGAR CSS */
                dialogPane.getStylesheets().add(
                        getClass().getResource("/style.css").toExternalForm()
                );

                /* CLASE PERSONALIZADA */
                dialogPane.getStyleClass().add("custom-alert");

                alerta.showAndWait();

                regStage.close();

            } else {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setContentText("Error: usuario o email duplicado.");
                alerta.show();
            }
        });

        root.getChildren().addAll(
                regTitulo,
                nuevoUser,
                nuevoEmail,
                nuevaPass,
                btnCrear
        );

        Scene scene = new Scene(root, 300, 320);

        // 🔥 MISMO CSS TAMBIÉN AQUÍ
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        regStage.setTitle("Staem - Registro");
        regStage.setScene(scene);
        regStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}