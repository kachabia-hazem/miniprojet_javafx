package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    // Configuration de la base de données
    private static final String URL = "jdbc:mysql://localhost:3306/restaurant_db";
    private static final String USER = "root";   
    private static final String PASSWORD = "";   
    
    // Singleton pour la connexion
    private static Connection connection = null;
    
    /**
     * Établit et retourne une connexion à la base de données
     * @return Connection ou null en cas d'erreur
     */
    public static synchronized Connection getConnection() {
        try {
            // Vérifier si la connexion est fermée ou invalide
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                // Fermer l'ancienne connexion si elle existe
                closeConnection();
                
                // Chargement du pilote JDBC
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Établissement d'une nouvelle connexion
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion à la base de données établie avec succès.");
            }
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur: Le pilote MySQL n'a pas été trouvé.");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la connexion à la base de données.");
            e.printStackTrace();
            
            // En cas d'erreur, s'assurer que la connexion est bien fermée
            closeConnection();
            return null;
        }
    }
    
    /**
     * Ferme la connexion à la base de données
     */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Connexion à la base de données fermée.");
                }
                connection = null;
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion.");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Alternative: création d'une nouvelle connexion à chaque appel
     * Utile pour les opérations qui doivent avoir leur propre connexion
     * @return Une nouvelle connexion à la base de données
     */
    public static Connection createNewConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection newConnection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Nouvelle connexion à la base de données créée.");
            return newConnection;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Erreur lors de la création d'une nouvelle connexion.");
            e.printStackTrace();
            return null;
        }
    }
}