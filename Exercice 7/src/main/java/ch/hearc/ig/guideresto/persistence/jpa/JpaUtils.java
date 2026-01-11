package ch.hearc.ig.guideresto.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utilitaire centralisant la gestion de l'EntityManagerFactory et l'exécution de blocs transactionnels
 *
 * L'EntityManagerFactory est initialisé à la demande à partir de l'unité de persistance guideRestoJPA
 * Chaque appel transactionnel crée un EntityManager dédié, démarre une transaction, puis commit ou rollback
 *
 * Cette approche garantit
 * - Une fermeture systématique de l'EntityManager
 * - Un rollback en cas d'exception d'exécution
 */
public final class JpaUtils {

    /**
     * Factory partagée au niveau applicatif
     * Initialisée paresseusement lors du premier accès
     */
    private static EntityManagerFactory emf;

    private JpaUtils() {
    }

    /**
     * Crée un EntityManager à partir de l'EntityManagerFactory
     * Initialise la factory si nécessaire
     */
    public static EntityManager getEntityManager() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("guideRestoJPA");
        }
        return emf.createEntityManager();
    }

    /**
     * Ferme l'EntityManagerFactory
     * À appeler en fin d'application pour libérer les ressources JDBC et caches associés
     */
    public static void close() {
        if (emf != null) {
            emf.close();
            emf = null;
        }
    }

    /**
     * Exécute un traitement dans une transaction
     *
     * Le bloc reçoit un EntityManager utilisable pour effectuer des opérations JPA
     * La transaction est validée si aucune exception n'est levée, sinon elle est annulée
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
     * Exécute un traitement dans une transaction et retourne un résultat
     *
     * Le bloc reçoit un EntityManager utilisable pour effectuer des opérations JPA
     * La transaction est validée si aucune exception n'est levée, sinon elle est annulée
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
     * Raccourci utilitaire pour effectuer un find dans une transaction dédiée
     */
    public static <T> T find(Class<T> entityClass, int id) {
        return inTransactionResult(em -> em.find(entityClass, id));
    }

    /**
     * Raccourci utilitaire pour obtenir une référence lazy via getReference dans une transaction dédiée
     */
    public static <T> T getReference(Class<T> entityClass, int id) {
        return inTransactionResult(em -> em.getReference(entityClass, id));
    }

    /**
     * Raccourci utilitaire pour persister une entité dans une transaction dédiée
     */
    public static <T> T persist(T entity) {
        inTransaction(em -> em.persist(entity));
        return entity;
    }

    /**
     * Raccourci utilitaire pour merger une entité dans une transaction dédiée
     */
    public static <T> T merge(T entity) {
        return inTransactionResult(em -> em.merge(entity));
    }

    /**
     * Raccourci utilitaire pour supprimer une entité dans une transaction dédiée
     *
     * Si l'entité n'est pas managée, elle est rattachée via merge avant suppression
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
