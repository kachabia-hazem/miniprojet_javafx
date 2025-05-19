package application.controllers;

import application.DatabaseConnector;
import application.models.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddProductController {
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField priceField;
    
    @FXML
    private TextField imagePathField;
    
    @FXML
    private ComboBox<String> categoryComboBox;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    public void initialize() {
        // Initialiser la ComboBox avec les catégories disponibles
        categoryComboBox.getItems().addAll("Entrée", "Plat principal", "Dessert", "Boisson");
    }
    
    @FXML
    public void saveProduct(ActionEvent event) {
        if (validateInputs()) {
            Product newProduct = new Product();
            newProduct.setName(nameField.getText().trim());
            newProduct.setPrice(Double.parseDouble(priceField.getText().trim()));
            newProduct.setCategory(categoryComboBox.getValue());
            newProduct.setImagePath(imagePathField.getText().trim());
            
            if (saveProductToDB(newProduct)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté avec succès !");
                closeWindow(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du produit.");
            }
        }
    }
    
    @FXML
    public void cancel(ActionEvent event) {
        closeWindow(event);
    }
    
    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom du produit est requis.");
            return false;
        }
        
        if (priceField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le prix du produit est requis.");
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le prix doit être supérieur à 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le prix doit être un nombre valide.");
            return false;
        }
        
        if (categoryComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une catégorie.");
            return false;
        }
        
        return true;
    }
    
    private boolean saveProductToDB(Product product) {
        String query = "INSERT INTO products (name, price, category, image_path) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setString(3, product.getCategory());
            pstmt.setString(4, product.getImagePath());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du produit:");
            e.printStackTrace();
            return false;
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void closeWindow(ActionEvent event) {
        // Retourner à la vue du menu
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/application/views/menu_view.fxml"));
            javafx.scene.Parent menuView = loader.load();
            javafx.scene.Scene menuScene = new javafx.scene.Scene(menuView);
            
            // Obtenir la fenêtre actuelle et définir la nouvelle scène
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
            stage.setScene(menuScene);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}