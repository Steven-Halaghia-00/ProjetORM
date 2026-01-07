package ch.hearc.ig.guideresto.persistence.jpa;

import ch.hearc.ig.guideresto.business.Restaurant;
import jakarta.persistence.EntityManager;
import java.util.List;

public class RestaurantMapper extends AbstractJpaMapper<Restaurant> {

    // Initialise le mapper avec la classe d'entité gérée
    public RestaurantMapper() {
        super(Restaurant.class);
    }

    // Fournit le nom de la NamedQuery utilisée pour récupérer tous les restaurants
    @Override
    protected String getFindAllNamedQuery() {
        return "Restaurant.findAll";
    }

    // Recherche un restaurant par nom exact via NamedQuery, et retourne null si aucun résultat
    public Restaurant findByName(EntityManager em, String name) {
        List<Restaurant> res = em.createNamedQuery("Restaurant.findByName", Restaurant.class)
                .setParameter("name", name)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }

    // Recherche les restaurants dont le nom de ville contient une sous-chaîne en utilisant un LIKE
    public List<Restaurant> findByCityNameContains(EntityManager em, String cityNamePart) {
        return em.createNamedQuery("Restaurant.findByCityName", Restaurant.class)
                .setParameter("cityName", "%" + cityNamePart + "%")
                .getResultList();
    }
}
