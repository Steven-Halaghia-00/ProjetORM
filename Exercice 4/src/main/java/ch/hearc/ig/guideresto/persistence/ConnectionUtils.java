package ch.hearc.ig.guideresto.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Classe utilitaire pour gérer une connexion JDBC unique
 * Centralise la lecture de la configuration et l’ouverture/fermeture de la connexion
 *
 * Remarque : dans une architecture ORM, cette classe n’est plus utilisée pour la persistance JPA
 * Elle peut rester présente pour compatibilité ou pour les parties encore en JDBC
 */
public class ConnectionUtils {

    // Logger de classe pour tracer les tentatives de connexion et les erreurs
    private static final Logger logger = LogManager.getLogger();

    // Connexion JDBC singleton conservée en mémoire pour réutilisation
    private static Connection connection;

    /**
     * Retourne une connexion JDBC ouverte
     * Charge la configuration depuis resources/database.properties
     * Ouvre la connexion si elle n’existe pas ou si elle est fermée
     * Désactive l’auto-commit afin de laisser la transaction contrôlée par le code appelant
     */
    public static Connection getConnection() {
        try {
            // Charge les paramètres de connexion depuis un ResourceBundle
            ResourceBundle dbProps = ResourceBundle.getBundle("database");
            String url = dbProps.getString("database.url");
            String username = dbProps.getString("database.username");
            String password = dbProps.getString("database.password");

            // Log informatif pour diagnostiquer le schéma utilisé et la chaîne JDBC
            logger.info("Trying to connect to user schema '{}' with JDBC string '{}'", username, url);

            // Ouvre la connexion uniquement si nécessaire afin d’éviter une reconnexion à chaque appel
            if (ConnectionUtils.connection == null || ConnectionUtils.connection.isClosed()) {
                Connection connection = DriverManager.getConnection(url, username, password);
                connection.setAutoCommit(false);
                ConnectionUtils.connection = connection;
            }
        } catch (SQLException ex) {
            // Trace l’exception SQL, typiquement due à un mauvais mot de passe, un accès réseau ou un driver manquant
            logger.error(ex.getMessage(), ex);
        } catch (MissingResourceException ex) {
            // Trace l’exception si le fichier database.properties est introuvable ou incomplet
            logger.error(ex.getMessage(), ex);
        }
        return ConnectionUtils.connection;
    }

    /**
     * Ferme la connexion JDBC si elle est ouverte
     * Permet de libérer proprement les ressources en fin de programme
     */
    public static void closeConnection() {
        try {
            if (ConnectionUtils.connection != null && !ConnectionUtils.connection.isClosed()) {
                ConnectionUtils.connection.close();
            }
        } catch (SQLException e) {
            // Trace les erreurs de fermeture de connexion
            logger.error(e.getMessage(), e);
        }
    }
}
