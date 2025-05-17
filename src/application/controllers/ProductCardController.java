package application.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import application.models.Product;
import application.models.ShoppingCart;

public class ProductCardController {

    @FXML
    private VBox cardBox;

    @FXML
    private ImageView productImage;

    @FXML
    private Label productName;

    @FXML
    private Label productPrice;

    @FXML
    private Label productCategory;
    
    @FXML
    private Button orderButton;
    
    @FXML
    private Label quantityLabel;
    
    private int quantity = 0;
    private Product currentProduct;
    private ShoppingCart cart = ShoppingCart.getInstance();
    
    // Interface fonctionnelle pour la notification d'ajout de produit
    public interface OnProductAddedListener {
        void onProductAdded();
    }
    
    private OnProductAddedListener onProductAddedListener;
    
    // Setter pour l'écouteur
    public void setOnProductAdded(OnProductAddedListener listener) {
        this.onProductAddedListener = listener;
    }
    
    @FXML
    public void initialize() {
        // Initialisation éventuelle
        if (quantityLabel != null) {
            quantityLabel.setText("0");
        }
    }
    
    @FXML
    public void incrementQuantity() {
        quantity++;
        quantityLabel.setText(String.valueOf(quantity));
        
        // Ajouter le produit au panier global
        if (currentProduct != null) {
            cart.addProduct(currentProduct);
            System.out.println("Ajouté au panier: " + currentProduct.getName() + ", Quantité: " + quantity);
            System.out.println("Total dans le panier: " + cart.getTotalQuantity() + " articles pour " + cart.getTotalAmount() + " €");
            
            // Notifier l'écouteur que le produit a été ajouté
            if (onProductAddedListener != null) {
                onProductAddedListener.onProductAdded();
            }
        }
    }

    public void setData(Product product) {
        if (product == null) return;
        
        this.currentProduct = product;
        
        if (productName != null) productName.setText(product.getName());
        if (productPrice != null) productPrice.setText(String.format("%.2f €", product.getPrice()));
        if (productCategory != null) productCategory.setText(product.getCategory());
        
        // Réinitialiser la quantité pour chaque nouveau produit
        quantity = 0;
        if (quantityLabel != null) quantityLabel.setText("0");

        // Récupérer le chemin d'image stocké dans la base de données
        String dbImagePath = product.getImagePath();
        if (dbImagePath == null || dbImagePath.isEmpty()) {
            System.err.println("Image path is empty for product: " + product.getName());
            return;
        }

        // Extraire seulement le nom du fichier (1.jpeg, 2.jpg, etc.)
        String fileName = new File(dbImagePath).getName();

        // Construire le chemin correct pour accéder à l'image dans les ressources
        String correctPath = "/resources/images/products/" + fileName;

        System.out.println("Trying to load image from: " + correctPath);

        try {
            if (productImage == null) {
                System.err.println("productImage is null!");
                return;
            }
            
            InputStream imageStream = getClass().getResourceAsStream(correctPath);

            if (imageStream != null) {
                productImage.setImage(new Image(imageStream));
                System.out.println("Image loaded successfully: " + fileName);
            } else {
                System.err.println("Image not found: " + correctPath);

                // Essayons de charger directement avec le chemin complet depuis la racine du projet
                URL imageUrl = getClass().getResource(correctPath);
                if (imageUrl != null) {
                    productImage.setImage(new Image(imageUrl.toString()));
                    System.out.println("Image loaded via URL: " + imageUrl);
                } else {
                    System.err.println("Image URL is null for: " + correctPath);

                    // Essai avec un chemin absolu (pour le débogage uniquement)
                    try {
                        String projectDir = System.getProperty("user.dir");
                        File imageFile = new File(projectDir, "src/" + dbImagePath);
                        if (imageFile.exists()) {
                            productImage.setImage(new Image(imageFile.toURI().toString()));
                            System.out.println("Image loaded from file system: " + imageFile.getAbsolutePath());
                        } else {
                            System.err.println("Image file doesn't exist: " + imageFile.getAbsolutePath());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour compatibilité avec MenuViewController
    public void setProduct(Product product) {
        setData(product);
    }
    
    // Getter pour récupérer la quantité actuelle
    public int getQuantity() {
        return quantity;
    }
    
    // Getter pour récupérer le produit actuel
    public Product getCurrentProduct() {
        return currentProduct;
    }
}