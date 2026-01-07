package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.jpa.RestaurantMapper;

import java.util.List;

public class RestaurantService extends AbstractService {

    // Accès aux opérations de lecture orientées "Restaurant" via un mapper dédié
    private final RestaurantMapper restaurantMapper = new RestaurantMapper();

    // Retourne la liste des restaurants avec la stratégie de chargement définie par le mapper
    public List<Restaurant> findAll() {
        return doInTx(em -> restaurantMapper.findAll(em));
    }

    // Crée un restaurant en reliant des références existantes pour la ville et le type
    public Restaurant createRestaurantWithRefs(
            String name,
            String street,
            String description,
            String website,
            int cityId,
            int typeId
    ) {
        return doInTx(em -> {
            // Récupère des références sans requête immédiate tant que les champs ne sont pas accédés
            City city = em.getReference(City.class, cityId);
            RestaurantType type = em.getReference(RestaurantType.class, typeId);

            // Construit l’entité avec un id null afin de laisser la base générer la clé primaire
            Restaurant r = new Restaurant(null, name, description, website, street, city, type);

            // Persiste le restaurant dans la transaction courante
            em.persist(r);
            return r;
        });
    }

    // Met à jour les champs simples d’un restaurant et éventuellement son type
    public void updateRestaurantDetails(int restaurantId, String newName, String newDescription, String newWebsite, Integer typeIdOrNull) {
        doInTxVoid(em -> {
            // Charge le restaurant dans le contexte de persistance
            Restaurant r = em.find(Restaurant.class, restaurantId);
            if (r == null) return;

            // Met à jour les attributs principaux
            r.setName(newName);
            r.setDescription(newDescription);
            r.setWebsite(newWebsite);

            // Met à jour le type uniquement si un identifiant a été fourni
            if (typeIdOrNull != null) {
                RestaurantType type = em.getReference(RestaurantType.class, typeIdOrNull);
                r.setType(type);
            }
        });
    }

    // Met à jour l’adresse et la ville d’un restaurant
    public void updateRestaurantAddress(int restaurantId, String newStreet, int newCityId) {
        doInTxVoid(em -> {
            // Charge le restaurant à modifier
            Restaurant r = em.find(Restaurant.class, restaurantId);
            if (r == null) return;

            // Met à jour la rue dans la valeur embarquée
            r.getAddress().setStreet(newStreet);

            // Remplace la ville par une référence vers la nouvelle ville
            r.setCity(em.getReference(City.class, newCityId));
        });
    }

    // Supprime un restaurant par son identifiant
    public void deleteRestaurant(int restaurantId) {
        doInTxVoid(em -> {
            // Charge l’entité pour pouvoir l’attacher au contexte avant suppression
            Restaurant r = em.find(Restaurant.class, restaurantId);
            if (r != null) em.remove(r);
        });
    }

    /**
     * Recharge un restaurant depuis la base et initialise toutes les associations nécessaires à l'affichage
     */
    public Restaurant loadRestaurantForDisplay(int restaurantId) {
        return doInTx(em -> {
            // Charge le restaurant et ses associations directes pour éviter une initialisation lazy hors transaction
            Restaurant r = em.createQuery(
                            "select distinct r " +
                                    "from Restaurant r " +
                                    "join fetch r.city " +
                                    "join fetch r.restaurantType " +
                                    "left join fetch r.basicEvaluations " +
                                    "left join fetch r.completeEvaluations " +
                                    "where r.id = :id",
                            Restaurant.class
                    )
                    .setParameter("id", restaurantId)
                    .getSingleResult();

            // Initialise les sous-graphes nécessaires à l’affichage des évaluations complètes
            for (Evaluation e : r.getEvaluations()) {
                if (e instanceof CompleteEvaluation ce) {
                    ce.getGrades().size();
                    for (Grade g : ce.getGrades()) {
                        if (g.getCriteria() != null) {
                            g.getCriteria().getName();
                        }
                    }
                }
            }
            return r;
        });
    }
}
