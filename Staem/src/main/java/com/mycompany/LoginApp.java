package com.mycompany;

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

        // Configuración de la interfaz
        Label titulo = new Label("STAEM - INICIO DE SESIÓN");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        TextField txtUser = new TextField();
        txtUser.setPromptText("usuario");

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("contraseña");

        Button btnLogin = new Button("Entrar");
        btnLogin.setMaxWidth(Double.MAX_VALUE);

        Button btnRegistro = new Button("Crear cuenta nueva");
        btnRegistro.setMaxWidth(Double.MAX_VALUE);

        Label lblMensaje = new Label();

        // Acción del botón Login
        btnLogin.setOnAction(e -> {
            int id = auth.login(txtUser.getText(), txtPass.getText());
            if (id != -1) {
                lblMensaje.setText("¡Bienvenido!");
                stage.close();
                BibliotecaView biblioteca = new BibliotecaView();
                biblioteca.start(id);
            } else {
                lblMensaje.setText("Error: Usuario o clave incorrectos");
                lblMensaje.setStyle("-fx-text-fill: red;");
            }
        });

        btnRegistro.setOnAction(e -> {
            mostrarVentanaRegistro();
        });

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(titulo, txtUser, txtPass, btnLogin, btnRegistro, lblMensaje);

        Scene scene = new Scene(layout, 350, 400);
        stage.setTitle("Staem Manager v1.0");
        stage.setScene(scene);
        stage.show();
    }

    private void mostrarVentanaRegistro() {
        Stage regStage = new Stage();
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label regTitulo = new Label("REGISTRO DE USUARIO");
        regTitulo.setStyle("-fx-font-weight: bold;");

        TextField nuevoUser = new TextField();
        nuevoUser.setPromptText("Nombre de usuario");

        // NUEVO: Campo de Email
        TextField nuevoEmail = new TextField();
        nuevoEmail.setPromptText("Correo electrónico");

        PasswordField nuevaPass = new PasswordField();
        nuevaPass.setPromptText("Contraseña");

        Button btnCrear = new Button("Registrar ahora");
        btnCrear.setMaxWidth(Double.MAX_VALUE);

        btnCrear.setOnAction(ev -> {
            AuthService auth = new AuthService();
            // Validamos que los campos no estén vacíos
            if (nuevoUser.getText().isEmpty() || nuevoEmail.getText().isEmpty() || nuevaPass.getText().isEmpty()) {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setContentText("Por favor, rellena todos los campos.");
                alerta.show();
                return;
            }

            // Enviamos los 3 parámetros al método actualizado de AuthService
            if(auth.registrarUsuario(nuevoUser.getText(), nuevaPass.getText(), nuevoEmail.getText())) {
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Registro");
                alerta.setHeaderText(null);
                alerta.setContentText("¡Cuenta creada con éxito! Ya puedes iniciar sesión.");
                alerta.show();
                regStage.close();
            } else {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setContentText("No se pudo crear la cuenta. El usuario o el email podrían estar duplicados.");
                alerta.show();
            }
        });

        // Añadimos el nuevo campo al diseño
        root.getChildren().addAll(regTitulo, nuevoUser, nuevoEmail, nuevaPass, btnCrear);
        regStage.setScene(new Scene(root, 300, 320)); // Aumentamos un poco el alto para que quepa el email
        regStage.setTitle("Staem - Nuevo Registro");
        regStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}