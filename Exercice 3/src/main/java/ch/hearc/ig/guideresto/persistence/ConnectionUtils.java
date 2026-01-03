package ch.hearc.ig.guideresto.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Centralise la création et la fermeture d’une connexion JDBC
 * Lit les paramètres de connexion depuis le fichier de configuration "database.properties"
 */
public class ConnectionUtils {

    /** Logger applicatif pour tracer les tentatives de connexion et les erreurs */
    private static final Logger logger = LogManager.getLogger();

    /**
     * Connexion JDBC partagée
     * La connexion est initialisée à la demande et conservée pour les appels suivants
     */
    private static Connection connection;

    /**
     * Retourne une connexion JDBC prête à l’emploi
     * La connexion est créée si elle n’existe pas encore ou si elle est fermée
     * L’auto-commit est désactivé afin de permettre une gestion explicite des transactions
     *
     * @return connexion JDBC initialisée ou null en cas d’échec
     */
    public static Connection getConnection() {
        try {
            // Chargement de la configuration depuis resources/database.properties
            ResourceBundle dbProps = ResourceBundle.getBundle("database");
            String url = dbProps.getString("database.url");
            String username = dbProps.getString("database.username");
            String password = dbProps.getString("database.password");

            logger.info("Trying to connect to user schema '{}' with JDBC string '{}'", username, url);

            // Création de la connexion uniquement si nécessaire
            if (ConnectionUtils.connection == null || ConnectionUtils.connection.isClosed()) {
                Connection connection = DriverManager.getConnection(url, username, password);
                connection.setAutoCommit(false);
                ConnectionUtils.connection = connection;
            }
        } catch (SQLException ex) {
            // Capture des erreurs SQL liées à la connexion ou à l’état de la connexion
            logger.error(ex.getMessage(), ex);
        } catch (MissingResourceException ex) {
            // Capture des erreurs de configuration lorsque le fichier ou les clés sont absents
            logger.error(ex.getMessage(), ex);
        }
        return ConnectionUtils.connection;
    }

    /**
     * Ferme la connexion JDBC si elle est ouverte
     * Permet de libérer proprement les ressources en fin d’application
     */
    public static void closeConnection() {
        try {
            if (ConnectionUtils.connection != null && !ConnectionUtils.connection.isClosed()) {
                ConnectionUtils.connection.close();
            }
        } catch (SQLException e) {
            // Capture des erreurs lors de la fermeture de la connexion
            logger.error(e.getMessage(), e);
        }
    }
}
