package application.controllers;

import application.DatabaseConnector;
import application.Session;
import application.models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button registerButton;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Hyperlink loginLink;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les composants
        if (errorLabel != null) {
            errorLabel.setText("");
        }
        
        // Ajouter l'événement au bouton d'inscription s'il n'est pas déjà configuré dans FXML
        if (registerButton != null) {
            registerButton.setOnAction(this::handleRegister);
        }
        
        if (loginLink != null) {
            loginLink.setOnAction(this::handleLoginLink);
        }
    }
    
    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        // Validation des entrées
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champs vides", "Veuillez remplir tous les champs.");
            if (errorLabel != null) {
                errorLabel.setText("Veuillez remplir tous les champs.");
            }
            return;
        }
        
        // Vérifier si le nom d'utilisateur existe déjà
        if (usernameExists(username)) {
            showAlert(Alert.AlertType.ERROR, "Nom d'utilisateur déjà utilisé", 
                    "Ce nom d'utilisateur est déjà pris. Veuillez en choisir un autre.");
            if (errorLabel != null) {
                errorLabel.setText("Nom d'utilisateur déjà utilisé.");
            }
            return;
        }
        
        // Enregistrer le nouvel utilisateur
        if (registerUser(username, password)) {
            showAlert(Alert.AlertType.INFORMATION, "Inscription réussie", 
                    "Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter.");
            redirectToLogin(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur d'inscription", 
                    "Une erreur est survenue lors de l'inscription. Veuillez réessayer.");
            if (errorLabel != null) {
                errorLabel.setText("Erreur d'inscription. Veuillez réessayer.");
            }
        }
    }
    
    private boolean usernameExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du nom d'utilisateur:");
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean registerUser(String username, String password) {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, "client"); // Le rôle est toujours 'client' lors de l'inscription
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement de l'utilisateur:");
            e.printStackTrace();
            return false;
        }
    }
    
    @FXML
    private void handleLoginLink(ActionEvent event) {
        redirectToLogin(event);
    }
    
    private void redirectToLogin(ActionEvent event) {
        try {
            // Charger la vue de connexion
            Parent root = FXMLLoader.load(getClass().getResource("/application/views/Login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors de la redirection vers la page de connexion:");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", 
                    "Impossible de revenir à la page de connexion.");
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