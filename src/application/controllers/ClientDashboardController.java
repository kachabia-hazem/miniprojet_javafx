package application.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class ClientDashboardController {

    @FXML
    private Button menuButton; // fx:id doit correspondre à votre FXML

    @FXML
    private Button ordersButton; // fx:id doit correspondre à votre FXML

    // Méthode pour le bouton "Voir le Menu"
    @FXML
    private void handleMenuButton() {
        loadView("/application/views/MenuView.fxml", "Menu du Restaurant");
    }

    // Méthode pour le bouton "Voir mes Commandes"
    @FXML
    private void handleOrdersButton() {
        loadView("/application/views/OrderManagement.fxml", "Mes Commandes");
    }

    // Méthode générique pour charger les vues
    private void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) menuButton.getScene().getWindow(); // Utilise n'importe quel bouton comme référence
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue : " + fxmlPath);
        }
    }
}