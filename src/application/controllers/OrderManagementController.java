/*package application.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import application.DatabaseConnector;
import application.models.Order;

public class OrderManagementController implements Initializable {

    @FXML
    private TableView<Order> ordersTable;
    
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    
    @FXML
    private TableColumn<Order, String> clientColumn;
    
    @FXML
    private TableColumn<Order, String> itemsColumn;
    
    @FXML
    private TableColumn<Order, String> statusColumn;
    
    @FXML
    private TableColumn<Order, Void> actionsColumn;
    
    private int currentUserId; // This will store the ID of the currently logged-in user
    private DatabaseConnector dbConnection;
    
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        loadOrders(); // Load orders for this specific user
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DatabaseConnector();
        
        // Initialize table columns
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        clientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        itemsColumn.setCellValueFactory(new PropertyValueFactory<>("orderItems"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Configure the actions column with buttons
        setupActionsColumn();
        
        // Default to showing all orders (admin view)
        // Individual user view will be loaded when setCurrentUserId is called
        loadOrders();
    }
    
    private void setupActionsColumn() {
        Callback<TableColumn<Order, Void>, TableCell<Order, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Order, Void> call(TableColumn<Order, Void> param) {
                return new TableCell<>() {
                    private final Button detailsButton = new Button("Détails");
                    
                    {
                        detailsButton.setStyle("-fx-background-color: #8B4513; -fx-text-fill: white;");
                        detailsButton.setOnAction(event -> {
                            Order order = getTableView().getItems().get(getIndex());
                            showOrderDetails(order);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(detailsButton);
                        }
                    }
                };
            }
        };
        
        actionsColumn.setCellFactory(cellFactory);
    }
    
    public void loadOrders() {
        ObservableList<Order> ordersList = FXCollections.observableArrayList();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            
            String query;
            if (currentUserId > 0) {
                // Load orders for a specific user
                query = "SELECT o.id, o.user_id, o.total_amount, o.status, o.order_date, u.username " +
                        "FROM orders o " +
                        "JOIN users u ON o.user_id = u.id " +
                        "WHERE o.user_id = ? " +
                        "ORDER BY o.order_date DESC";
                pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, currentUserId);
            } else {
                // Load all orders (admin view)
                query = "SELECT o.id, o.user_id, o.total_amount, o.status, o.order_date, u.username " +
                        "FROM orders o " +
                        "JOIN users u ON o.user_id = u.id " +
                        "ORDER BY o.order_date DESC";
                pstmt = conn.prepareStatement(query);
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int orderId = rs.getInt("id");
                String clientName = rs.getString("username");
                String status = rs.getString("status");
                double totalAmount = rs.getDouble("total_amount");
                String orderDate = rs.getString("order_date");
                
                // Get order items for this order
                String orderItems = getOrderItems(orderId);
                
                Order order = new Order(orderId, clientName, orderItems, status, totalAmount, orderDate);
                ordersList.add(order);
            }
            
            ordersTable.setItems(ordersList);
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading orders: " + e.getMessage());
        } finally {
            // Close all resources
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private String getOrderItems(int orderId) {
        StringBuilder items = new StringBuilder();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn =  dbConnection.getConnection();
            String query = "SELECT p.name, oi.quantity " +
                          "FROM order_items oi " +
                          "JOIN products p ON oi.product_id = p.id " +
                          "WHERE oi.order_id = ?";
            
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, orderId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String productName = rs.getString("name");
                int quantity = rs.getInt("quantity");
                
                if (items.length() > 0) {
                    items.append(", ");
                }
                items.append(quantity).append("x ").append(productName);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur de chargement";
        } finally {
            // Close all resources
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return items.toString();
    }
    
    private void showOrderDetails(Order order) {
        // Implement code to show a detailed view of the order
        // This could open a new window or dialog with complete order details
        System.out.println("Showing details for order #" + order.getId());
        
        // In a real application, you would create a new stage or dialog here
        // For example:
        // try {
        //     FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OrderDetails.fxml"));
        //     Parent root = loader.load();
        //     
        //     OrderDetailsController controller = loader.getController();
        //     controller.setOrderData(order);
        //     
        //     Stage stage = new Stage();
        //     stage.setTitle("Détails de la commande #" + order.getOrderId());
        //     stage.setScene(new Scene(root));
        //     stage.show();
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }
}*/