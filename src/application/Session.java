package application;

import application.models.User;

public class Session {
    private static Session instance;
    private User currentUser;

    private Session() {
        // Constructeur privé pour le singleton
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public boolean isAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    public void logout() {
        currentUser = null;
    }
}