package ch.hearc.ig.guideresto.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;
import java.util.function.Function;

public final class JpaUtils {

    // Fabrique JPA partagée, initialisée à la demande et fermée en fin d'application
    private static EntityManagerFactory emf;

    // Constructeur privé pour empêcher l'instanciation d'une classe utilitaire
    private JpaUtils() {
    }

    // Retourne un EntityManager neuf pour isoler l'unité de travail et éviter un état partagé
    public static EntityManager getEntityManager() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("guideRestoJPA");
        }
        return emf.createEntityManager();
    }

    // Ferme proprement l'EntityManagerFactory afin de libérer les ressources JPA
    public static void close() {
        if (emf != null) {
            emf.close();
            emf = null;
        }
    }

    // Exécute un bloc de travail dans une transaction et garantit la fermeture de l'EntityManager
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

    // Exécute un bloc de travail qui retourne un résultat dans une transaction, avec gestion commit/rollback
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

    // Raccourci pour lire une entité par identifiant dans une transaction
    public static <T> T find(Class<T> entityClass, int id) {
        return inTransactionResult(em -> em.find(entityClass, id));
    }

    // Raccourci pour obtenir une référence (proxy) par identifiant dans une transaction
    public static <T> T getReference(Class<T> entityClass, int id) {
        return inTransactionResult(em -> em.getReference(entityClass, id));
    }

    // Persiste une entité dans une transaction et renvoie la même instance pour chaînage
    public static <T> T persist(T entity) {
        inTransaction(em -> em.persist(entity));
        return entity;
    }

    // Met à jour une entité via merge et renvoie l'instance managée retournée par JPA
    public static <T> T merge(T entity) {
        return inTransactionResult(em -> em.merge(entity));
    }

    // Supprime une entité en s'assurant qu'elle est attachée au contexte de persistance
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
