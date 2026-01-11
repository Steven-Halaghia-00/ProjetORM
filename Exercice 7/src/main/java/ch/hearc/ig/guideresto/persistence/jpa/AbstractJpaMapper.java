package ch.hearc.ig.guideresto.persistence.jpa;

import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Mapper JPA générique fournissant des opérations CRUD de base pour une entité donnée
 *
 * Deux styles d'utilisation sont proposés
 * - Méthodes autonomes qui ouvrent et gèrent leur propre transaction via JpaUtils
 * - Méthodes qui reçoivent un EntityManager afin d'être utilisées dans une transaction gérée à un niveau supérieur
 *
 * Les implémentations concrètes doivent fournir la requête nommée utilisée pour findAll
 */
public abstract class AbstractJpaMapper<T> {

    /**
     * Type de l'entité gérée par le mapper
     */
    private final Class<T> entityClass;

    protected AbstractJpaMapper(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * Retourne le nom de la requête nommée utilisée pour charger toutes les entités
     */
    protected abstract String getFindAllNamedQuery();

    /**
     * Recherche une entité par identifiant en gérant transaction et EntityManager
     */
    public T findById(int id) {
        return JpaUtils.inTransactionResult(em -> em.find(entityClass, id));
    }

    /**
     * Charge toutes les entités via une requête nommée en gérant transaction et EntityManager
     */
    public List<T> findAll() {
        return JpaUtils.inTransactionResult(em ->
                em.createNamedQuery(getFindAllNamedQuery(), entityClass).getResultList()
        );
    }

    /**
     * Persiste une entité en gérant transaction et EntityManager
     */
    public T create(T entity) {
        return JpaUtils.inTransactionResult(em -> {
            em.persist(entity);
            return entity;
        });
    }

    /**
     * Met à jour une entité via merge en gérant transaction et EntityManager
     */
    public T update(T entity) {
        return JpaUtils.inTransactionResult(em -> em.merge(entity));
    }

    /**
     * Supprime une entité en gérant transaction et EntityManager
     *
     * L'entité est rattachée au contexte de persistance si nécessaire afin de permettre remove
     */
    public boolean delete(T entity) {
        return JpaUtils.inTransactionResult(em -> {
            T managed = attachIfNeeded(em, entity);
            if (managed == null) return false;
            em.remove(managed);
            return true;
        });
    }

    /**
     * Supprime une entité par identifiant en gérant transaction et EntityManager
     */
    public boolean deleteById(int id) {
        return JpaUtils.inTransactionResult(em -> {
            T managed = em.find(entityClass, id);
            if (managed == null) return false;
            em.remove(managed);
            return true;
        });
    }

    /**
     * Rattache l'entité au contexte de persistance si elle n'est pas déjà managée
     * Retourne null si l'entité en entrée est null
     */
    private T attachIfNeeded(EntityManager em, T entity) {
        if (entity == null) return null;
        if (em.contains(entity)) return entity;
        return em.merge(entity);
    }

    /**
     * Recherche une entité par identifiant en utilisant l'EntityManager fourni
     * À utiliser dans le cadre d'une transaction gérée à un niveau supérieur
     */
    public T findById(EntityManager em, int id) {
        return em.find(entityClass, id);
    }

    /**
     * Charge toutes les entités via une requête nommée en utilisant l'EntityManager fourni
     * À utiliser dans le cadre d'une transaction gérée à un niveau supérieur
     */
    public List<T> findAll(EntityManager em) {
        return em.createNamedQuery(getFindAllNamedQuery(), entityClass).getResultList();
    }

    /**
     * Persiste une entité en utilisant l'EntityManager fourni
     * À utiliser dans le cadre d'une transaction gérée à un niveau supérieur
     */
    public T create(EntityManager em, T entity) {
        em.persist(entity);
        return entity;
    }

    /**
     * Met à jour une entité via merge en utilisant l'EntityManager fourni
     * À utiliser dans le cadre d'une transaction gérée à un niveau supérieur
     */
    public T update(EntityManager em, T entity) {
        return em.merge(entity);
    }

    /**
     * Supprime une entité en utilisant l'EntityManager fourni
     * À utiliser dans le cadre d'une transaction gérée à un niveau supérieur
     */
    public boolean delete(EntityManager em, T entity) {
        T managed = attachIfNeeded(em, entity);
        if (managed == null) return false;
        em.remove(managed);
        return true;
    }

    /**
     * Supprime une entité par identifiant en utilisant l'EntityManager fourni
     * À utiliser dans le cadre d'une transaction gérée à un niveau supérieur
     */
    public boolean deleteById(EntityManager em, int id) {
        T managed = em.find(entityClass, id);
        if (managed == null) return false;
        em.remove(managed);
        return true;
    }
}
