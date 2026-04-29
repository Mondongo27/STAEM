package com.mycompany;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class BibliotecaView {

    public void start(int usuarioId) {
        Stage stage = new Stage();
        BibliotecaService service = new BibliotecaService();
        VBox layoutPrincipal = new VBox(10);
        layoutPrincipal.setPadding(new Insets(15));

        // --- 1. CABECERA (Buscador, Perfil y Logout) ---
        HBox buscadorComunidad = new HBox(10);
        TextField txtBusquedaUser = new TextField();
        txtBusquedaUser.setPromptText("Buscar usuario...");

        Button btnVisitar = new Button("Ver Perfil Público");
        btnVisitar.setStyle("-fx-background-color: #2F52A2; -fx-text-fill: white;");

        Button btnMiPerfil = new Button("Mi Perfil");
        btnMiPerfil.setStyle("-fx-background-color: #03A9F4; -fx-text-fill: white;");

        Button btnLogOut = new Button("Cerrar Sesión");
        btnLogOut.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");

        buscadorComunidad.getChildren().addAll(new Label("Comunidad:"), txtBusquedaUser, btnVisitar, btnMiPerfil, btnLogOut);

        // --- 2. TABLA PRINCIPAL ---
        TableView<Videojuego> tabla = new TableView<>();
        tabla.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                Videojuego seleccionado = tabla.getSelectionModel().getSelectedItem();
                if (seleccionado != null) {
                    new GameProfileView().start(seleccionado.getTitulo());
                }
            }
        });
        TableColumn<Videojuego, String> colTit = new TableColumn<>("Título");
        colTit.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        TableColumn<Videojuego, String> colEst = new TableColumn<>("Estado");
        colEst.setCellValueFactory(new PropertyValueFactory<>("estado"));
        TableColumn<Videojuego, Integer> colNot = new TableColumn<>("Nota");
        colNot.setCellValueFactory(new PropertyValueFactory<>("valoracion"));
        TableColumn<Videojuego, String> colRes = new TableColumn<>("Reseña");
        colRes.setCellValueFactory(new PropertyValueFactory<>("resena"));

        tabla.getColumns().addAll(colTit, colEst, colNot, colRes);
        tabla.setItems(cargarDatos(usuarioId));

        // --- 3. FORMULARIO IZQUIERDO ---
        VBox formulario = new VBox(10);
        formulario.setMinWidth(280);

        ComboBox<String> cbBusquedaJuego = new ComboBox<>();
        cbBusquedaJuego.setEditable(true);
        cbBusquedaJuego.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 2) {
                cbBusquedaJuego.setItems(service.buscarEnCatalogoGlobal(newVal));
                cbBusquedaJuego.show();
            }
        });

        ComboBox<String> cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("pendiente", "jugando", "jugado");
        cbEstado.setValue("pendiente");

        ComboBox<Integer> cbNota = new ComboBox<>();
        cbNota.getItems().addAll(0, 1, 2, 3, 4, 5);
        cbNota.setValue(0);

        TextArea txtResena = new TextArea();
        txtResena.setPromptText("Escribe aquí tu reseña...");
        txtResena.setPrefRowCount(4);

        // Cargar datos en formulario al seleccionar fila
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, sel) -> {
            if (sel != null) {
                cbBusquedaJuego.getEditor().setText(sel.getTitulo());
                cbEstado.setValue(sel.getEstado());
                cbNota.setValue(sel.getValoracion());
                txtResena.setText(sel.getResena());
            }
        });

        ListView<String> lvAmigos = new ListView<>();
        lvAmigos.setPrefHeight(200);
        lvAmigos.setItems(cargarAmigos(usuarioId));

        // Evento Doble Clic en Amigos
        lvAmigos.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                String seleccionado = lvAmigos.getSelectionModel().getSelectedItem();
                if (seleccionado != null) {
                    new PublicProfileView().start(seleccionado, usuarioId);
                }
            }
        });

        Button btnAdd = new Button("Guardar Nuevo");
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        Button btnUpdate = new Button("Actualizar Seleccionado");
        btnUpdate.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnUpdate.setMaxWidth(Double.MAX_VALUE);
        Button btnDelete = new Button("Eliminar Seleccionado");
        btnDelete.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        btnDelete.setMaxWidth(Double.MAX_VALUE);

        formulario.getChildren().addAll(
                new Label("GESTIÓN DE JUEGOS"), cbBusquedaJuego, cbEstado, cbNota, txtResena,
                btnAdd, btnUpdate, btnDelete, new Separator(),
                new Label("MIS AMIGOS (Doble clic)"), lvAmigos
        );

        // --- 4. LÓGICA DE BOTONES ---

        btnLogOut.setOnAction(e -> {
            stage.close();
            try { new LoginApp().start(new Stage()); } catch (Exception ex) {}
        });

        btnMiPerfil.setOnAction(e -> {
            String miNombre = obtenerMiNombre(usuarioId);
            if (miNombre != null) {
                PublicProfileView ppv = new PublicProfileView();
                ppv.start(miNombre, usuarioId);
            }
        });

        btnVisitar.setOnAction(e -> {
            String userABuscar = txtBusquedaUser.getText().trim();
            String miNombre = obtenerMiNombre(usuarioId);

            if (!userABuscar.isEmpty()) {
                if (userABuscar.equalsIgnoreCase(miNombre)) {
                    // Si intenta buscarse a sí mismo, le avisamos y abrimos su perfil
                    new Alert(Alert.AlertType.INFORMATION, "Estás viendo tu propio perfil.").show();
                    new PublicProfileView().start(miNombre, usuarioId);
                } else {
                    PublicProfileView ppv = new PublicProfileView();
                    // Usamos un pequeño truco: al cerrarse la ventana de perfil, refrescamos la lista
                    Stage profileStage = ppv.start(userABuscar, usuarioId);
                    profileStage.setOnHiding(event -> {
                        lvAmigos.setItems(cargarAmigos(usuarioId));
                    });
                }
            }
        });

        btnAdd.setOnAction(e -> {
            String titulo = cbBusquedaJuego.getEditor().getText();
            if (service.existeEnCatalogo(titulo)) {
                if (service.añadirVideojuego(usuarioId, titulo, cbEstado.getValue())) {
                    tabla.setItems(cargarDatos(usuarioId));
                } else {
                    new Alert(Alert.AlertType.INFORMATION, "Ya tienes este juego").show();
                }
            }
        });

        btnUpdate.setOnAction(e -> {
            Videojuego sel = tabla.getSelectionModel().getSelectedItem();
            if (sel != null) {
                service.actualizarVideojuego(sel.getId(), cbEstado.getValue(), cbNota.getValue(), txtResena.getText());
                tabla.setItems(cargarDatos(usuarioId));
            }
        });

        btnDelete.setOnAction(e -> {
            Videojuego sel = tabla.getSelectionModel().getSelectedItem();
            if (sel != null && service.eliminarVideojuego(sel.getId())) {
                tabla.setItems(cargarDatos(usuarioId));
            }
        });

        HBox contenedor = new HBox(20, formulario, tabla);
        HBox.setHgrow(tabla, Priority.ALWAYS);
        layoutPrincipal.getChildren().addAll(buscadorComunidad, contenedor);

        stage.setScene(new Scene(layoutPrincipal, 1150, 750));
        stage.setTitle("MyGameList - Biblioteca de Usuario");
        stage.show();
    }

    private String obtenerMiNombre(int id) {
        String sql = "select username from usuarios where id = ?";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private ObservableList<Videojuego> cargarDatos(int id) {
        ObservableList<Videojuego> lista = FXCollections.observableArrayList();
        String sql = "select * from videojuegos where usuario_id = ?";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new Videojuego(rs.getInt("id"), rs.getInt("usuario_id"), rs.getString("titulo"),
                        rs.getString("estado"), rs.getInt("valoracion"), rs.getString("resena")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private ObservableList<String> cargarAmigos(int id) {
        ObservableList<String> amigos = FXCollections.observableArrayList();
        String sql = "select amigo_nombre from amigos where usuario_id = ?";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) amigos.add(rs.getString("amigo_nombre"));
        } catch (SQLException e) { e.printStackTrace(); }
        return amigos;
    }
}