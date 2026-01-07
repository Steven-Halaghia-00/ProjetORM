package ch.hearc.ig.guideresto.persistence.jpa;

import ch.hearc.ig.guideresto.business.RestaurantType;
import jakarta.persistence.EntityManager;
import java.util.List;

public class RestaurantTypeMapper extends AbstractJpaMapper<RestaurantType> {

    // Initialise le mapper avec la classe d'entité gérée
    public RestaurantTypeMapper() {
        super(RestaurantType.class);
    }

    // Fournit le nom de la NamedQuery utilisée pour récupérer tous les types de restaurant
    @Override
    protected String getFindAllNamedQuery() {
        return "RestaurantType.findAll";
    }

    // Recherche un type de restaurant par libellé exact via NamedQuery, et retourne null si aucun résultat
    public RestaurantType findByLabel(EntityManager em, String label) {
        List<RestaurantType> res = em.createNamedQuery("RestaurantType.findByLabel", RestaurantType.class)
                .setParameter("label", label)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }
}
