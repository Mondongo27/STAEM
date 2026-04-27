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
        btnLogin.setMaxWidth(Double.MAX_VALUE); // Botón ancho

        Label lblMensaje = new Label();

        // Acción del botón conectada a tu clase AuthService
        btnLogin.setOnAction(e -> {
            int id = auth.login(txtUser.getText(), txtPass.getText());
            if (id != -1) {
                lblMensaje.setText("¡Bienvenido!");

                // Cerramos la ventana de Login
                stage.close();

                // Abrimos la ventana de la Biblioteca
                BibliotecaView biblioteca = new BibliotecaView();
                biblioteca.start(id);
            } else {
                lblMensaje.setText("Error: Usuario o clave incorrectos");
                lblMensaje.setStyle("-fx-text-fill: red;");
            }
        });

        // Diseño del contenedor
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(titulo, txtUser, txtPass, btnLogin, lblMensaje);

        Scene scene = new Scene(layout, 350, 400);
        stage.setTitle("Staem Manager v1.0");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}