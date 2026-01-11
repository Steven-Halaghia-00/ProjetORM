package ch.hearc.ig.guideresto.persistence.jpa;

import ch.hearc.ig.guideresto.business.RestaurantType;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Mapper JPA dédié à l'entité RestaurantType
 *
 * Cette classe spécialise AbstractJpaMapper en fournissant la requête findAll
 * Elle expose une méthode de recherche spécifique basée sur le libellé
 *
 * Les méthodes prennent un EntityManager en paramètre afin d'être utilisées dans une transaction
 * gérée à un niveau supérieur, typiquement dans la couche de services
 */
public class RestaurantTypeMapper extends AbstractJpaMapper<RestaurantType> {

    public RestaurantTypeMapper() {
        super(RestaurantType.class);
    }

    /**
     * Nom de la requête nommée utilisée pour charger tous les types gastronomiques
     */
    @Override
    protected String getFindAllNamedQuery() {
        return "RestaurantType.findAll";
    }

    /**
     * Recherche le premier type gastronomique correspondant au libellé fourni
     * La requête nommée gère la casse via upper
     * Retourne null si aucun résultat n'est trouvé
     */
    public RestaurantType findByLabel(EntityManager em, String label) {
        List<RestaurantType> res = em.createNamedQuery("RestaurantType.findByLabel", RestaurantType.class)
                .setParameter("label", label)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }
}
