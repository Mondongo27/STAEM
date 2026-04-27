package com.mycompany;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Lanzamos la ventana de Login para empezar la app
            LoginApp loginApp = new LoginApp();
            loginApp.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Este método inicia el ciclo de vida de JavaFX
        launch(args);
    }
}