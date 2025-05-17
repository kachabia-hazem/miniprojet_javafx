package application.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCart {
    private static ShoppingCart instance;
    private Map<Integer, CartItem> items;
    
    private ShoppingCart() {
        items = new HashMap<>();
    }
    
    public static ShoppingCart getInstance() {
        if (instance == null) {
            instance = new ShoppingCart();
        }
        return instance;
    }
    
    public void addProduct(Product product) {
        int productId = product.getId();
        if (items.containsKey(productId)) {
            // Si le produit existe déjà, augmentez la quantité
            items.get(productId).incrementQuantity();
        } else {
            // Sinon, ajoutez un nouvel élément
            items.put(productId, new CartItem(product, 1));
        }
    }
    
    public void updateQuantity(int productId, int quantity) {
        if (items.containsKey(productId)) {
            if (quantity <= 0) {
                items.remove(productId);
            } else {
                items.get(productId).setQuantity(quantity);
            }
        }
    }
    
    public void removeProduct(int productId) {
        items.remove(productId);
    }
    
    public void clear() {
        items.clear();
    }
    
    public List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }
    
    public int getItemCount() {
        return items.size();
    }
    
    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : items.values()) {
            total += item.getQuantity();
        }
        return total;
    }
    
    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : items.values()) {
            total += item.getTotal();
        }
        return total;
    }
}