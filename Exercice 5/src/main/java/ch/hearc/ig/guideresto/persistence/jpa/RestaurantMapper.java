package ch.hearc.ig.guideresto.persistence.jpa;

import ch.hearc.ig.guideresto.business.Restaurant;

import java.util.List;

/**
 * Mapper JPA dédié à l’entité Restaurant
 * Centralise les requêtes de recherche utilisées par la couche présentation
 */
public class RestaurantMapper extends AbstractJpaMapper<Restaurant> {

    /**
     * Initialise le mapper avec la classe d’entité gérée
     */
    public RestaurantMapper() {
        super(Restaurant.class);
    }

    /**
     * Retourne le nom de la requête nommée utilisée pour charger tous les restaurants
     * Permet à la classe parente de factoriser l’implémentation de findAll
     */
    @Override
    protected String getFindAllNamedQuery() {
        return "Restaurant.findAll";
    }

    /**
     * Recherche un restaurant par son nom exact
     * Retourne null si aucun résultat n’est trouvé
     */
    public Restaurant findByName(String name) {
        List<Restaurant> res = JpaUtils.inTransactionResult(em ->
                em.createNamedQuery("Restaurant.findByName", Restaurant.class)
                        .setParameter("name", name)
                        .getResultList()
        );
        return res.isEmpty() ? null : res.get(0);
    }

    /**
     * Recherche les restaurants dont le nom de ville contient une sous-chaîne donnée
     * La méthode délègue le filtrage à la base via une requête nommée et un paramètre LIKE
     */
    public List<Restaurant> findByCityNameContains(String cityNamePart) {
        return JpaUtils.inTransactionResult(em ->
                em.createNamedQuery("Restaurant.findByCityName", Restaurant.class)
                        .setParameter("cityName", "%" + cityNamePart + "%")
                        .getResultList()
        );
    }
}
