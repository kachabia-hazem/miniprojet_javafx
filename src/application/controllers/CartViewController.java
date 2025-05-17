package application.controllers;

import application.DatabaseConnector;
import application.models.CartItem;
import application.models.ShoppingCart;
import application.models.Order;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Optional;

public class CartViewController {

    @FXML
    private TableView<CartItem> cartTable;
    
    @FXML
    private TableColumn<CartItem, String> productNameColumn;
    
    @FXML
    private TableColumn<CartItem, Double> productPriceColumn;
    
    @FXML
    private TableColumn<CartItem, Integer> quantityColumn;
    
    @FXML
    private TableColumn<CartItem, Double> totalColumn;
    
    @FXML
    private TableColumn<CartItem, Void> actionColumn;
    
    @FXML
    private Label totalItemsLabel;
    
    @FXML
    private Label totalAmountLabel;
    
    private ShoppingCart cart = ShoppingCart.getInstance();
    
    @FXML
    public void initialize() {
        // Configuration des colonnes de la TableView
        productNameColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getProduct().getName()));
            
        productPriceColumn.setCellValueFactory(data -> 
            new SimpleDoubleProperty(data.getValue().getProduct().getPrice()).asObject());
            
        quantityColumn.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
            
        totalColumn.setCellValueFactory(data -> 
            new SimpleDoubleProperty(data.getValue().getTotal()).asObject());
        
        // Configuration de la colonne d'action avec un bouton de suppression
        setupActionColumn();
        
        // Chargement des données du panier
        updateCartDisplay();
    }
    
    private void setupActionColumn() {
        Callback<TableColumn<CartItem, Void>, TableCell<CartItem, Void>> cellFactory = 
            new Callback<TableColumn<CartItem, Void>, TableCell<CartItem, Void>>() {
                @Override
                public TableCell<CartItem, Void> call(final TableColumn<CartItem, Void> param) {
                    final TableCell<CartItem, Void> cell = new TableCell<CartItem, Void>() {
                        private final Button btn = new Button("Supprimer");
                        {
                            btn.setOnAction((ActionEvent event) -> {
                                CartItem item = getTableView().getItems().get(getIndex());
                                cart.removeProduct(item.getProduct().getId());
                                updateCartDisplay();
                            });
                            btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                        }

                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                setGraphic(btn);
                            }
                        }
                    };
                    return cell;
                }
            };
        
        actionColumn.setCellFactory(cellFactory);
    }
    
    private void updateCartDisplay() {
        List<CartItem> items = cart.getItems();
        ObservableList<CartItem> cartItems = FXCollections.observableArrayList(items);
        
        cartTable.setItems(cartItems);
        
        totalItemsLabel.setText(String.valueOf(cart.getTotalQuantity()));
        totalAmountLabel.setText(String.format("%.2f €", cart.getTotalAmount()));
    }
    
    @FXML
    public void clearCart() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Vider le panier");
        confirmAlert.setHeaderText("Êtes-vous sûr de vouloir vider votre panier ?");
        confirmAlert.setContentText("Cette action ne peut pas être annulée.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cart.clear();
            updateCartDisplay();
        }
    }
    
    @FXML
    public void continueShopping(ActionEvent event) {
        try {
            Parent menuView = FXMLLoader.load(getClass().getResource("/application/views/menu_view.fxml"));
            Scene menuScene = new Scene(menuView);
            
            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(menuScene);
            window.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue du menu", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void placeOrder(ActionEvent event) {
        // Vérifier si le panier n'est pas vide
        if (cart.getItemCount() == 0) {
            showAlert(Alert.AlertType.WARNING, "Panier vide", "Votre panier est vide", 
                    "Veuillez ajouter des produits à votre panier avant de passer commande.");
            return;
        }
        
        try {
            // Obtenir l'ID de l'utilisateur connecté
            int userId = getCurrentUserId();
            
            // Créer la commande dans la base de données
            boolean success = createOrder(userId, cart);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Commande passée", 
                        "Votre commande a été enregistrée avec succès", 
                        "Merci pour votre commande!");
                
                // Vider le panier après une commande réussie
                cart.clear();
                updateCartDisplay();
                
                // Rediriger vers la page d'accueil ou une autre page appropriée
                try {
                    // Essayer d'abord la vue ClientDashboard
                    Parent dashboardView = FXMLLoader.load(getClass().getResource("/application/views/ClientDashboard.fxml"));
                    Scene dashboardScene = new Scene(dashboardView);
                    
                    Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
                    window.setScene(dashboardScene);
                    window.show();
                } catch (IOException e) {
                    // Si ClientDashboard n'existe pas, rediriger vers menu_view
                    Parent menuView = FXMLLoader.load(getClass().getResource("/application/views/menu_view.fxml"));
                    Scene menuScene = new Scene(menuView);
                    
                    Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
                    window.setScene(menuScene);
                    window.show();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Une erreur s'est produite lors de l'enregistrement de votre commande", 
                        "Veuillez réessayer plus tard.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du traitement de la commande", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int getCurrentUserId() {
        // Implémentation adaptée pour obtenir l'utilisateur actuel
        // Si vous avez un système d'authentification, récupérez l'ID de l'utilisateur ici
        
        // Pour le développement, utiliser un ID fixe
        return 1; // À remplacer par votre logique d'authentification
    }
    
    private boolean createOrder(int userId, ShoppingCart cart) {
        Connection conn = null;
        PreparedStatement orderStmt = null;
        PreparedStatement itemStmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            // Vérifier la connexion
            if (conn == null) {
                System.err.println("Erreur: Impossible de se connecter à la base de données");
                return false;
            }
            
            conn.setAutoCommit(false); // Démarrer une transaction
            
            // Insérer l'ordre principal
            String orderSql = "INSERT INTO orders (user_id, total_amount, status, order_date) VALUES (?, ?, ?, ?)";
            orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, userId);
            orderStmt.setDouble(2, cart.getTotalAmount());
            orderStmt.setString(3, "En attente"); // Statut initial
            orderStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            
            int affectedRows = orderStmt.executeUpdate();
            
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }
            
            generatedKeys = orderStmt.getGeneratedKeys();
            int orderId;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            } else {
                conn.rollback();
                return false;
            }
            
            // Insérer les articles de la commande
            String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
            itemStmt = conn.prepareStatement(itemSql);
            
            List<CartItem> items = cart.getItems();
            for (CartItem item : items) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.getProduct().getId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setDouble(4, item.getProduct().getPrice());
                itemStmt.addBatch();
            }
            
            int[] itemResults = itemStmt.executeBatch();
            
            // Vérifier si tous les éléments ont été insérés
            boolean allInserted = true;
            for (int result : itemResults) {
                if (result <= 0) {
                    allInserted = false;
                    break;
                }
            }
            
            if (allInserted) {
                conn.commit();
                // Créer et sauvegarder un objet Order pour référence future si nécessaire
                Order order = new Order(userId, cart.getTotalAmount(), "En attente");
                order.setId(orderId);
                
                return true;
            } else {
                conn.rollback();
                return false;
            }
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (itemStmt != null) itemStmt.close();
                if (orderStmt != null) orderStmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}