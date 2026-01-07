package ch.hearc.ig.guideresto.persistence.jpa;

import ch.hearc.ig.guideresto.business.City;

import java.util.List;

/**
 * Mapper JPA dédié à l’entité City
 * Centralise les requêtes de lecture spécifiques (findAll, findByZipCode)
 */
public class CityMapper extends AbstractJpaMapper<City> {

    /**
     * Initialise le mapper avec la classe d’entité gérée
     */
    public CityMapper() {
        super(City.class);
    }

    /**
     * Retourne le nom de la NamedQuery utilisée pour charger toutes les villes
     * La NamedQuery est déclarée sur l’entité City
     */
    @Override
    protected String getFindAllNamedQuery() {
        return "City.findAll";
    }

    /**
     * Recherche une ville par son code postal
     * Retourne null si aucune ville ne correspond
     */
    public City findByZipCode(String zipCode) {
        List<City> res = JpaUtils.inTransactionResult(em ->
                em.createNamedQuery("City.findByZipCode", City.class)
                        .setParameter("zipCode", zipCode)
                        .getResultList()
        );
        return res.isEmpty() ? null : res.get(0);
    }
}
