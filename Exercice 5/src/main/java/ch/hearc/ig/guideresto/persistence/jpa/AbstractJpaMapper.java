package ch.hearc.ig.guideresto.persistence.jpa;

import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Mapper JPA générique fournissant des opérations CRUD et des recherches communes
 * Centralise l'accès à l'EntityManager via des transactions gérées par JpaUtils
 *
 * @param <T> Type d'entité JPA manipulée
 */
public abstract class AbstractJpaMapper<T> {

    /**
     * Classe de l'entité afin de typer correctement les appels JPA
     */
    private final Class<T> entityClass;

    /**
     * Construit un mapper pour un type d'entité donné
     *
     * @param entityClass Classe de l'entité gérée par ce mapper
     */
    protected AbstractJpaMapper(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Retourne le type d'entité géré
     */
    protected Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * Retourne le nom de la NamedQuery utilisée pour findAll
     * Chaque mapper concret fournit sa propre requête nommée
     */
    protected abstract String getFindAllNamedQuery();

    /**
     * Recherche une entité par sa clé primaire
     */
    public T findById(int id) {
        return JpaUtils.inTransactionResult(em -> em.find(entityClass, id));
    }

    /**
     * Retourne toutes les entités en s'appuyant sur une NamedQuery
     * Permet de centraliser la requête et d'éviter les chaînes JPQL dispersées
     */
    public List<T> findAll() {
        return JpaUtils.inTransactionResult(em ->
                em.createNamedQuery(getFindAllNamedQuery(), entityClass).getResultList()
        );
    }

    /**
     * Persiste une nouvelle entité
     * Retourne l'entité pour faciliter l'enchaînement côté appelant
     */
    public T create(T entity) {
        return JpaUtils.inTransactionResult(em -> {
            em.persist(entity);
            return entity;
        });
    }

    /**
     * Met à jour une entité via merge
     * Retourne l'instance managée renvoyée par JPA
     */
    public T update(T entity) {
        return JpaUtils.inTransactionResult(em -> em.merge(entity));
    }

    /**
     * Supprime une entité si elle existe et peut être attachée au contexte de persistance
     * Retourne false si l'entité passée est null
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
     * Supprime une entité par identifiant
     * Retourne false si aucune entité ne correspond à l'id fourni
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
     * Attache une entité au contexte de persistance si nécessaire
     * Utilise merge si l'entité n'est pas déjà managée
     */
    private T attachIfNeeded(EntityManager em, T entity) {
        if (entity == null) return null;
        if (em.contains(entity)) return entity;
        return em.merge(entity);
    }
}
