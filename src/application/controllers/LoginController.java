package application.controllers;

import application.DatabaseConnector;
import application.Session;
import application.models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        // Initialisation si besoin
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champs vides", "Veuillez remplir tous les champs.");
            if (errorLabel != null) {
                errorLabel.setText("Veuillez remplir tous les champs.");
            }
            return;
        }

        User user = authenticateUser(username, password);

        if (user != null) {
            Session.getInstance().setCurrentUser(user);
            redirectToDashboard(user.getRole(), event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Connexion échouée", "Nom d'utilisateur ou mot de passe incorrect.");
            if (errorLabel != null) {
                errorLabel.setText("Nom d'utilisateur ou mot de passe incorrect.");
            }
        }
    }

    private User authenticateUser(String username, String password) {
        // IMPORTANT: Vérifiez le nom de votre table (users ou user)
        String query = "SELECT * FROM users WHERE username = ? AND password = ?"; // Attention : à sécuriser
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'authentification :");
            e.printStackTrace();
        }
        return null;
    }

    private void redirectToDashboard(String role, ActionEvent event) {
        String viewPath;
        
        // Redirection selon le rôle
        if ("admin".equalsIgnoreCase(role)) {
            viewPath = "/application/views/AdminDashboard.fxml"; // Assurez-vous que ce fichier existe!
        } else {
            viewPath = "/application/views/ClientDashboard.fxml";
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.setTitle("Tableau de bord - " + role);
            window.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'affichage", 
                    "Impossible de charger la vue du tableau de bord pour " + role);
            System.err.println("Erreur lors du chargement de la vue " + viewPath + " :");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegisterLink(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/application/views/Register.fxml"));
            Stage stage = (Stage) registerLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'inscription.");
            System.err.println("Erreur lors du chargement de la vue d'inscription :");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}