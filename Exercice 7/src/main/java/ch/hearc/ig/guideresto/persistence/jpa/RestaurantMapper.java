package ch.hearc.ig.guideresto.persistence.jpa;

import ch.hearc.ig.guideresto.business.Restaurant;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Mapper JPA dédié à l'entité Restaurant
 *
 * Cette classe spécialise AbstractJpaMapper en fournissant la requête findAll
 * Les méthodes de recherche spécifiques utilisent les requêtes nommées définies sur l'entité Restaurant
 *
 * Les méthodes exposées ici prennent un EntityManager en paramètre afin de s'exécuter dans une transaction
 * gérée à un niveau supérieur, typiquement dans la couche de services
 */
public class RestaurantMapper extends AbstractJpaMapper<Restaurant> {

    public RestaurantMapper() {
        super(Restaurant.class);
    }

    /**
     * Nom de la requête nommée utilisée pour charger tous les restaurants
     */
    @Override
    protected String getFindAllNamedQuery() {
        return "Restaurant.findAll";
    }

    /**
     * Recherche le premier restaurant dont le nom correspond exactement au paramètre fourni
     * La requête nommée gère la casse via upper
     * Retourne null si aucun résultat n'est trouvé
     */
    public Restaurant findByName(EntityManager em, String name) {
        List<Restaurant> res = em.createNamedQuery("Restaurant.findByName", Restaurant.class)
                .setParameter("name", name)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }

    /**
     * Recherche les restaurants dont le nom de ville contient une sous-chaîne
     * Le wildcard est construit côté Java pour permettre une recherche de type contains
     */
    public List<Restaurant> findByCityNameContains(EntityManager em, String cityNamePart) {
        return em.createNamedQuery("Restaurant.findByCityName", Restaurant.class)
                .setParameter("cityName", "%" + cityNamePart + "%")
                .getResultList();
    }
}
