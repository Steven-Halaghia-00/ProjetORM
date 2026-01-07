package ch.hearc.ig.guideresto.persistence.jpa;

import ch.hearc.ig.guideresto.business.City;
import jakarta.persistence.EntityManager;

import java.util.List;

public class CityMapper extends AbstractJpaMapper<City> {

    // Initialise le mapper en indiquant la classe d'entité gérée
    public CityMapper() {
        super(City.class);
    }

    // Retourne le nom de la NamedQuery utilisée pour récupérer toutes les villes
    @Override
    protected String getFindAllNamedQuery() {
        return "City.findAll";
    }

    // Recherche une ville par code postal via une transaction gérée par JpaUtils
    public City findByZipCode(String zipCode) {
        List<City> res = JpaUtils.inTransactionResult(em ->
                em.createNamedQuery("City.findByZipCode", City.class)
                        .setParameter("zipCode", zipCode)
                        .getResultList()
        );
        return res.isEmpty() ? null : res.get(0);
    }

    // Recherche une ville par code postal et nom en réutilisant un EntityManager déjà ouvert
    // Utile lorsque la recherche s'inscrit dans une transaction plus large côté service
    public City findByZipAndName(EntityManager em, String zipCode, String cityName) {
        List<City> res = em.createNamedQuery("City.findByZipAndName", City.class)
                .setParameter("zipCode", zipCode)
                .setParameter("cityName", cityName)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }
}
