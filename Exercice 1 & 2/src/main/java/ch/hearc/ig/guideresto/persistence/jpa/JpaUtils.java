package ch.hearc.ig.guideresto.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;

public final class JpaUtils {

    /*
     * CHANGEMENT : suppression du EntityManager statique.
     * Raison : un EntityManager est prévu pour être court-lived (par unité de travail/transaction).
     * Le conserver en statique peut introduire des effets de bord (cache 1er niveau, état partagé, etc.).
     */
    private static EntityManagerFactory emf;

    /*
     * CHANGEMENT : constructeur privé pour indiquer qu'il s'agit d'une classe utilitaire.
     */
    private JpaUtils() {
        // Utility class
    }

    /*
     * CHANGEMENT : getEntityManager() retourne un nouvel EntityManager à chaque appel.
     * Raison : évite de partager un EntityManager entre plusieurs opérations/transactions.
     */
    public static EntityManager getEntityManager() {
        if (emf == null) {
            // CHANGEMENT : création lazy de l'EntityManagerFactory (singleton)
            emf = Persistence.createEntityManagerFactory("guideRestoJPA");
        }
        return emf.createEntityManager();
    }

    /*
     * CHANGEMENT : ajout d'une méthode close() pour fermer l'EntityManagerFactory proprement.
     * Raison : fermeture propre des ressources en fin d'application.
     */
    public static void close() {
        if (emf != null) {
            emf.close();
            emf = null;
        }
    }

    /*
     * CHANGEMENT : l'EntityManager est créé dans la méthode puis fermé dans le finally.
     * Raison : garantit la libération des ressources même en cas d'exception.
     *
     * CHANGEMENT : suppression du em.flush() explicite.
     * Raison : le commit déclenche déjà un flush si nécessaire évite du code en trop.
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
}
