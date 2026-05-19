package com.example.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.List;

public class BibliotecaView {

    // ─── Componentes de formulario (panel derecho) ───
    private ComboBox<String>  cbBusquedaJuego;
    private ComboBox<String>  cbEstado;
    private ComboBox<Integer> cbNota;
    private TextArea          txtResena;

    // ─── Lista de amigos ───
    private ListView<String> lvAmigos;

    // ─── Área principal ───
    private Label    gamesCountLabel;
    private GridPane gamesGrid;

    // ─── Estado ───
    private int                          usuarioId;
    private BibliotecaService            service;
    private ObservableList<Videojuego>   todosLosJuegos;
    private Videojuego                   juegoSeleccionado;    // referencia directa en lugar de buscar por estilo

    // ─── Filtros ───
    private Button filterAll, filterPlaying, filterPlayed, filterPending;
    private String  filtroActual = null;

    public void start(int usuarioId) {
        this.usuarioId      = usuarioId;
        this.service        = new BibliotecaService();
        this.todosLosJuegos = FXCollections.observableArrayList();

        Stage stage = new Stage();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #121212;");

        // ============================================================
        // HEADER
        // ============================================================
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setStyle(
                "-fx-background-color: #1a1a1a; " +
                        "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0;"
        );

        Label logoLabel = new Label("🎮 STAEM");
        logoLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: white;");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Buscar en mi biblioteca...");
        searchField.setPrefWidth(400);
        searchField.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: white; " +
                        "-fx-prompt-text-fill: rgba(255,255,255,0.4); -fx-background-radius: 8; " +
                        "-fx-border-color: transparent; -fx-padding: 8 16;"
        );

        HBox userActions = new HBox(12);
        userActions.setAlignment(Pos.CENTER_RIGHT);

        Button btnMiPerfil = createHeaderButton("👤 Mi Perfil");
        Button btnLogOut   = new Button("Salir");
        btnLogOut.setStyle(
                "-fx-background-color: rgba(255,69,58,0.15); -fx-text-fill: #ff453a; " +
                        "-fx-font-weight: 500; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;"
        );

        userActions.getChildren().addAll(btnMiPerfil, btnLogOut);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(logoLabel, spacer, searchField, userActions);

        // ============================================================
        // SIDEBAR
        // ============================================================
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(20, 0, 20, 20));
        sidebar.setPrefWidth(240);
        sidebar.setStyle(
                "-fx-background-color: #1a1a1a; " +
                        "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 1 0 0;"
        );

        Label menuLabel = new Label("MENÚ");
        menuLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px; " +
                        "-fx-font-weight: 600; -fx-padding: 0 0 6 16;"
        );

        Button btnLibrary  = createSidebarButton("📚 Biblioteca",    true);
        Button btnFriends  = createSidebarButton("👥 Amigos",        false);
        Button btnSettings = createSidebarButton("⚙️ Configuración", false);

        Region sep1 = new Region(); sep1.setPrefHeight(16);

        Label friendsLabel = new Label("AMIGOS");
        friendsLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px; " +
                        "-fx-font-weight: 600; -fx-padding: 0 0 6 16;"
        );

        lvAmigos = new ListView<>();
        lvAmigos.setPrefHeight(220);
        lvAmigos.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        lvAmigos.setItems(cargarAmigos(usuarioId));
        lvAmigos.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText("👤 " + item);
                    setStyle(
                            "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.85); " +
                                    "-fx-font-size: 13px; -fx-padding: 9 16; -fx-background-radius: 8;"
                    );
                }
            }
        });
        lvAmigos.setOnMouseClicked(ev -> {
            if (ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 2) {
                String sel = lvAmigos.getSelectionModel().getSelectedItem();
                if (sel != null) new PublicProfileView().start(sel, usuarioId);
            }
        });

        sidebar.getChildren().addAll(menuLabel, btnLibrary, btnFriends, btnSettings, sep1, friendsLabel, lvAmigos);

        // ============================================================
        // CONTENT AREA (centro)
        // ============================================================
        VBox contentArea = new VBox(20);
        contentArea.setPadding(new Insets(30));
        contentArea.setStyle("-fx-background-color: #121212;");
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        HBox sectionHeader = new HBox(16);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Mi Biblioteca");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: 700; -fx-text-fill: white;");
        gamesCountLabel = new Label("0 juegos");
        gamesCountLabel.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.5); " +
                        "-fx-padding: 5 12; -fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 12;"
        );
        sectionHeader.getChildren().addAll(titleLabel, gamesCountLabel);

        // Filtros
        HBox filtersBar = new HBox(10);
        filtersBar.setAlignment(Pos.CENTER_LEFT);
        filterAll     = createFilterButton("Todos",      true);
        filterPlaying = createFilterButton("Jugando",    false);
        filterPlayed  = createFilterButton("Jugados",    false);
        filterPending = createFilterButton("Pendientes", false);
        filtersBar.getChildren().addAll(filterAll, filterPlaying, filterPlayed, filterPending);

        // Grid de juegos
        gamesGrid = new GridPane();
        gamesGrid.setHgap(20);
        gamesGrid.setVgap(20);
        gamesGrid.setStyle("-fx-padding: 10 0;");

        ScrollPane scrollPane = new ScrollPane(gamesGrid);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        contentArea.getChildren().addAll(sectionHeader, filtersBar, scrollPane);

        // ============================================================
        // SIDE PANEL (derecha — gestión)
        // ============================================================
        VBox sidePanel = new VBox(12);
        sidePanel.setPadding(new Insets(30, 20, 30, 20));
        sidePanel.setPrefWidth(340);
        sidePanel.setStyle(
                "-fx-background-color: #1a1a1a; " +
                        "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 0 1;"
        );

        Label panelTitle = new Label("Gestión de Juegos");
        panelTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: white;");

        cbBusquedaJuego = new ComboBox<>();
        cbBusquedaJuego.setEditable(true);
        cbBusquedaJuego.setPromptText("Buscar videojuego...");
        cbBusquedaJuego.setMaxWidth(Double.MAX_VALUE);
        styleCombo(cbBusquedaJuego);

        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("pendiente", "jugando", "jugado");
        cbEstado.setValue("pendiente");
        cbEstado.setMaxWidth(Double.MAX_VALUE);
        styleCombo(cbEstado);

        cbNota = new ComboBox<>();
        cbNota.getItems().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        cbNota.setValue(0);
        cbNota.setMaxWidth(Double.MAX_VALUE);
        styleCombo(cbNota);

        txtResena = new TextArea();
        txtResena.setPromptText("Escribe tu reseña aquí...");
        txtResena.setPrefRowCount(5);
        txtResena.setWrapText(true);
        txtResena.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: white; " +
                        "-fx-prompt-text-fill: rgba(255,255,255,0.4); -fx-background-radius: 8; " +
                        "-fx-border-color: transparent; -fx-control-inner-background: #2a2a2a;"
        );

        Button btnAdd       = createActionButton("➕ Añadir Juego",   "#0078d4");
        Button btnUpdate    = createActionButton("💾 Actualizar",      "#107c10");
        Button btnDelete    = createActionButton("🗑 Eliminar",        "#d13438");
        Button btnAddFriend = createActionButton("👤 Añadir Amigo",   "#5a4fcf");

        Label selLabel = new Label("Ningún juego seleccionado");
        selLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 12px; " +
                        "-fx-font-style: italic; -fx-padding: 4 0;"
        );

        sidePanel.getChildren().addAll(
                panelTitle,
                createFormLabel("Juego"),   cbBusquedaJuego,
                createFormLabel("Estado"),  cbEstado,
                createFormLabel("Valoración (0–10)"), cbNota,
                createFormLabel("Reseña"),  txtResena,
                selLabel,
                btnAdd, btnUpdate, btnDelete,
                new Separator(),
                btnAddFriend
        );

        // ============================================================
        // COMPOSICIÓN PRINCIPAL
        // ============================================================
        HBox mainContent = new HBox();
        mainContent.setStyle("-fx-background-color: #121212;");
        mainContent.getChildren().addAll(sidebar, contentArea, sidePanel);

        root.setTop(header);
        root.setCenter(mainContent);

        // ============================================================
        // SCENE
        // ============================================================
        Scene scene = new Scene(root, 1600, 900);
        try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); }
        catch (Exception ignored) {}
        stage.setScene(scene);
        stage.setTitle("STAEM — Mi Biblioteca");
        try { stage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png"))); }
        catch (Exception ignored) {}
        stage.setMaximized(true);
        stage.show();

        // ============================================================
        // CARGA INICIAL
        // ============================================================
        cargarJuegosGrid();

        // ============================================================
        // EVENTOS
        // ============================================================

        // Logout
        btnLogOut.setOnAction(e -> {
            stage.close();
            try { new LoginApp().start(new Stage()); } catch (Exception ignored) {}
        });

        // Mi perfil
        btnMiPerfil.setOnAction(e -> {
            String miNombre = service.obtenerNombrePorId(usuarioId);
            if (miNombre != null) new PublicProfileView().start(miNombre, usuarioId);
        });

        // Amigos / Configuración desde sidebar
        btnFriends.setOnAction(e -> mostrarDialogoAñadirAmigo());
        btnAddFriend.setOnAction(e -> mostrarDialogoAñadirAmigo());
        btnSettings.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Configuración");
            a.setHeaderText("⚙️ Configuración");
            a.setContentText("Esta función estará disponible próximamente.");
            a.getDialogPane().setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;");
            a.showAndWait();
        });

        // Filtros
        filterAll.setOnAction(e     -> { filtroActual = null;        aplicarFiltro(); setActiveFilter(filterAll); });
        filterPlaying.setOnAction(e -> { filtroActual = "jugando";   aplicarFiltro(); setActiveFilter(filterPlaying); });
        filterPlayed.setOnAction(e  -> { filtroActual = "jugado";    aplicarFiltro(); setActiveFilter(filterPlayed); });
        filterPending.setOnAction(e -> { filtroActual = "pendiente"; aplicarFiltro(); setActiveFilter(filterPending); });

        // Búsqueda local en la biblioteca
        searchField.textProperty().addListener((o, ov, nv) -> filtrarPorTexto(nv));

        // Autocompletar catálogo global en el combo
        cbBusquedaJuego.getEditor().textProperty().addListener((o, ov, nv) -> {
            if (nv != null && nv.length() > 1) {
                cbBusquedaJuego.setItems(service.buscarEnCatalogoGlobal(nv));
                if (!cbBusquedaJuego.isShowing()) cbBusquedaJuego.show();
            }
        });

        // Añadir juego
        btnAdd.setOnAction(e -> {
            String titulo = cbBusquedaJuego.getEditor().getText().trim();
            if (titulo.isEmpty()) { mostrarAlert("⚠ Escribe o selecciona un juego."); return; }
            if (!service.existeEnCatalogo(titulo)) { mostrarAlert("❌ El juego no existe en el catálogo de STAEM."); return; }
            if (service.añadirVideojuego(usuarioId, titulo, cbEstado.getValue())) {
                cargarJuegosGrid();
                mostrarAlert("✅ Juego añadido a tu biblioteca.");
                limpiarFormulario();
            } else {
                mostrarAlert("⚠ Ya tienes este juego en tu biblioteca.");
            }
        });

        // Actualizar juego
        btnUpdate.setOnAction(e -> {
            if (juegoSeleccionado == null) { mostrarAlert("⚠ Selecciona un juego para actualizar."); return; }
            if (service.actualizarVideojuego(
                    juegoSeleccionado.getId(),
                    cbEstado.getValue(),
                    cbNota.getValue(),
                    txtResena.getText())) {
                cargarJuegosGrid();
                mostrarAlert("✅ Juego actualizado.");
            } else {
                mostrarAlert("❌ No se pudo actualizar.");
            }
        });

        // Eliminar juego
        btnDelete.setOnAction(e -> {
            if (juegoSeleccionado == null) { mostrarAlert("⚠ Selecciona un juego para eliminar."); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Eliminar juego");
            confirm.setHeaderText(null);
            confirm.setContentText("¿Eliminar \"" + juegoSeleccionado.getTitulo() + "\" de tu biblioteca?");
            confirm.getDialogPane().setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK && service.eliminarVideojuego(juegoSeleccionado.getId())) {
                    juegoSeleccionado = null;
                    selLabel.setText("Ningún juego seleccionado");
                    cargarJuegosGrid();
                    limpiarFormulario();
                    mostrarAlert("✅ Juego eliminado.");
                }
            });
        });

        // Actualizar etiqueta de selección cuando cambia juegoSeleccionado
        // (lo hacemos dentro de createGameCard)
    }

    // ─────────────────────────────────────────
    // FILTRADO
    // ─────────────────────────────────────────

    private void aplicarFiltro() {
        gamesGrid.getChildren().clear();
        int col = 0, row = 0;
        for (Videojuego j : todosLosJuegos) {
            if (filtroActual == null || j.getEstado().equalsIgnoreCase(filtroActual)) {
                gamesGrid.add(createGameCard(j), col, row);
                if (++col >= 3) { col = 0; row++; }
            }
        }
    }

    private void filtrarPorTexto(String texto) {
        gamesGrid.getChildren().clear();
        int col = 0, row = 0;
        for (Videojuego j : todosLosJuegos) {
            boolean pasaEstado = filtroActual == null || j.getEstado().equalsIgnoreCase(filtroActual);
            boolean pasaTexto  = texto == null || texto.isBlank() ||
                    j.getTitulo().toLowerCase().contains(texto.toLowerCase());
            if (pasaEstado && pasaTexto) {
                gamesGrid.add(createGameCard(j), col, row);
                if (++col >= 3) { col = 0; row++; }
            }
        }
    }

    private void setActiveFilter(Button active) {
        String estiloActivo  = "-fx-background-color: #0078d4; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 8 16;";
        String estiloInactivo = "-fx-background-color: #2a2a2a; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 13px; -fx-font-weight: 500; -fx-background-radius: 8; -fx-padding: 8 16;";
        for (Button btn : List.of(filterAll, filterPlaying, filterPlayed, filterPending))
            btn.setStyle(btn == active ? estiloActivo : estiloInactivo);
    }

    private void cargarJuegosGrid() {
        todosLosJuegos.setAll(cargarDatos(usuarioId));
        gamesCountLabel.setText(todosLosJuegos.size() + " juegos");
        aplicarFiltro();
        setActiveFilter(filtroActual == null ? filterAll :
                "jugando".equals(filtroActual)   ? filterPlaying :
                        "jugado".equals(filtroActual)    ? filterPlayed  : filterPending);
    }

    // ─────────────────────────────────────────
    // GAME CARD
    // ─────────────────────────────────────────

    private VBox createGameCard(Videojuego juego) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setPrefWidth(270);
        card.setStyle(cardStyle(false));
        card.setUserData(juego); // guardamos referencia directa

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(140);
        imageContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #2a2a2a, #1a1a1a); " +
                        "-fx-background-radius: 8;"
        );
        Label icon = new Label("🎮");
        icon.setStyle("-fx-font-size: 44px;");
        imageContainer.getChildren().add(icon);
        StackPane.setAlignment(icon, Pos.CENTER);

        Label title = new Label(juego.getTitulo());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: white;");
        title.setWrapText(true);

        String color = switch (juego.getEstado().toLowerCase()) {
            case "jugando"   -> "#107c10";
            case "jugado"    -> "#0078d4";
            default          -> "#ffb900";
        };
        Label state = new Label(juego.getEstado().toUpperCase());
        state.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; " +
                        "-fx-font-size: 10px; -fx-font-weight: 700; " +
                        "-fx-padding: 3 9; -fx-background-radius: 6;", color
        ));

        // Nota si tiene valoración
        if (juego.getValoracion() > 0) {
            Label nota = new Label("⭐ " + juego.getValoracion() + "/10");
            nota.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.6);");
            card.getChildren().addAll(imageContainer, title, state, nota);
        } else {
            card.getChildren().addAll(imageContainer, title, state);
        }

        // Hover
        card.setOnMouseEntered(e -> {
            if (juegoSeleccionado == null || juegoSeleccionado.getId() != juego.getId())
                card.setStyle(cardStyle(false) + "-fx-background-color: #252525;");
        });
        card.setOnMouseExited(e -> {
            if (juegoSeleccionado == null || juegoSeleccionado.getId() != juego.getId())
                card.setStyle(cardStyle(false));
        });

        // Clic simple → seleccionar
        card.setOnMouseClicked(e -> {
            // Deseleccionar todos
            gamesGrid.getChildren().forEach(n -> {
                if (n instanceof VBox v) v.setStyle(cardStyle(false));
            });
            // Seleccionar éste
            card.setStyle(cardStyle(true));
            juegoSeleccionado = juego;
            cbBusquedaJuego.getEditor().setText(juego.getTitulo());
            cbEstado.setValue(juego.getEstado());
            cbNota.setValue(juego.getValoracion());
            txtResena.setText(juego.getResena() != null ? juego.getResena() : "");

            // Doble clic → ficha del juego
            if (e.getClickCount() == 2) new GameProfileView().start(juego.getTitulo());
        });

        return card;
    }

    private String cardStyle(boolean selected) {
        if (selected) return
                "-fx-background-color: #2a2a2a; -fx-background-radius: 12; " +
                        "-fx-border-color: rgba(0,120,212,0.7); -fx-border-width: 2; -fx-border-radius: 12; -fx-cursor: hand;";
        return
                "-fx-background-color: #1e1e1e; -fx-background-radius: 12; " +
                        "-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1; -fx-border-radius: 12; -fx-cursor: hand;";
    }

    // ─────────────────────────────────────────
    // DIÁLOGO AÑADIR AMIGO
    // ─────────────────────────────────────────

    private void mostrarDialogoAñadirAmigo() {
        Dialog<String> d = new Dialog<>();
        d.setTitle("Añadir amigo");
        d.setHeaderText("Busca a un usuario de STAEM");

        ButtonType btnAñadir = new ButtonType("Añadir", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(btnAñadir, ButtonType.CANCEL);

        TextField tf = new TextField();
        tf.setPromptText("Nombre de usuario exacto");
        tf.setPrefWidth(280);
        tf.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: white; " +
                        "-fx-prompt-text-fill: rgba(255,255,255,0.4); -fx-background-radius: 8; " +
                        "-fx-border-color: transparent; -fx-padding: 8 12;"
        );

        VBox content = new VBox(10, new Label("Usuario:"), tf);
        content.setPadding(new Insets(16));
        d.getDialogPane().setContent(content);
        d.getDialogPane().setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;");
        d.setResultConverter(b -> b == btnAñadir ? tf.getText().trim() : null);

        tf.setOnAction(e -> d.getDialogPane().lookupButton(btnAñadir).fireEvent(
                new javafx.event.ActionEvent()));

        d.showAndWait().ifPresent(nombre -> {
            if (nombre.isEmpty()) return;
            if (service.esAmigo(usuarioId, nombre)) {
                mostrarAlert("⚠ Ya es tu amigo.");
            } else if (service.agregarAmigo(usuarioId, nombre)) {
                mostrarAlert("✅ ¡Amigo añadido!");
                lvAmigos.setItems(cargarAmigos(usuarioId));
            } else {
                mostrarAlert("❌ Usuario no encontrado o no válido.");
            }
        });
    }

    // ─────────────────────────────────────────
    // HELPERS DE UI
    // ─────────────────────────────────────────

    private Button createSidebarButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(active
                ? "-fx-background-color: rgba(0,120,212,0.15); -fx-text-fill: #0078d4; -fx-font-size: 14px; -fx-font-weight: 500; -fx-padding: 10 16; -fx-background-radius: 8; -fx-cursor: hand;"
                : "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 14px; -fx-font-weight: 500; -fx-padding: 10 16; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        return btn;
    }

    private Button createFilterButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setStyle(active
                ? "-fx-background-color: #0078d4; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 8 16;"
                : "-fx-background-color: #2a2a2a; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 13px; -fx-font-weight: 500; -fx-background-radius: 8; -fx-padding: 8 16;"
        );
        return btn;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-font-weight: 600; " +
                        "-fx-background-radius: 8; -fx-padding: 11; -fx-cursor: hand;", color
        ));
        return btn;
    }

    private Button createHeaderButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: white; " +
                        "-fx-font-weight: 500; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;"
        );
        return btn;
    }

    private Label createFormLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 12px; -fx-font-weight: 500;");
        return l;
    }

    private <T> void styleCombo(ComboBox<T> cb) {
        cb.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-border-color: transparent; -fx-padding: 6;"
        );
    }

    private void mostrarAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.getDialogPane().setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;");
        a.showAndWait();
    }

    private void limpiarFormulario() {
        cbBusquedaJuego.getEditor().clear();
        cbBusquedaJuego.setValue(null);
        cbEstado.setValue("pendiente");
        cbNota.setValue(0);
        txtResena.clear();
        juegoSeleccionado = null;
    }

    // ─────────────────────────────────────────
    // DATOS
    // ─────────────────────────────────────────

    private ObservableList<Videojuego> cargarDatos(int id) {
        ObservableList<Videojuego> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM videojuegos WHERE usuario_id = ? ORDER BY titulo ASC";
        try (Connection c = ConexionDB.conectar();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, id);
            ResultSet r = s.executeQuery();
            while (r.next()) list.add(new Videojuego(
                    r.getInt("id"), r.getInt("usuario_id"),
                    r.getString("titulo"), r.getString("estado"),
                    r.getInt("valoracion"), r.getString("resena")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private ObservableList<String> cargarAmigos(int id) {
        ObservableList<String> list = FXCollections.observableArrayList();
        String sql = "SELECT amigo_nombre FROM amigos WHERE usuario_id = ? ORDER BY amigo_nombre ASC";
        try (Connection c = ConexionDB.conectar();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, id);
            ResultSet r = s.executeQuery();
            while (r.next()) list.add(r.getString("amigo_nombre"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}