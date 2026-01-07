package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.function.Function;

public abstract class AbstractService {

    /**
     * Exécute un bloc de travail dans une transaction et retourne un résultat
     * Ouvre et ferme un EntityManager par appel pour éviter le partage d’état entre opérations
     */
    protected <T> T doInTx(Function<EntityManager, T> work) {
        EntityManager em = JpaUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T res = work.apply(em);
            tx.commit();
            return res;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    /**
     * Variante utilitaire pour exécuter un bloc de travail transactionnel sans valeur de retour
     * Délègue à doInTx en retournant null
     */
    protected void doInTxVoid(java.util.function.Consumer<EntityManager> work) {
        doInTx(em -> { work.accept(em); return null; });
    }
}
