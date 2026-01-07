package ch.hearc.ig.guideresto.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utilitaire JPA centralisant la création des EntityManager et la gestion des transactions
 * Fournit des helpers pour exécuter du code transactionnel avec ou sans valeur de retour
 */
public final class JpaUtils {

    /**
     * Fabrique partagée d’EntityManager
     * Créée de manière lazy au premier accès
     */
    private static EntityManagerFactory emf;

    /**
     * Constructeur privé pour empêcher l’instanciation
     */
    private JpaUtils() {
    }

    /**
     * Retourne un nouvel EntityManager
     * L’EntityManagerFactory est initialisée à la demande
     */
    public static EntityManager getEntityManager() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("guideRestoJPA");
        }
        return emf.createEntityManager();
    }

    /**
     * Ferme proprement l’EntityManagerFactory
     * À appeler en fin d’application pour libérer les ressources
     */
    public static void close() {
        if (emf != null) {
            emf.close();
            emf = null;
        }
    }

    /**
     * Exécute un bloc dans une transaction avec commit automatique
     * En cas d’exception, rollback si la transaction est active puis relance de l’exception
     */
    public static void inTransaction(Consumer<EntityManager> consumer) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            consumer.accept(em);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    /**
     * Exécute un bloc dans une transaction et retourne une valeur
     * Garantit la fermeture de l’EntityManager dans tous les cas
     */
    public static <R> R inTransactionResult(Function<EntityManager, R> function) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            R result = function.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    /**
     * Helper de lecture par identifiant basé sur em.find
     * Exécute la lecture dans une transaction courte
     */
    public static <T> T find(Class<T> entityClass, int id) {
        return inTransactionResult(em -> em.find(entityClass, id));
    }

    /**
     * Helper pour obtenir une référence paresseuse via em.getReference
     * Peut lever une EntityNotFoundException si l’entité est accédée et inexistante
     */
    public static <T> T getReference(Class<T> entityClass, int id) {
        return inTransactionResult(em -> em.getReference(entityClass, id));
    }

    /**
     * Persiste une entité en ouvrant une transaction courte
     * Retourne l’entité fournie pour faciliter l’enchaînement d’appels
     */
    public static <T> T persist(T entity) {
        inTransaction(em -> em.persist(entity));
        return entity;
    }

    /**
     * Fusionne une entité détachée et retourne l’instance managée résultante
     * La valeur de retour doit être préférée à l’instance passée en paramètre
     */
    public static <T> T merge(T entity) {
        return inTransactionResult(em -> em.merge(entity));
    }

    /**
     * Supprime une entité
     * Si l’instance est détachée, elle est d’abord fusionnée pour obtenir une instance managée
     */
    public static void remove(Object entity) {
        inTransaction(em -> {
            Object managed = entity;
            if (!em.contains(entity)) {
                managed = em.merge(entity);
            }
            em.remove(managed);
        });
    }
}
