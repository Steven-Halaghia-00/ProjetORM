package ch.hearc.ig.guideresto.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;

/**
 * Classe utilitaire pour centraliser l’accès à JPA
 * Fournit la création d’EntityManager et une exécution transactionnelle simple
 */
public final class JpaUtils {

    // EntityManagerFactory singleton initialisée à la demande
    // Doit être partagée et réutilisée pendant toute la durée de vie de l’application
    private static EntityManagerFactory emf;

    // Constructeur privé pour empêcher l’instanciation
    private JpaUtils() {
        // Utility class
    }

    /**
     * Retourne un nouvel EntityManager à chaque appel
     * L’EntityManager est prévu pour être de courte durée et non partagé entre threads
     */
    public static EntityManager getEntityManager() {
        // Initialisation lazy de l’EntityManagerFactory
        // La création est coûteuse, donc on la fait une seule fois
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("guideRestoJPA");
        }
        return emf.createEntityManager();
    }

    /**
     * Ferme l’EntityManagerFactory
     * À appeler en fin d’application pour libérer les ressources JDBC associées
     */
    public static void close() {
        if (emf != null) {
            emf.close();
            emf = null;
        }
    }

    /**
     * Exécute un bloc de code dans une transaction
     * Gère begin/commit et rollback en cas d’exception runtime
     * Ferme systématiquement l’EntityManager en fin d’exécution
     */
    public static void inTransaction(Consumer<EntityManager> consumer) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            // Démarre la transaction applicative en mode RESOURCE_LOCAL
            tx.begin();

            // Exécute le travail métier/persistance fourni par l’appelant
            consumer.accept(em);

            // Commit de la transaction
            // Le commit déclenche un flush implicite si nécessaire
            tx.commit();
        } catch (RuntimeException ex) {
            // Rollback si la transaction est encore active afin d’éviter un état partiellement écrit
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            // Libère systématiquement l’EntityManager
            em.close();
        }
    }
}
