package com.mycompany;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import java.sql.*;

public class BibliotecaView {

    public void start(int usuarioId) {
        Stage stage = new Stage();
        BibliotecaService service = new BibliotecaService();

        VBox layoutPrincipal = new VBox(10);
        layoutPrincipal.setPadding(new Insets(15));

        // --- BUSCADOR DE COMUNIDAD (ESTILO MYANIMELIST) ---
        HBox buscadorComunidad = new HBox(10);
        buscadorComunidad.setPadding(new Insets(0, 0, 10, 0));
        TextField txtBusquedaUser = new TextField();
        txtBusquedaUser.setPromptText("Buscar lista de usuario...");
        Button btnVisitar = new Button("Ver Perfil Público");
        btnVisitar.setStyle("-fx-background-color: #2F52A2; -fx-text-fill: white;"); // Azul MAL

        btnVisitar.setOnAction(e -> {
            String target = txtBusquedaUser.getText();
            if(!target.isEmpty()) {
                PublicProfileView perfilPublico = new PublicProfileView();
                perfilPublico.start(target);
            }
        });
        buscadorComunidad.getChildren().addAll(new Label("Comunidad:"), txtBusquedaUser, btnVisitar);

        HBox root = new HBox(20);
        VBox formulario = new VBox(10);
        formulario.setMinWidth(220);

        Label lblAdd = new Label("GESTIÓN DE JUEGOS");
        lblAdd.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ComboBox<String> cbCategoria = new ComboBox<>();
        cbCategoria.setPromptText("Selecciona Categoría");
        cbCategoria.setMaxWidth(Double.MAX_VALUE);
        cbCategoria.setItems(service.obtenerNombresCategorias());

        ComboBox<String> cbConsola = new ComboBox<>();
        cbConsola.setPromptText("Selecciona Consola");
        cbConsola.setMaxWidth(Double.MAX_VALUE);
        cbConsola.setItems(service.obtenerNombresConsolas());

        ComboBox<String> cbJuegoCatalogo = new ComboBox<>();
        cbJuegoCatalogo.setPromptText("Escribe para buscar...");
        cbJuegoCatalogo.setMaxWidth(Double.MAX_VALUE);
        cbJuegoCatalogo.setDisable(true);
        cbJuegoCatalogo.setEditable(true);

        ObservableList<String> listaJuegosMaster = FXCollections.observableArrayList();

        cbConsola.setOnAction(e -> {
            if (cbCategoria.getValue() != null && cbConsola.getValue() != null) {
                listaJuegosMaster.setAll(service.obtenerJuegosFiltrados(cbCategoria.getValue(), cbConsola.getValue()));
                cbJuegoCatalogo.setItems(listaJuegosMaster);
                cbJuegoCatalogo.setDisable(false);
            }
        });

        cbJuegoCatalogo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (cbJuegoCatalogo.isDisable()) return;
            if (newVal == null || newVal.isEmpty()) {
                cbJuegoCatalogo.setItems(listaJuegosMaster);
            } else {
                FilteredList<String> filteredData = new FilteredList<>(listaJuegosMaster, s ->
                        s.toLowerCase().contains(newVal.toLowerCase())
                );
                cbJuegoCatalogo.setItems(filteredData);
                cbJuegoCatalogo.show();
            }
        });

        ComboBox<String> cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("pendiente", "jugando", "jugado");
        cbEstado.setPromptText("Estado");
        cbEstado.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Integer> cbNota = new ComboBox<>();
        cbNota.getItems().addAll(1, 2, 3, 4, 5);
        cbNota.setPromptText("Nota (1-5)");
        cbNota.setMaxWidth(Double.MAX_VALUE);

        TextArea txtResena = new TextArea();
        txtResena.setPromptText("Escribe aquí tu reseña...");
        txtResena.setPrefRowCount(3);
        txtResena.setWrapText(true);

        Label lblMediaPublico = new Label("Media Global: -");
        lblMediaPublico.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");

        Button btnAdd = new Button("Guardar Nuevo");
        btnAdd.setMaxWidth(Double.MAX_VALUE);

        Button btnUpdate = new Button("Actualizar Seleccionado");
        btnUpdate.setMaxWidth(Double.MAX_VALUE);
        btnUpdate.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Button btnDelete = new Button("Eliminar Seleccionado");
        btnDelete.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");
        btnDelete.setMaxWidth(Double.MAX_VALUE);

        Button btnPerfil = new Button("Mi Perfil");
        btnPerfil.setMaxWidth(Double.MAX_VALUE);
        btnPerfil.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        Button btnLogOut = new Button("Cerrar Sesión");
        btnLogOut.setMaxWidth(Double.MAX_VALUE);
        btnLogOut.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");

        formulario.getChildren().addAll(
                lblAdd, cbCategoria, cbConsola, cbJuegoCatalogo, cbEstado, cbNota,
                txtResena, lblMediaPublico, btnAdd, btnUpdate, btnDelete, btnPerfil, btnLogOut
        );

        TableView<Videojuego> tabla = new TableView<>();
        HBox.setHgrow(tabla, Priority.ALWAYS);

        // --- COLORES DINÁMICOS POR ESTADO (STYLE MAL) ---
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

                    setStyle("-fx-background-color: " + colorBase + "; " +
                            "-fx-border-color: " + borde + "; " +
                            "-fx-border-width: 0 0 0 5; " +
                            "-fx-text-background-color: black;");
                }
            }
        });

        TableColumn<Videojuego, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colTitulo.setPrefWidth(200);

        TableColumn<Videojuego, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<Videojuego, Integer> colNota = new TableColumn<>("Nota");
        colNota.setCellValueFactory(new PropertyValueFactory<>("valoracion"));

        tabla.getColumns().addAll(colTitulo, colEstado, colNota);
        tabla.setItems(cargarDatos(usuarioId));

        // --- LÓGICA DE INTERACCIÓN ---
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cbJuegoCatalogo.getEditor().setText(newSelection.getTitulo());
                cbEstado.setValue(newSelection.getEstado());
                cbNota.setValue(newSelection.getValoracion());
                txtResena.setText(newSelection.getResena());
                cbCategoria.setDisable(true);
                cbConsola.setDisable(true);
                cbJuegoCatalogo.setDisable(true);

                double media = service.obtenerMediaPublico(newSelection.getTitulo());
                lblMediaPublico.setText(media == 0.0 ? "Media Global: Sin votos" : "Media Global: " + String.format("%.1f", media) + " / 5");
            }
        });

        btnAdd.setOnAction(e -> {
            String juegoSeleccionado = cbJuegoCatalogo.getEditor().getText();
            String estado = cbEstado.getValue();
            if (!juegoSeleccionado.isEmpty() && listaJuegosMaster.contains(juegoSeleccionado) && estado != null) {
                service.añadirVideojuego(usuarioId, juegoSeleccionado, estado);
                tabla.setItems(cargarDatos(usuarioId));
                limpiarFormulario(cbCategoria, cbConsola, cbJuegoCatalogo, cbEstado, cbNota, txtResena, lblMediaPublico);
            } else {
                new Alert(Alert.AlertType.WARNING, "Selecciona un juego válido del catálogo.").show();
            }
        });

        btnUpdate.setOnAction(e -> {
            Videojuego sel = tabla.getSelectionModel().getSelectedItem();
            if (sel != null && service.actualizarVideojuego(sel.getId(), cbEstado.getValue(), cbNota.getValue(), txtResena.getText())) {
                tabla.setItems(cargarDatos(usuarioId));
                limpiarFormulario(cbCategoria, cbConsola, cbJuegoCatalogo, cbEstado, cbNota, txtResena, lblMediaPublico);
            }
        });

        btnDelete.setOnAction(e -> {
            Videojuego sel = tabla.getSelectionModel().getSelectedItem();
            if (sel != null && new Alert(Alert.AlertType.CONFIRMATION, "¿Borrar juego?").showAndWait().get() == ButtonType.OK) {
                if (service.eliminarVideojuego(sel.getId())) tabla.setItems(cargarDatos(usuarioId));
            }
        });

        btnPerfil.setOnAction(e -> new PerfilView().start(usuarioId));
        btnLogOut.setOnAction(e -> { stage.close(); try { new LoginApp().start(new Stage()); } catch (Exception ex) {} });

        root.getChildren().addAll(formulario, tabla);
        layoutPrincipal.getChildren().addAll(buscadorComunidad, root);

        Scene scene = new Scene(layoutPrincipal, 900, 700);
        stage.setTitle("MyGameList - Gestión de Biblioteca");
        stage.setScene(scene);
        stage.show();
    }

    private ObservableList<Videojuego> cargarDatos(int usuarioId) {
        ObservableList<Videojuego> lista = FXCollections.observableArrayList();
        String sql = "select * from videojuegos where usuario_id = ?";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new Videojuego(rs.getInt("id"), rs.getInt("usuario_id"), rs.getString("titulo"),
                        rs.getString("estado"), rs.getInt("valoracion"), rs.getString("resena")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private void limpiarFormulario(ComboBox<String> cat, ComboBox<String> con, ComboBox<String> jue, ComboBox<String> est, ComboBox<Integer> not, TextArea res, Label med) {
        cat.setDisable(false); cat.setValue(null);
        con.setDisable(false); con.setValue(null);
        jue.setDisable(true); jue.getEditor().clear(); jue.setValue(null);
        est.setValue(null); not.setValue(null); res.clear(); med.setText("Media Global: -");
    }
}