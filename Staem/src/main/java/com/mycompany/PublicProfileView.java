package com.mycompany;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PublicProfileView {
    public void start(String targetUsername) {
        Stage stage = new Stage();
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label lblUser = new Label("Lista de juegos de: " + targetUsername);
        lblUser.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2F52A2;");

        TableView<Videojuego> tabla = new TableView<>();

        // --- CONFIGURACIÓN DE COLUMNAS (Obligatorio para que se vea algo) ---
        TableColumn<Videojuego, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colTitulo.setPrefWidth(250);

        TableColumn<Videojuego, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<Videojuego, Integer> colNota = new TableColumn<>("Nota");
        colNota.setCellValueFactory(new PropertyValueFactory<>("valoracion"));

        tabla.getColumns().addAll(colTitulo, colEstado, colNota);

        // --- COLORES POR ESTADO (Estilo MyAnimeList) ---
        tabla.setRowFactory(tv -> new TableRow<Videojuego>() {
            @Override
            protected void updateItem(Videojuego item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    String colorBase;
                    String borde;

                    switch (item.getEstado()) {
                        case "jugando": colorBase = "#e1f5fe"; borde = "#03a9f4"; break;
                        case "jugado": colorBase = "#e8f5e9"; borde = "#4caf50"; break;
                        case "pendiente": colorBase = "#f5f5f5"; borde = "#9e9e9e"; break;
                        default: colorBase = "white"; borde = "transparent"; break;
                    }

                    // Añadimos -fx-text-background-color: black para forzar que el texto sea siempre legible
                    setStyle("-fx-background-color: " + colorBase + "; " +
                            "-fx-border-color: " + borde + "; " +
                            "-fx-border-width: 0 0 0 5; " +
                            "-fx-text-background-color: black;");
                }
            }
        });

        // Carga de datos desde el service
        BibliotecaService service = new BibliotecaService();
        tabla.setItems(service.obtenerJuegosDeOtroUsuario(targetUsername));

        root.getChildren().addAll(lblUser, tabla);
        stage.setScene(new Scene(root, 600, 450));
        stage.setTitle("Perfil de " + targetUsername + " - MyGameList");
        stage.show();
    }
}