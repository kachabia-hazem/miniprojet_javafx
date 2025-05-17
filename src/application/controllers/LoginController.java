package application.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;
    @FXML private Label errorLabel;

    // Connexion à la base de données
    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/restaurant_db";
        String user = "root";  // Remplacez par votre utilisateur MySQL
        String password = "";  // Remplacez par votre mot de passe MySQL
        return DriverManager.getConnection(url, user, password);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs !");
            return;
        }

        try (Connection conn = getConnection()) {
            if (authenticate(conn, username, password)) {
                redirectToClientDashboard(event);
            } else {
                showError("Identifiants incorrects !");
            }
        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données");
            e.printStackTrace();
        }
    }

    private boolean authenticate(Connection conn, String username, String password) throws SQLException {
        String query = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                return password.equals(dbPassword);  // À sécuriser plus tard
            }
        }
        return false;
    }

    private void redirectToClientDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/ClientDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Tableau de bord client");
        } catch (IOException e) {
            showError("Erreur lors du chargement du dashboard");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegisterLink(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/application/views/Register.fxml"));
            Stage stage = (Stage) registerLink.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture de l'inscription");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }
}
