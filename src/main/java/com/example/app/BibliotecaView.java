package com.example.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

        // =====================================================
        // ROOT
        // =====================================================

        VBox layoutPrincipal = new VBox(20);

        layoutPrincipal.setPadding(new Insets(20));

        // =====================================================
        // HEADER
        // =====================================================

        VBox topContainer = new VBox(10);

        Label appTitle = new Label("🎮 STAEM ");
        appTitle.getStyleClass().add("title");

        Label subtitle = new Label(
                "Gestiona tu biblioteca, descubre perfiles y comparte reseñas."
        );

        subtitle.getStyleClass().add("subtitle");

        HBox buscadorComunidad = new HBox(12);
        buscadorComunidad.setAlignment(Pos.CENTER_LEFT);

        TextField txtBusquedaUser = new TextField();
        txtBusquedaUser.setPromptText("Buscar usuario...");
        txtBusquedaUser.setPrefWidth(240);

        Button btnVisitar = new Button("🔍 Ver Perfil");

        Button btnMiPerfil = new Button("👤 Mi Perfil");

        Button btnLogOut = new Button("⛔ Cerrar Sesión");
        btnLogOut.getStyleClass().add("logout-button");

        buscadorComunidad.getChildren().addAll(
                txtBusquedaUser,
                btnVisitar,
                btnMiPerfil,
                btnLogOut
        );

        topContainer.getChildren().addAll(
                appTitle,
                subtitle,
                buscadorComunidad
        );

        // =====================================================
        // TABLA PRINCIPAL
        // =====================================================

        TableView<Videojuego> tabla = new TableView<>();

        tabla.getStyleClass().add("panel");

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tabla.setPlaceholder(
                new Label("No tienes juegos todavía.")
        );

        tabla.setOnMouseClicked(event -> {

            if (
                    event.getButton().equals(MouseButton.PRIMARY)
                            && event.getClickCount() == 2
            ) {

                Videojuego seleccionado =
                        tabla.getSelectionModel().getSelectedItem();

                if (seleccionado != null) {

                    new GameProfileView()
                            .start(seleccionado.getTitulo());
                }
            }
        });

        // =====================================================
        // COLUMNAS
        // =====================================================

        TableColumn<Videojuego, String> colTit =
                new TableColumn<>("🎮 Título");

        colTit.setCellValueFactory(
                new PropertyValueFactory<>("titulo")
        );

        TableColumn<Videojuego, String> colEst =
                new TableColumn<>("📌 Estado");

        colEst.setCellValueFactory(
                new PropertyValueFactory<>("estado")
        );

        TableColumn<Videojuego, Integer> colNot =
                new TableColumn<>("⭐ Nota");

        colNot.setCellValueFactory(
                new PropertyValueFactory<>("valoracion")
        );

        TableColumn<Videojuego, String> colRes =
                new TableColumn<>("📝 Reseña");

        colRes.setCellValueFactory(
                new PropertyValueFactory<>("resena")
        );

        tabla.getColumns().addAll(
                colTit,
                colEst,
                colNot,
                colRes
        );

        tabla.setItems(cargarDatos(usuarioId));

        // =====================================================
        // SIDEBAR
        // =====================================================

        VBox formulario = new VBox(15);

        formulario.setMinWidth(320);

        formulario.getStyleClass().add("panel");

        // =====================================================
        // TITULOS
        // =====================================================

        Label lblGestion = new Label("🎮 Gestión de Juegos");
        lblGestion.getStyleClass().add("title");

        Label lblFriends = new Label("👥 Amigos");
        lblFriends.getStyleClass().add("title");

        // =====================================================
        // BUSCADOR JUEGOS
        // =====================================================

        ComboBox<String> cbBusquedaJuego = new ComboBox<>();

        cbBusquedaJuego.setEditable(true);

        cbBusquedaJuego.setPromptText("Buscar videojuego...");

        cbBusquedaJuego.getEditor()
                .textProperty()
                .addListener((obs, oldVal, newVal) -> {

                    if (
                            newVal != null
                                    && newVal.length() > 2
                    ) {

                        cbBusquedaJuego.setItems(
                                service.buscarEnCatalogoGlobal(newVal)
                        );

                        cbBusquedaJuego.show();
                    }
                });

        // =====================================================
        // ESTADO
        // =====================================================

        ComboBox<String> cbEstado = new ComboBox<>();

        cbEstado.getItems().addAll(
                "pendiente",
                "jugando",
                "jugado"
        );

        cbEstado.setValue("pendiente");

        // =====================================================
        // NOTA
        // =====================================================

        ComboBox<Integer> cbNota = new ComboBox<>();

        cbNota.getItems().addAll(
                0, 1, 2, 3, 4, 5
        );

        cbNota.setValue(0);

        // =====================================================
        // RESEÑA
        // =====================================================

        TextArea txtResena = new TextArea();

        txtResena.setPromptText(
                "Escribe tu reseña..."
        );

        txtResena.setPrefRowCount(5);

        // =====================================================
        // SELECCIÓN TABLA -> FORM
        // =====================================================

        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, sel) -> {

                    if (sel != null) {

                        cbBusquedaJuego
                                .getEditor()
                                .setText(sel.getTitulo());

                        cbEstado.setValue(sel.getEstado());

                        cbNota.setValue(sel.getValoracion());

                        txtResena.setText(sel.getResena());
                    }
                });

        // =====================================================
        // AMIGOS
        // =====================================================

        ListView<String> lvAmigos = new ListView<>();

        lvAmigos.setPrefHeight(220);

        lvAmigos.setItems(
                cargarAmigos(usuarioId)
        );

        lvAmigos.setPlaceholder(
                new Label("No tienes amigos todavía.")
        );

        lvAmigos.setOnMouseClicked(event -> {

            if (
                    event.getButton().equals(MouseButton.PRIMARY)
                            && event.getClickCount() == 2
            ) {

                String seleccionado =
                        lvAmigos.getSelectionModel()
                                .getSelectedItem();

                if (seleccionado != null) {

                    new PublicProfileView()
                            .start(seleccionado, usuarioId);
                }
            }
        });

        // =====================================================
        // BOTONES CRUD
        // =====================================================

        Button btnAdd =
                new Button("➕ Añadir Juego");

        btnAdd.setMaxWidth(Double.MAX_VALUE);

        Button btnUpdate =
                new Button("💾 Actualizar");

        btnUpdate.getStyleClass()
                .add("success-button");

        btnUpdate.setMaxWidth(Double.MAX_VALUE);

        Button btnDelete =
                new Button("🗑 Eliminar");

        btnDelete.getStyleClass()
                .add("danger-button");

        btnDelete.setMaxWidth(Double.MAX_VALUE);

        // =====================================================
        // FORMULARIO
        // =====================================================

        formulario.getChildren().addAll(

                lblGestion,

                new Label("Juego"),
                cbBusquedaJuego,

                new Label("Estado"),
                cbEstado,

                new Label("Valoración"),
                cbNota,

                new Label("Reseña"),
                txtResena,

                btnAdd,
                btnUpdate,
                btnDelete,

                new Separator(),

                lblFriends,

                lvAmigos
        );

        // =====================================================
        // EVENTOS
        // =====================================================

        btnLogOut.setOnAction(e -> {

            stage.close();

            try {

                new LoginApp().start(new Stage());

            } catch (Exception ignored) {
            }
        });

        // =====================================================

        btnMiPerfil.setOnAction(e -> {

            String miNombre =
                    obtenerMiNombre(usuarioId);

            if (miNombre != null) {

                new PublicProfileView()
                        .start(miNombre, usuarioId);
            }
        });

        // =====================================================

        btnVisitar.setOnAction(e -> {

            String userABuscar =
                    txtBusquedaUser.getText().trim();

            String miNombre =
                    obtenerMiNombre(usuarioId);

            if (!userABuscar.isEmpty()) {

                if (
                        userABuscar.equalsIgnoreCase(miNombre)
                ) {

                    Alert alert = new Alert(
                            Alert.AlertType.INFORMATION
                    );

                    alert.setTitle("Perfil");
                    alert.setHeaderText(null);

                    alert.setContentText(
                            "Estás viendo tu propio perfil."
                    );

                    alert.getDialogPane()
                            .getStylesheets()
                            .add(
                                    getClass()
                                            .getResource("/style.css")
                                            .toExternalForm()
                            );

                    alert.showAndWait();

                    new PublicProfileView()
                            .start(miNombre, usuarioId);

                } else {

                    PublicProfileView ppv =
                            new PublicProfileView();

                    Stage profileStage =
                            ppv.start(userABuscar, usuarioId);

                    profileStage.setOnHiding(event -> {

                        lvAmigos.setItems(
                                cargarAmigos(usuarioId)
                        );
                    });
                }
            }
        });

        // =====================================================

        btnAdd.setOnAction(e -> {

            String titulo =
                    cbBusquedaJuego
                            .getEditor()
                            .getText();

            if (service.existeEnCatalogo(titulo)) {

                if (
                        service.añadirVideojuego(
                                usuarioId,
                                titulo,
                                cbEstado.getValue()
                        )
                ) {

                    tabla.setItems(
                            cargarDatos(usuarioId)
                    );

                } else {

                    mostrarAlert(
                            "Ya tienes este juego."
                    );
                }
            }
        });

        // =====================================================

        btnUpdate.setOnAction(e -> {

            Videojuego sel =
                    tabla.getSelectionModel()
                            .getSelectedItem();

            if (sel != null) {

                service.actualizarVideojuego(
                        sel.getId(),
                        cbEstado.getValue(),
                        cbNota.getValue(),
                        txtResena.getText()
                );

                tabla.setItems(
                        cargarDatos(usuarioId)
                );
            }
        });

        // =====================================================

        btnDelete.setOnAction(e -> {

            Videojuego sel =
                    tabla.getSelectionModel()
                            .getSelectedItem();

            if (
                    sel != null
                            && service.eliminarVideojuego(
                            sel.getId()
                    )
            ) {

                tabla.setItems(
                        cargarDatos(usuarioId)
                );
            }
        });

        // =====================================================
        // MAIN LAYOUT
        // =====================================================

        HBox contenedor = new HBox(20);

        contenedor.getChildren().addAll(
                formulario,
                tabla
        );

        HBox.setHgrow(
                tabla,
                Priority.ALWAYS
        );

        layoutPrincipal.getChildren().addAll(
                topContainer,
                contenedor
        );

        // =====================================================
        // SCENE
        // =====================================================

        Scene scene =
                new Scene(layoutPrincipal, 1350, 820);

        scene.getStylesheets().add(
                getClass()
                        .getResource("/style.css")
                        .toExternalForm()
        );

        stage.setMinWidth(1250);
        stage.setMinHeight(760);

        stage.setScene(scene);

        stage.setTitle("STAEM");

        stage.show();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void mostrarAlert(String texto) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setHeaderText(null);

        alert.setContentText(texto);

        alert.getDialogPane()
                .getStylesheets()
                .add(
                        getClass()
                                .getResource("/style.css")
                                .toExternalForm()
                );

        alert.showAndWait();
    }

    // =========================================================

    private String obtenerMiNombre(int id) {

        String sql =
                "select username from usuarios where id = ?";

        try (
                Connection conn =
                        ConexionDB.conectar();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {

                return rs.getString("username");
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return null;
    }

    // =========================================================

    private ObservableList<Videojuego> cargarDatos(int id) {

        ObservableList<Videojuego> lista =
                FXCollections.observableArrayList();

        String sql =
                "select * from videojuegos where usuario_id = ?";

        try (
                Connection conn =
                        ConexionDB.conectar();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);

            ResultSet rs =
                    stmt.executeQuery();

            while (rs.next()) {

                lista.add(
                        new Videojuego(
                                rs.getInt("id"),
                                rs.getInt("usuario_id"),
                                rs.getString("titulo"),
                                rs.getString("estado"),
                                rs.getInt("valoracion"),
                                rs.getString("resena")
                        )
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return lista;
    }

    // =========================================================

    private ObservableList<String> cargarAmigos(int id) {

        ObservableList<String> amigos =
                FXCollections.observableArrayList();

        String sql =
                "select amigo_nombre from amigos where usuario_id = ?";

        try (
                Connection conn =
                        ConexionDB.conectar();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);

            ResultSet rs =
                    stmt.executeQuery();

            while (rs.next()) {

                amigos.add(
                        rs.getString("amigo_nombre")
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return amigos;
    }
}