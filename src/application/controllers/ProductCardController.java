package application.controllers;

import application.DatabaseConnector;
import application.models.Product;
import application.models.ShoppingCart;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProductCardController {

    @FXML private VBox cardBox;
    @FXML private ImageView productImage;
    @FXML private Label productNameLabel; // Modifié pour correspondre au FXML
    @FXML private Label productPriceLabel; // Modifié pour correspondre au FXML
    @FXML private Label productCategory;
    @FXML private Label quantityLabel;
    @FXML private Button addToCartButton; // Modifié pour correspondre au FXML
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private int quantity = 0;
    private Product currentProduct;
    private boolean adminMode = false;
    private ShoppingCart cart = ShoppingCart.getInstance();

    // Listener utilisateur
    public interface OnProductAddedListener {
        void onProductAdded();
    }
    private OnProductAddedListener onProductAddedListener;

    public void setOnProductAdded(OnProductAddedListener listener) {
        this.onProductAddedListener = listener;
    }

    @FXML
    public void initialize() {
        if (quantityLabel != null) quantityLabel.setText("0");
        if (editButton != null) editButton.setVisible(false);
        if (deleteButton != null) deleteButton.setVisible(false);
    }

    // Mode admin
    public void setAdminMode(boolean isAdmin) {
        this.adminMode = isAdmin;
        if (editButton != null) editButton.setVisible(isAdmin);
        if (deleteButton != null) deleteButton.setVisible(isAdmin);
    }

    @FXML
    public void addToCart() { // Renommé pour correspondre au FXML
        quantity++;
        if (quantityLabel != null) quantityLabel.setText(String.valueOf(quantity));

        if (currentProduct != null) {
            cart.addProduct(currentProduct);
            System.out.println("Ajouté au panier: " + currentProduct.getName() + ", Quantité: " + quantity);
            System.out.println("Total: " + cart.getTotalQuantity() + " articles, " + cart.getTotalAmount() + " €");

            if (onProductAddedListener != null) onProductAddedListener.onProductAdded();
        }
    }

    public void setData(Product product) {
        if (product == null) return;

        this.currentProduct = product;
        if (productNameLabel != null) productNameLabel.setText(product.getName()); // Modifié
        if (productPriceLabel != null) productPriceLabel.setText(String.format("%.2f €", product.getPrice())); // Modifié
        if (productCategory != null) productCategory.setText(product.getCategory());

        quantity = 0;
        if (quantityLabel != null) quantityLabel.setText("0");

        String dbImagePath = product.getImagePath();
        if (dbImagePath == null || dbImagePath.isEmpty()) return;

        String fileName = new File(dbImagePath).getName();
        String correctPath = "/resources/images/products/" + fileName;
        System.out.println("Trying to load image from: " + correctPath);

        try {
            if (productImage == null) return;
            InputStream imageStream = getClass().getResourceAsStream(correctPath);

            if (imageStream != null) {
                productImage.setImage(new Image(imageStream));
            } else {
                URL imageUrl = getClass().getResource(correctPath);
                if (imageUrl != null) {
                    productImage.setImage(new Image(imageUrl.toString()));
                } else {
                    String projectDir = System.getProperty("user.dir");
                    File imageFile = new File(projectDir, "src/" + dbImagePath);
                    if (imageFile.exists()) {
                        productImage.setImage(new Image(imageFile.toURI().toString()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Pour compatibilité
    public void setProduct(Product product) {
        setData(product);
    }

    public int getQuantity() {
        return quantity;
    }

    public Product getCurrentProduct() {
        return currentProduct;
    }

    @FXML
    public void editProduct() {
        if (currentProduct != null && adminMode) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/edit_product_view.fxml"));
                Parent editView = loader.load();
                EditProductController controller = loader.getController();
                controller.setProduct(currentProduct);

                Stage stage = new Stage();
                stage.setTitle("Modifier le produit");
                stage.setScene(new Scene(editView));
                stage.showAndWait();

                // Ici : rafraîchissement manuel possible
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void deleteProduct(javafx.event.ActionEvent event) {
        if (currentProduct != null && adminMode) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Êtes-vous sûr de vouloir supprimer ce produit ?",
                    ButtonType.YES, ButtonType.NO);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer " + currentProduct.getName());

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    if (deleteProductFromDB(currentProduct.getId())) {
                        Alert success = new Alert(Alert.AlertType.INFORMATION, "Produit supprimé avec succès.");
                        success.showAndWait();

                        Node source = (Node) event.getSource();
                        Stage stage = (Stage) source.getScene().getWindow();
                        refreshView(stage);
                    }
                }
            });
        }
    }

    private boolean deleteProductFromDB(int productId) {
        String query = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression:");
            e.printStackTrace();
            return false;
        }
    }

    private void refreshView(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/menu_view.fxml"));
            Parent menuView = loader.load();
            stage.setScene(new Scene(menuView));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}