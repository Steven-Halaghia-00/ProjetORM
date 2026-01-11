package ch.hearc.ig.guideresto.persistence.jpa;

import ch.hearc.ig.guideresto.business.City;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Mapper JPA dédié à l'entité City
 *
 * Cette classe spécialise AbstractJpaMapper en fournissant la requête findAll
 * Elle expose également des méthodes de recherche spécifiques basées sur les requêtes nommées définies sur l'entité
 *
 * Deux styles d'accès sont proposés
 * - Méthodes autonomes qui gèrent leur propre transaction via JpaUtils
 * - Méthodes prenant un EntityManager pour être utilisées dans une transaction déjà ouverte
 */
public class CityMapper extends AbstractJpaMapper<City> {

    public CityMapper() {
        super(City.class);
    }

    /**
     * Nom de la requête nommée utilisée pour charger toutes les villes
     */
    @Override
    protected String getFindAllNamedQuery() {
        return "City.findAll";
    }

    /**
     * Recherche la première ville correspondant à un code postal
     * Retourne null si aucun résultat n'est trouvé
     */
    public City findByZipCode(String zipCode) {
        List<City> res = JpaUtils.inTransactionResult(em ->
                em.createNamedQuery("City.findByZipCode", City.class)
                        .setParameter("zipCode", zipCode)
                        .getResultList()
        );
        return res.isEmpty() ? null : res.get(0);
    }

    /**
     * Recherche la première ville correspondant à un couple (code postal, nom)
     * Utilise l'EntityManager fourni afin de rester dans une transaction appelante
     * Retourne null si aucun résultat n'est trouvé
     */
    public City findByZipAndName(EntityManager em, String zipCode, String cityName) {
        List<City> res = em.createNamedQuery("City.findByZipAndName", City.class)
                .setParameter("zipCode", zipCode)
                .setParameter("cityName", cityName)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }
}
