package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.function.Function;

/**
 * Classe de base pour la couche de services
 *
 * Centralise la gestion des transactions afin d'assurer un comportement cohérent
 * - Ouverture et fermeture systématique de l'EntityManager
 * - Démarrage et validation de la transaction
 * - Rollback en cas d'exception d'exécution
 *
 * Les services concrets encapsulent la logique applicative et appellent ces méthodes utilitaires
 */
public abstract class AbstractService {

    /**
     * Exécute un traitement dans une transaction et retourne un résultat
     *
     * Le bloc de travail reçoit un EntityManager utilisable pour effectuer des opérations JPA
     * La transaction est validée si aucune exception n'est levée, sinon elle est annulée
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
     * Variante utilitaire pour un traitement ne retournant pas de résultat
     */
    protected void doInTxVoid(java.util.function.Consumer<EntityManager> work) {
        doInTx(em -> {
            work.accept(em);
            return null;
        });
    }
}
