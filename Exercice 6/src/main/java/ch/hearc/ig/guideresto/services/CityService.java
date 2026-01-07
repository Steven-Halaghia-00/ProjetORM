package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.persistence.jpa.CityMapper;

import java.util.List;

public class CityService extends AbstractService {

    // Mapper dédié à l'accès aux entités City via JPA
    private final CityMapper cityMapper = new CityMapper();

    // Retourne toutes les villes, en exécutant la requête dans une transaction
    public List<City> findAll() {
        return doInTx(em -> cityMapper.findAll(em));
    }

    // Crée et persiste une nouvelle ville à partir des champs saisis
    public City createCity(String zipCode, String cityName) {
        return doInTx(em -> {
            City c = new City(zipCode, cityName);
            cityMapper.create(em, c);
            return c;
        });
    }

    // Retourne une référence JPA (proxy) sur une ville sans charger l'entité immédiatement
    public City getReference(int id) {
        return doInTx(em -> em.getReference(City.class, id));
    }
}
