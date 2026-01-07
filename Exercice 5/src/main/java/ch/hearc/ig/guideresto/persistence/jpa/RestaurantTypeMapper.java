package ch.hearc.ig.guideresto.persistence.jpa;

import ch.hearc.ig.guideresto.business.RestaurantType;

import java.util.List;

/**
 * Mapper JPA dédié à l’entité RestaurantType
 * Centralise les opérations de lecture spécifiques aux types gastronomiques
 */
public class RestaurantTypeMapper extends AbstractJpaMapper<RestaurantType> {

    /**
     * Initialise le mapper avec la classe d’entité gérée
     */
    public RestaurantTypeMapper() {
        super(RestaurantType.class);
    }

    /**
     * Retourne le nom de la requête nommée utilisée pour charger tous les types
     * Permet à la classe parente de factoriser l’implémentation de findAll
     */
    @Override
    protected String getFindAllNamedQuery() {
        return "RestaurantType.findAll";
    }

    /**
     * Recherche un type de restaurant par son libellé exact
     * Retourne null si aucun résultat n’est trouvé
     */
    public RestaurantType findByLabel(String label) {
        List<RestaurantType> res = JpaUtils.inTransactionResult(em ->
                em.createNamedQuery("RestaurantType.findByLabel", RestaurantType.class)
                        .setParameter("label", label)
                        .getResultList()
        );
        return res.isEmpty() ? null : res.get(0);
    }
}
