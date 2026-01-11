package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.persistence.jpa.CityMapper;

import java.util.List;

/**
 * Service applicatif dédié aux opérations sur les villes
 *
 * La classe encapsule la gestion transactionnelle et délègue l'accès aux données au CityMapper
 * Les méthodes exposées correspondent aux besoins de l'application en lecture et en création
 */
public class CityService extends AbstractService {

    private final CityMapper cityMapper = new CityMapper();

    /**
     * Retourne la liste de toutes les villes triées selon la requête nommée associée
     */
    public List<City> findAll() {
        return doInTx(em -> cityMapper.findAll(em));
    }

    /**
     * Crée et persiste une ville à partir d'un NPA et d'un nom
     */
    public City createCity(String zipCode, String cityName) {
        return doInTx(em -> {
            City c = new City(zipCode, cityName);
            cityMapper.create(em, c);
            return c;
        });
    }

    /**
     * Retourne une référence JPA vers une ville sans effectuer de chargement immédiat
     * Utile pour poser une relation ManyToOne via une clé connue
     */
    public City getReference(int id) {
        return doInTx(em -> em.getReference(City.class, id));
    }
}
