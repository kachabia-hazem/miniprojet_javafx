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

public class EditProductController {
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField priceField;
    
    @FXML
    private TextField imagePathField;
    
    @FXML
    private ComboBox<String> categoryComboBox;
    
    @FXML
    private Button updateButton;
    
    @FXML
    private Button cancelButton;
    
    private Product currentProduct;
    
    @FXML
    public void initialize() {
        // Initialiser la ComboBox avec les catégories disponibles
        categoryComboBox.getItems().addAll("Entrée", "Plat principal", "Dessert", "Boisson");
    }
    
    public void setProduct(Product product) {
        this.currentProduct = product;
        
        // Pré-remplir les champs avec les valeurs actuelles du produit
        nameField.setText(product.getName());
        priceField.setText(String.valueOf(product.getPrice()));
        categoryComboBox.setValue(product.getCategory());
        imagePathField.setText(product.getImagePath());
    }
    
    @FXML
    public void updateProduct(ActionEvent event) {
        if (validateInputs()) {
            // Mettre à jour les valeurs du produit actuel
            currentProduct.setName(nameField.getText().trim());
            currentProduct.setPrice(Double.parseDouble(priceField.getText().trim()));
            currentProduct.setCategory(categoryComboBox.getValue());
            currentProduct.setImagePath(imagePathField.getText().trim());
            
            if (updateProductInDB()) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit mis à jour avec succès !");
                closeWindow(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du produit.");
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
    
    private boolean updateProductInDB() {
        String query = "UPDATE products SET name = ?, price = ?, category = ?, image_path = ? WHERE id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, currentProduct.getName());
            pstmt.setDouble(2, currentProduct.getPrice());
            pstmt.setString(3, currentProduct.getCategory());
            pstmt.setString(4, currentProduct.getImagePath());
            pstmt.setInt(5, currentProduct.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du produit:");
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
        // Fermer la fenêtre actuelle
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        
        // Rafraîchir la vue principale du menu
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/application/views/menu_view.fxml"));
            javafx.scene.Parent menuView = loader.load();
            javafx.scene.Scene menuScene = new javafx.scene.Scene(menuView);
            
            // Obtenir la fenêtre principale et définir la nouvelle scène
            Stage mainWindow = (Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
            mainWindow.setScene(menuScene);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}