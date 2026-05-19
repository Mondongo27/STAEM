package com.example.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BibliotecaView {

    private static final Logger LOGGER = Logger.getLogger(BibliotecaView.class.getName());

    // 🔹 Controles UI
    private ComboBox<String> cbBusquedaJuego;
    private ComboBox<String> cbEstado;
    private ComboBox<Integer> cbNota;
    private TextArea txtResena;
    private ListView<String> lvAmigos;
    private Label gamesCountLabel;
    private GridPane gamesGrid;

    // 🔹 Estado interno
    private int usuarioId;
    private BibliotecaService service;
    private ObservableList<Videojuego> todosLosJuegos;

    // 🔹 Filtros
    private Button filterAll, filterPlaying, filterPlayed, filterPending;

    // ✅ Mapa para vincular tarjetas ↔ objetos
    private final Map<VBox, Videojuego> cardsMap = new HashMap<>();
    private VBox selectedCard = null;

    // ✅ Timer para debounce en búsqueda
    private Timer searchTimer = new Timer();

    public void start(int usuarioId) {
        this.usuarioId = usuarioId;
        this.service = new BibliotecaService();
        this.todosLosJuegos = FXCollections.observableArrayList();

        Stage stage = new Stage();
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #121212;");

        // ==================== HEADER ====================
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0;");

        // ✅ LOGO PNG en lugar de texto
        ImageView logoView = new ImageView();
        try {
            Image logoImage = new Image(getClass().getResourceAsStream("/logo.png"));
            // ✅ Solo verificar isError(), isPlaceholder NO existe en JavaFX
            if (logoImage != null && !logoImage.isError()) {
                logoView.setImage(logoImage);
                logoView.setFitHeight(32);
                logoView.setPreserveRatio(true);
                header.getChildren().add(logoView);
            } else {
                Label fallback = new Label("STAEM");
                fallback.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: white;");
                header.getChildren().add(fallback);
            }
        } catch (Exception e) {
            Label fallback = new Label("STAEM");
            fallback.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: white;");
            header.getChildren().add(fallback);
        }

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Buscar juegos...");
        searchField.setPrefWidth(400);
        searchField.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.4); -fx-background-radius: 8; -fx-border-color: transparent; -fx-padding: 8 16;");

        searchField.setOnAction(e -> {
            String texto = searchField.getText();
            if (texto != null && texto.length() >= 3) {
                aplicarFiltroPorTexto(texto);
            }
        });

        HBox userActions = new HBox(12);
        userActions.setAlignment(Pos.CENTER_RIGHT);

        Button btnMiPerfil = new Button("👤 Mi Perfil");
        btnMiPerfil.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-weight: 500; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");

        Button btnLogOut = new Button("Salir");
        btnLogOut.setStyle("-fx-background-color: rgba(255,69,58,0.15); -fx-text-fill: #ff453a; -fx-font-weight: 500; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");

        userActions.getChildren().addAll(btnMiPerfil, btnLogOut);
        header.getChildren().addAll(new Region(), searchField, userActions);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // ==================== MAIN CONTENT ====================
        HBox mainContent = new HBox();
        mainContent.setStyle("-fx-background-color: #121212;");

        // ==================== SIDEBAR ====================
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(20, 0, 20, 20));
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 1 0 0;");

        Label menuLabel = new Label("MENÚ");
        menuLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 0 0 10 16;");

        VBox menuItems = new VBox(4);
        Button btnLibrary = createSidebarButton("📚 Biblioteca", true);
        Button btnFriends = createSidebarButton("👥 Amigos", false);
        // ✅ ELIMINADO: btnSettings ("⚙️ Configuración")
        menuItems.getChildren().addAll(btnLibrary, btnFriends);

        Region separator1 = new Region();
        separator1.setPrefHeight(20);

        Label friendsLabel = new Label("AMIGOS");
        friendsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 0 0 10 16;");

        lvAmigos = new ListView<>();
        lvAmigos.setPrefHeight(200);
        lvAmigos.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        lvAmigos.setItems(cargarAmigos(usuarioId));
        lvAmigos.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
                } else {
                    setText("👤 " + item);
                    setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 13px; -fx-padding: 10 16; -fx-background-radius: 8;");
                }
            }
        });
        lvAmigos.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                String seleccionado = lvAmigos.getSelectionModel().getSelectedItem();
                if (seleccionado != null) new PublicProfileView().start(seleccionado, usuarioId);
            }
        });
        sidebar.getChildren().addAll(menuLabel, menuItems, separator1, friendsLabel, lvAmigos);

        // ==================== CONTENT AREA ====================
        VBox contentArea = new VBox(20);
        contentArea.setPadding(new Insets(30));
        contentArea.setStyle("-fx-background-color: #121212;");
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        HBox sectionHeader = new HBox(20);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Mi Biblioteca");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: 700; -fx-text-fill: white;");
        gamesCountLabel = new Label("0 juegos");
        gamesCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.5); -fx-padding: 5 12; -fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 12;");
        sectionHeader.getChildren().addAll(titleLabel, gamesCountLabel);

        HBox filtersBar = new HBox(10);
        filtersBar.setAlignment(Pos.CENTER_LEFT);
        filterAll = createFilterButton("Todos", true);
        filterPlaying = createFilterButton("Jugando", false);
        filterPlayed = createFilterButton("Jugados", false);
        filterPending = createFilterButton("Pendientes", false);
        filtersBar.getChildren().addAll(filterAll, filterPlaying, filterPlayed, filterPending);

        gamesGrid = new GridPane();
        gamesGrid.setHgap(20);
        gamesGrid.setVgap(20);
        gamesGrid.setStyle("-fx-padding: 20 0;");
        ScrollPane scrollPane = new ScrollPane(gamesGrid);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentArea.getChildren().addAll(sectionHeader, filtersBar, scrollPane);

        // ==================== SIDE PANEL ====================
        VBox sidePanel = new VBox(20);
        sidePanel.setPadding(new Insets(30, 30, 30, 0));
        sidePanel.setPrefWidth(350);
        sidePanel.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 0 1;");

        Label panelTitle = new Label("Gestión de Juegos");
        panelTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: white;");

        cbBusquedaJuego = new ComboBox<>();
        cbBusquedaJuego.setEditable(true);
        cbBusquedaJuego.setPromptText("Buscar videojuego...");
        cbBusquedaJuego.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.4); -fx-background-radius: 8; -fx-border-color: transparent; -fx-padding: 12;");

        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("pendiente", "jugando", "jugado");
        cbEstado.setValue("pendiente");
        cbEstado.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-color: transparent; -fx-padding: 12;");

        cbNota = new ComboBox<>();
        cbNota.getItems().addAll(0,1,2,3,4,5);
        cbNota.setValue(0);
        cbNota.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-color: transparent; -fx-padding: 12;");

        txtResena = new TextArea();
        txtResena.setPromptText("Escribe tu reseña...");
        txtResena.setPrefRowCount(5);
        txtResena.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.4); -fx-background-radius: 8; -fx-border-color: transparent; -fx-padding: 12; -fx-control-inner-background: #2a2a2a;");

        Button btnAdd = createActionButton("➕ Añadir Juego", "#0078d4");
        Button btnUpdate = createActionButton("💾 Actualizar", "#107c10");
        Button btnDelete = createActionButton("🗑 Eliminar", "#d13438");

        sidePanel.getChildren().addAll(
                panelTitle,
                createFormLabel("Juego"), cbBusquedaJuego,
                createFormLabel("Estado"), cbEstado,
                createFormLabel("Valoración"), cbNota,
                createFormLabel("Reseña"), txtResena,
                btnAdd, btnUpdate, btnDelete
        );

        mainContent.getChildren().addAll(sidebar, contentArea, sidePanel);
        root.setTop(header);
        root.setCenter(mainContent);

        // ==================== SCENE ====================
        Scene scene = new Scene(root, 1600, 900);
        cargarCSS(scene);
        stage.setScene(scene);
        stage.setTitle("STAEM - Mi Biblioteca");
        cargarIcono(stage);
        stage.setMaximized(true);
        stage.show();

        // ==================== FUNCIONALIDAD ====================
        cargarJuegosGrid();

        btnLogOut.setOnAction(e -> {
            stage.close();
            try {
                new LoginApp().start(new Stage());
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error al volver al login", ex);
            }
        });

        btnMiPerfil.setOnAction(e -> {
            String miNombre = obtenerMiNombre(usuarioId);
            if (miNombre != null) new PublicProfileView().start(miNombre, usuarioId);
        });

        // ✅ "Añadir Amigo" SOLO desde el botón del sidebar
        btnFriends.setOnAction(e -> mostrarDialogoAñadirAmigo(usuarioId, lvAmigos, stage));

        // FILTROS FUNCIONALES
        filterAll.setOnAction(e -> { aplicarFiltro(null); setActiveFilter(filterAll); });
        filterPlaying.setOnAction(e -> { aplicarFiltro("jugando"); setActiveFilter(filterPlaying); });
        filterPlayed.setOnAction(e -> { aplicarFiltro("jugado"); setActiveFilter(filterPlayed); });
        filterPending.setOnAction(e -> { aplicarFiltro("pendiente"); setActiveFilter(filterPending); });

        // ✅ BOTÓN AÑADIR JUEGO
        btnAdd.setOnAction(e -> {
            String titulo = cbBusquedaJuego.getEditor().getText();
            if (titulo == null || titulo.trim().isEmpty()) {
                mostrarAlert("⚠️ Ingresa un nombre de juego", stage);
                return;
            }
            if (service.existeEnCatalogo(titulo.trim())) {
                if (service.añadirVideojuego(usuarioId, titulo.trim(), cbEstado.getValue())) {
                    cargarJuegosGrid();
                    mostrarAlert("✅ Juego añadido", stage);
                    limpiarFormulario();
                } else {
                    mostrarAlert("⚠️ Ya tienes este juego en tu biblioteca", stage);
                }
            } else {
                mostrarAlert("❌ El juego no existe en el catálogo global", stage);
            }
        });

        // ✅ BOTÓN ACTUALIZAR
        btnUpdate.setOnAction(e -> {
            Videojuego sel = getSelectedGame();
            if (sel != null) {
                if (service.actualizarVideojuego(sel.getId(), cbEstado.getValue(), cbNota.getValue(), txtResena.getText())) {
                    sel.setEstado(cbEstado.getValue());
                    sel.setValoracion(cbNota.getValue());
                    sel.setResena(txtResena.getText());
                    cargarJuegosGrid();
                    mostrarAlert("✅ Actualizado correctamente", stage);
                } else {
                    mostrarAlert("❌ Error al actualizar", stage);
                }
            } else {
                mostrarAlert("⚠️ Selecciona un juego de tu biblioteca para actualizar", stage);
            }
        });

        // ✅ BOTÓN ELIMINAR
        btnDelete.setOnAction(e -> {
            Videojuego sel = getSelectedGame();
            if (sel != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Eliminar juego");
                confirm.setHeaderText(null);
                confirm.setContentText("¿Estás seguro de eliminar \"" + sel.getTitulo() + "\" de tu biblioteca?");
                estilizarDialog(confirm.getDialogPane());
                confirm.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.OK) {
                        if (service.eliminarVideojuego(sel.getId())) {
                            cargarJuegosGrid();
                            mostrarAlert("✅ Eliminado", stage);
                            limpiarFormulario();
                        } else {
                            mostrarAlert("❌ Error al eliminar", stage);
                        }
                    }
                });
            } else {
                mostrarAlert("⚠️ Selecciona un juego para eliminar", stage);
            }
        });

        // ✅ BÚSQUEDA CON DEBOUNCE
        cbBusquedaJuego.getEditor().textProperty().addListener((o, ov, nv) -> {
            if (nv != null && nv.length() >= 3) {
                searchTimer.cancel();
                searchTimer = new Timer();
                searchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> {
                            ObservableList<String> resultados = service.buscarEnCatalogoGlobal(nv);
                            cbBusquedaJuego.setItems(resultados);
                            if (!resultados.isEmpty() && !cbBusquedaJuego.isShowing()) {
                                cbBusquedaJuego.show();
                            }
                        });
                    }
                }, 300);
            }
        });

        // ✅ Selección con Enter en ComboBox
        cbBusquedaJuego.getEditor().setOnAction(e -> {
            String texto = cbBusquedaJuego.getEditor().getText();
            if (texto != null && !texto.trim().isEmpty()) {
                Videojuego encontrado = service.buscarJuegoEnLista(todosLosJuegos, texto);
                if (encontrado != null) {
                    for (Map.Entry<VBox, Videojuego> entry : cardsMap.entrySet()) {
                        if (entry.getValue().getId() == encontrado.getId()) {
                            seleccionarTarjeta(entry.getKey(), entry.getValue());
                            break;
                        }
                    }
                }
            }
        });
    }

    // ✅ Filtro por texto en título
    private void aplicarFiltroPorTexto(String texto) {
        gamesGrid.getChildren().clear();
        int col = 0, row = 0;
        String filtro = texto.toLowerCase().trim();

        for (Videojuego juego : todosLosJuegos) {
            if (juego.getTitulo() != null && juego.getTitulo().toLowerCase().contains(filtro)) {
                VBox card = createGameCard(juego);
                gamesGrid.add(card, col, row);
                if (++col >= 3) { col = 0; row++; }
            }
        }

        if (gamesGrid.getChildren().isEmpty()) {
            Label empty = new Label("🔍 No se encontraron juegos con \"" + texto + "\"");
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 14px; -fx-padding: 20;");
            gamesGrid.add(empty, 0, 0);
        }
    }

    // ✅ Filtros con animación
    private void aplicarFiltro(String estado) {
        gamesGrid.getChildren().clear();
        cardsMap.clear();
        selectedCard = null;

        int col = 0, row = 0;
        for (Videojuego juego : todosLosJuegos) {
            if (estado == null || juego.getEstado().equalsIgnoreCase(estado)) {
                VBox card = createGameCard(juego);
                card.setOpacity(0);
                card.setTranslateY(10);
                gamesGrid.add(card, col, row);

                FadeTransition ft = new FadeTransition(Duration.millis(150), card);
                ft.setToValue(1);
                ft.play();

                if (++col >= 3) { col = 0; row++; }
            }
        }

        if (gamesGrid.getChildren().isEmpty()) {
            Label empty = new Label("🎮 No hay juegos con este filtro");
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 14px; -fx-padding: 20;");
            gamesGrid.add(empty, 0, 0);
        }
    }

    private void setActiveFilter(Button active) {
        for (Button btn : Arrays.asList(filterAll, filterPlaying, filterPlayed, filterPending)) {
            btn.setStyle(btn == active
                    ? "-fx-background-color: #0078d4; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 8 16;"
                    : "-fx-background-color: #2a2a2a; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 13px; -fx-font-weight: 500; -fx-background-radius: 8; -fx-padding: 8 16;");
        }
    }

    private void cargarJuegosGrid() {
        cardsMap.clear();
        selectedCard = null;
        todosLosJuegos.setAll(cargarDatos(usuarioId));
        gamesCountLabel.setText(todosLosJuegos.size() + " juegos");
        aplicarFiltro(null);
        setActiveFilter(filterAll);
    }

    private Videojuego getSelectedGame() {
        return (selectedCard != null) ? cardsMap.get(selectedCard) : null;
    }

    private void seleccionarTarjeta(VBox card, Videojuego juego) {
        if (selectedCard != null) {
            selectedCard.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1; -fx-border-radius: 12;");
        }
        selectedCard = card;
        card.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 12; -fx-border-color: rgba(0,120,212,0.8); -fx-border-width: 2; -fx-border-radius: 12;");

        cbBusquedaJuego.getEditor().setText(juego.getTitulo());
        cbEstado.setValue(juego.getEstado());
        cbNota.setValue(juego.getValoracion());
        txtResena.setText(juego.getResena());
    }

    private VBox createGameCard(Videojuego juego) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1; -fx-border-radius: 12; -fx-cursor: hand;");
        card.setPrefWidth(280);

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(160);
        imageContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #2a2a2a, #1a1a1a); -fx-background-radius: 8;");
        Label icon = new Label("🎮");
        icon.setStyle("-fx-font-size: 48px;");
        imageContainer.getChildren().add(icon);
        StackPane.setAlignment(icon, Pos.CENTER);

        Label title = new Label(juego.getTitulo());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: white; -fx-wrap-text: true;");

        Label state = new Label(juego.getEstado().toUpperCase());
        String color = switch (juego.getEstado().toLowerCase()) {
            case "jugando" -> "#107c10";
            case "jugado" -> "#0078d4";
            default -> "#ffb900";
        };
        state.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 4 10; -fx-background-radius: 6;", color));

        card.getChildren().addAll(imageContainer, title, state);

        card.setOnMouseEntered(e -> {
            if (card != selectedCard) {
                card.setStyle("-fx-background-color: #252525; -fx-background-radius: 12; -fx-border-color: rgba(0,120,212,0.3); -fx-border-width: 1; -fx-border-radius: 12;");
            }
        });
        card.setOnMouseExited(e -> {
            if (card != selectedCard) {
                card.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1; -fx-border-radius: 12;");
            }
        });

        card.setOnMouseClicked(e -> {
            seleccionarTarjeta(card, juego);
            if (e.getClickCount() == 2) {
                try {
                    Class.forName("com.example.app.DetalleJuegoView");
                    new DetalleJuegoView().start(juego.getTitulo(), usuarioId);
                } catch (ClassNotFoundException ex) {
                    mostrarAlert("⚠️ La vista de detalles aún no está disponible", null);
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Error al abrir detalles", ex);
                    mostrarAlert("Error al abrir detalles", null);
                }
            }
        });

        cardsMap.put(card, juego);
        return card;
    }

    private Button createSidebarButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(active
                ? "-fx-background-color: rgba(0,120,212,0.15); -fx-text-fill: #0078d4; -fx-font-size: 14px; -fx-font-weight: 500; -fx-padding: 12 16; -fx-background-radius: 8; -fx-cursor: hand;"
                : "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 14px; -fx-font-weight: 500; -fx-padding: 12 16; -fx-background-radius: 8; -fx-cursor: hand;");
        return btn;
    }

    private Button createFilterButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setStyle(active
                ? "-fx-background-color: #0078d4; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 8 16;"
                : "-fx-background-color: #2a2a2a; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 13px; -fx-font-weight: 500; -fx-background-radius: 8; -fx-padding: 8 16;");
        return btn;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 12; -fx-cursor: hand;", color));
        return btn;
    }

    private Label createFormLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px; -fx-font-weight: 500;");
        return l;
    }

    // ✅ CORRECCIÓN CLAVE: aceptar Window en lugar de Stage
    private void mostrarAlert(String msg, Window parent) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        if (parent != null) a.initOwner(parent);
        a.setHeaderText(null);
        a.setContentText(msg);
        estilizarDialog(a.getDialogPane());
        a.showAndWait();
    }

    private void mostrarAlert(String msg, Stage parent) {
        mostrarAlert(msg, (Window) parent);
    }

    private void mostrarAlert(String msg) {
        mostrarAlert(msg, (Window) null);
    }

    private void limpiarFormulario() {
        cbBusquedaJuego.getEditor().clear();
        cbEstado.setValue("pendiente");
        cbNota.setValue(0);
        txtResena.clear();
        if (selectedCard != null) {
            selectedCard.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1; -fx-border-radius: 12;");
            selectedCard = null;
        }
    }

    private String obtenerMiNombre(int id) {
        try (Connection c = ConexionDB.conectar();
             PreparedStatement s = c.prepareStatement("SELECT username FROM usuarios WHERE id=?")) {
            s.setInt(1,id);
            ResultSet r = s.executeQuery();
            if (r.next()) return r.getString("username");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error al obtener nombre", e);
        }
        return null;
    }

    private ObservableList<Videojuego> cargarDatos(int id) {
        ObservableList<Videojuego> list = FXCollections.observableArrayList();
        try (Connection c = ConexionDB.conectar();
             PreparedStatement s = c.prepareStatement("SELECT * FROM videojuegos WHERE usuario_id=?")) {
            s.setInt(1,id);
            ResultSet r = s.executeQuery();
            while (r.next()) {
                list.add(new Videojuego(
                        r.getInt("id"),
                        r.getInt("usuario_id"),
                        r.getString("titulo"),
                        r.getString("estado"),
                        r.getInt("valoracion"),
                        r.getString("resena")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar juegos", e);
        }
        return list;
    }

    private ObservableList<String> cargarAmigos(int id) {
        ObservableList<String> list = FXCollections.observableArrayList();
        try (Connection c = ConexionDB.conectar();
             PreparedStatement s = c.prepareStatement("SELECT amigo_nombre FROM amigos WHERE usuario_id=?")) {
            s.setInt(1,id);
            ResultSet r = s.executeQuery();
            while (r.next()) list.add(r.getString("amigo_nombre"));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error al cargar amigos", e);
        }
        return list;
    }

    // ✅ DIÁLOGO AÑADIR AMIGO - CORREGIDO
    private void mostrarDialogoAñadirAmigo(int uid, ListView<String> lv, Stage ownerStage) {
        Dialog<String> d = new Dialog<>();
        d.initOwner(ownerStage);
        d.setTitle("Añadir Amigo");
        d.setHeaderText("Buscar usuario por nombre exacto");

        ButtonType add = new ButtonType("Añadir", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(add, ButtonType.CANCEL);
        estilizarDialog(d.getDialogPane());

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(10);
        g.setPadding(new Insets(20, 40, 10, 20));

        TextField tf = new TextField();
        tf.setPromptText("Nombre de usuario exacto");
        tf.setPrefWidth(280);
        tf.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.4); -fx-background-radius: 8; -fx-border-color: transparent; -fx-padding: 8 12;");

        tf.setOnAction(e -> {
            Button btnOK = (Button) d.getDialogPane().lookupButton(add);
            if (btnOK != null && !tf.getText().trim().isEmpty()) {
                btnOK.fire();
            }
        });

        Label hint = new Label("💡 El nombre debe coincidir exactamente");
        hint.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px; -fx-font-style: italic;");

        g.add(new Label("Usuario:"), 0, 0);
        g.add(tf, 1, 0);
        g.add(hint, 1, 1);

        d.getDialogPane().setContent(g);
        d.setResultConverter(b -> b == add ? tf.getText() : null);

        d.showAndWait().ifPresent(u -> {
            if (u == null || u.trim().isEmpty()) return;

            String name = u.trim();
            String miNombre = obtenerMiNombre(uid);

            if (miNombre != null && name.equalsIgnoreCase(miNombre)) {
                mostrarAlert("⚠️ No puedes añadirte a ti mismo", ownerStage);
                return;
            }

            if (service.esAmigo(uid, name)) {
                mostrarAlert("⚠️ Ya es tu amigo", ownerStage);
            } else if (service.existeUsuario(name)) {
                if (service.agregarAmigo(uid, name)) {
                    mostrarAlert("✅ ¡Añadido!", ownerStage);
                    lv.setItems(cargarAmigos(uid));
                } else {
                    mostrarAlert("⚠️ No se pudo añadir. Intenta de nuevo.", ownerStage);
                }
            } else {
                mostrarAlert("❌ Usuario \"" + name + "\" no encontrado", ownerStage);
            }
        });
    }

    // ✅ Helpers para CSS y recursos
    private void cargarCSS(Scene scene) {
        if (scene == null) return;
        try {
            String css = getClass().getResource("/style.css").toExternalForm();
            if (css != null) scene.getStylesheets().add(css);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar style.css", e);
        }
    }

    private void cargarIcono(Stage stage) {
        try {
            Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
            // ✅ Solo verificar isError(), isPlaceholder NO existe
            if (icon != null && !icon.isError()) {
                stage.getIcons().add(icon);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar el icono", e);
        }
    }

    // ✅ CORREGIDO: Loop compatible con Java 8+ (sin .toList())
    private void estilizarDialog(DialogPane pane) {
        pane.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;");
        try {
            String css = getClass().getResource("/style.css").toExternalForm();
            if (css != null) pane.getStylesheets().add(css);
        } catch (Exception e) {}
        pane.getStyleClass().add("custom-alert");

        // ✅ Loop tradicional compatible con Java 8+
        for (ButtonType type : pane.getButtonTypes()) {
            Button button = (Button) pane.lookupButton(type);
            if (button != null) {
                button.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-font-weight: 600;");
            }
        }
    }
}