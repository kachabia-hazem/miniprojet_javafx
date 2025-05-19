package application.controllers;

import application.DatabaseConnector;
import application.models.Product;
import application.models.ShoppingCart;
import application.models.User;
import application.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuViewController {
    @FXML
    private GridPane productsGrid;
    
    @FXML
    private Label cartCountLabel;
    
    @FXML
    private Button viewCartButton;
    
    @FXML
    private Button addProductButton;  // Nouveau bouton pour ajouter un produit
    
    private ShoppingCart cart = ShoppingCart.getInstance();
    private boolean isAdmin = false;
    
    @FXML
    public void initialize() {
        // Vérifier si l'utilisateur connecté est un admin
        checkAdminStatus();
        
        // Charger les produits
        loadProducts();
        
        // Initialiser le compteur du panier
        if (cartCountLabel != null) {
            updateCartCount();
        }
        
        // Vérifier la visibilité du bouton d'ajout de produit en fonction du rôle
        if (addProductButton != null) {
            addProductButton.setVisible(isAdmin);
        }
    }
    
    private void checkAdminStatus() {
        // Obtenir l'utilisateur de la session
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            isAdmin = "admin".equalsIgnoreCase(currentUser.getRole());
        }
    }
    
    private void updateCartCount() {
        cartCountLabel.setText(String.valueOf(cart.getTotalQuantity()));
    }
    
    @FXML
    public void viewCart(ActionEvent event) {
        try {
            // Charger la vue du panier
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/cart_view.fxml"));
            Parent cartView = loader.load();
            Scene cartScene = new Scene(cartView);
            
            // Obtenir la fenêtre actuelle et définir la nouvelle scène
            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(cartScene);
            window.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de cart_view.fxml:");
            e.printStackTrace();
        }
    }
    
    @FXML
    public void addNewProduct(ActionEvent event) {
        try {
            // Charger la vue d'ajout de produit
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/add_product_view.fxml"));
            Parent addProductView = loader.load();
            Scene addProductScene = new Scene(addProductView);
            
            // Obtenir la fenêtre actuelle et définir la nouvelle scène
            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(addProductScene);
            window.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de add_product_view.fxml:");
            e.printStackTrace();
        }
    }
    
    @FXML
    public void returnToDashboard(ActionEvent event) {
        try {
            // Charger la vue du tableau de bord client
            Parent dashboardView = FXMLLoader.load(getClass().getResource("/application/views/ClientDashboard.fxml"));
            Scene dashboardScene = new Scene(dashboardView);
            
            // Obtenir la fenêtre actuelle et définir la nouvelle scène
            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(dashboardScene);
            window.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de ClientDashboard.fxml:");
            e.printStackTrace();
        }
    }
    
    private void loadProducts() {
        List<Product> products = fetchProductsFromDB();
        int col = 0;
        int row = 1;
        for (Product product : products) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/product_card.fxml"));
                Pane productCard = loader.load();
                
                // Récupérer le contrôleur de la carte
                ProductCardController controller = loader.getController();
                controller.setProduct(product);
                controller.setAdminMode(isAdmin);  // Indiquer au contrôleur si l'utilisateur est admin
                
                // Ajouter un listener pour mettre à jour le compteur du panier
                controller.setOnProductAdded(() -> {
                    updateCartCount();
                });
                
                productsGrid.add(productCard, col, row);
                col++;
                if (col == 3) {
                    col = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private List<Product> fetchProductsFromDB() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                product.setCategory(rs.getString("category"));
                product.setImagePath(rs.getString("image_path"));
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de base de données:");
            e.printStackTrace();
        }
        return products;
    }
}