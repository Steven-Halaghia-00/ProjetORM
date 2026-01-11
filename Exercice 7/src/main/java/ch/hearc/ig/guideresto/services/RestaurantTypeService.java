package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.jpa.RestaurantTypeMapper;

import java.util.List;

/**
 * Service applicatif dédié aux types gastronomiques
 *
 * La classe expose des opérations de lecture sur les types
 * La gestion transactionnelle est assurée par la classe parente
 */
public class RestaurantTypeService extends AbstractService {

    private final RestaurantTypeMapper typeMapper = new RestaurantTypeMapper();

    /**
     * Retourne la liste complète des types de restaurant, ordonnée selon la requête nommée associée
     */
    public List<RestaurantType> findAll() {
        return doInTx(em -> typeMapper.findAll(em));
    }
}
