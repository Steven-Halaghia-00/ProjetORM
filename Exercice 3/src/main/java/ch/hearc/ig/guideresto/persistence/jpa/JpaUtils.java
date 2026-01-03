package ch.hearc.ig.guideresto.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;

/**
 * Utilitaires liés à JPA pour centraliser la gestion de l'EntityManagerFactory,
 * la création d'EntityManager et l'exécution d'unités de travail transactionnelles
 */
public final class JpaUtils {

    /**
     * Factory JPA initialisée à la demande
     * Une seule instance est conservée pour l'application
     */
    private static EntityManagerFactory emf;

    /**
     * Constructeur privé pour empêcher l'instanciation
     */
    private JpaUtils() {
        // Classe utilitaire
    }

    /**
     * Crée et retourne un EntityManager
     * L'EntityManagerFactory est créée à la demande si elle n'existe pas encore
     *
     * @return un nouvel EntityManager
     */
    public static EntityManager getEntityManager() {
        if (emf == null) {
            // Création lazy de l'EntityManagerFactory associée à l'unité de persistance
            emf = Persistence.createEntityManagerFactory("guideRestoJPA");
        }
        return emf.createEntityManager();
    }

    /**
     * Ferme l'EntityManagerFactory et libère les ressources associées
     * Doit être appelée en fin d'application
     */
    public static void close() {
        if (emf != null) {
            emf.close();
            emf = null;
        }
    }

    /**
     * Exécute une unité de travail dans une transaction RESOURCE_LOCAL
     * Garantit le commit si l'exécution se déroule correctement, ou un rollback en cas d'exception
     * L'EntityManager est systématiquement fermé en fin de traitement
     *
     * @param consumer code métier à exécuter dans la transaction
     */
    public static void inTransaction(Consumer<EntityManager> consumer) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            consumer.accept(em);
            tx.commit();
        } catch (RuntimeException ex) {
            // Rollback défensif si la transaction est encore active
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            // Libération systématique des ressources associées à l'EntityManager
            em.close();
        }
    }
}
