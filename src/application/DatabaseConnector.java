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
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Chargement du pilote JDBC
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Établissement de la connexion
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion à la base de données établie avec succès.");
                
            } catch (ClassNotFoundException e) {
                System.err.println("Erreur: Le pilote MySQL n'a pas été trouvé.");
                e.printStackTrace();
                return null;
            } catch (SQLException e) {
                System.err.println("Erreur SQL lors de la connexion à la base de données.");
                e.printStackTrace();
                return null;
            }
        }
        return connection;
    }
    
    /**
     * Ferme la connexion à la base de données
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Connexion à la base de données fermée.");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion.");
                e.printStackTrace();
            }
        }
    }
}