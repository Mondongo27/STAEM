package com.mycompany;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

public class PerfilView {

    public void start(int usuarioId) {
        Stage stage = new Stage();
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titulo = new Label("EDITAR PERFIL");
        titulo.setStyle("-fx-font-weight: bold;");

        TextField txtUser = new TextField();
        TextField txtEmail = new TextField();
        PasswordField txtPass = new PasswordField();
        Button btnGuardar = new Button("Guardar Cambios");

        // 1. Cargar datos actuales
        cargarDatosUsuario(usuarioId, txtUser, txtEmail);

        // 2. Acción de guardar
        btnGuardar.setOnAction(e -> {
            if (actualizarPerfil(usuarioId, txtUser.getText(), txtEmail.getText(), txtPass.getText())) {
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Perfil actualizado");
                a.show();
                stage.close();
            }
        });

        root.getChildren().addAll(titulo, new Label("Usuario:"), txtUser,
                new Label("Email:"), txtEmail,
                new Label("Nueva Contraseña:"), txtPass, btnGuardar);

        stage.setScene(new Scene(root, 300, 400));
        stage.setTitle("Mi Perfil - Staem");
        stage.show();
    }

    private void cargarDatosUsuario(int id, TextField user, TextField email) {
        String sql = "SELECT username, email FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user.setText(rs.getString("username"));
                email.setText(rs.getString("email"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private boolean actualizarPerfil(int id, String user, String email, String pass) {
        String sql = pass.isEmpty() ?
                "UPDATE usuarios SET username = ?, email = ? WHERE id = ?" :
                "UPDATE usuarios SET username = ?, email = ?, password = ? WHERE id = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            stmt.setString(2, email);
            if (pass.isEmpty()) {
                stmt.setInt(3, id);
            } else {
                stmt.setString(3, pass);
                stmt.setInt(4, id);
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}