package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.jpa.RestaurantTypeMapper;

import java.util.List;

public class RestaurantTypeService extends AbstractService {

    // Mapper dédié à l’accès aux données pour les types de restaurants
    private final RestaurantTypeMapper typeMapper = new RestaurantTypeMapper();

    // Retourne tous les types de restaurants en s’appuyant sur la requête du mapper
    public List<RestaurantType> findAll() {
        return doInTx(em -> typeMapper.findAll(em));
    }
}
