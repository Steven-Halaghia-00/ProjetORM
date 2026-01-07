package ch.hearc.ig.guideresto.persistence.jpa;

import jakarta.persistence.EntityManager;

import java.util.List;

public abstract class AbstractJpaMapper<T> {

    // Type d’entité géré par ce mapper, utilisé pour typer les requêtes et les accès JPA
    private final Class<T> entityClass;

    // Construit un mapper en indiquant explicitement la classe de l’entité ciblée
    protected AbstractJpaMapper(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // Expose la classe de l’entité pour les sous-classes ou besoins internes
    protected Class<T> getEntityClass() {
        return entityClass;
    }

    // Fournit le nom de la NamedQuery utilisée par findAll
    protected abstract String getFindAllNamedQuery();

    // Recherche une entité par identifiant en ouvrant et fermant une transaction via JpaUtils
    public T findById(int id) {
        return JpaUtils.inTransactionResult(em -> em.find(entityClass, id));
    }

    // Retourne toutes les entités via une NamedQuery en ouvrant et fermant une transaction via JpaUtils
    public List<T> findAll() {
        return JpaUtils.inTransactionResult(em ->
                em.createNamedQuery(getFindAllNamedQuery(), entityClass).getResultList()
        );
    }

    // Persiste une nouvelle entité en gérant la transaction via JpaUtils
    public T create(T entity) {
        return JpaUtils.inTransactionResult(em -> {
            em.persist(entity);
            return entity;
        });
    }

    // Met à jour une entité en la fusionnant dans le contexte de persistance via JpaUtils
    public T update(T entity) {
        return JpaUtils.inTransactionResult(em -> em.merge(entity));
    }

    // Supprime une entité en s’assurant qu’elle est attachée au contexte avant suppression
    public boolean delete(T entity) {
        return JpaUtils.inTransactionResult(em -> {
            T managed = attachIfNeeded(em, entity);
            if (managed == null) return false;
            em.remove(managed);
            return true;
        });
    }

    // Supprime une entité par identifiant en vérifiant son existence avant suppression
    public boolean deleteById(int id) {
        return JpaUtils.inTransactionResult(em -> {
            T managed = em.find(entityClass, id);
            if (managed == null) return false;
            em.remove(managed);
            return true;
        });
    }

    // Attache l’entité au contexte si nécessaire afin de pouvoir la supprimer en toute sécurité
    private T attachIfNeeded(EntityManager em, T entity) {
        if (entity == null) return null;
        if (em.contains(entity)) return entity;
        return em.merge(entity);
    }

    // Variante qui réutilise un EntityManager fourni, utile lorsque la transaction est gérée ailleurs
    public T findById(EntityManager em, int id) {
        return em.find(entityClass, id);
    }

    // Variante qui réutilise un EntityManager fourni, évite d’ouvrir une transaction pour une séquence d’opérations
    public List<T> findAll(EntityManager em) {
        return em.createNamedQuery(getFindAllNamedQuery(), entityClass).getResultList();
    }

    // Variante de création en réutilisant un EntityManager fourni
    public T create(EntityManager em, T entity) {
        em.persist(entity);
        return entity;
    }

    // Variante de mise à jour en réutilisant un EntityManager fourni
    public T update(EntityManager em, T entity) {
        return em.merge(entity);
    }

    // Variante de suppression en réutilisant un EntityManager fourni et en attachant l’entité si besoin
    public boolean delete(EntityManager em, T entity) {
        T managed = attachIfNeeded(em, entity);
        if (managed == null) return false;
        em.remove(managed);
        return true;
    }

    // Variante de suppression par identifiant en réutilisant un EntityManager fourni
    public boolean deleteById(EntityManager em, int id) {
        T managed = em.find(entityClass, id);
        if (managed == null) return false;
        em.remove(managed);
        return true;
    }
}
