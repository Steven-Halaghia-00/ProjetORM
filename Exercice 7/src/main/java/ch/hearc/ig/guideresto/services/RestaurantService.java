package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.jpa.RestaurantMapper;
import jakarta.persistence.OptimisticLockException;

import java.util.List;
import java.util.Objects;

/**
 * Service applicatif dédié aux restaurants
 *
 * La classe centralise la gestion transactionnelle des opérations de lecture et d'écriture
 * Les mises à jour et suppressions sont protégées par un verrou optimiste basé sur un champ @Version
 * Une vérification applicative de version est effectuée avant modification afin de détecter les éditions sur un état obsolète
 */
public class RestaurantService extends AbstractService {

    private final RestaurantMapper restaurantMapper = new RestaurantMapper();

    /**
     * Retourne la liste des restaurants selon la requête nommée associée
     */
    public List<Restaurant> findAll() {
        return doInTx(em -> restaurantMapper.findAll(em));
    }

    /**
     * Crée un restaurant en rattachant des références vers une ville et un type existants
     *
     * Les associations sont posées via getReference afin d'éviter des chargements inutiles
     */
    public Restaurant createRestaurantWithRefs(
            String name,
            String street,
            String description,
            String website,
            int cityId,
            int typeId
    ) {
        return doInTx(em -> {
            City city = em.getReference(City.class, cityId);
            RestaurantType type = em.getReference(RestaurantType.class, typeId);

            Restaurant r = new Restaurant(null, name, description, website, street, city, type);
            em.persist(r);
            return r;
        });
    }

    /**
     * Met à jour les informations principales d'un restaurant
     *
     * La version attendue correspond à l'état affiché au moment du début de l'édition
     * Une divergence de version est interprétée comme un conflit de concurrence
     */
    public void updateRestaurantDetails(
            int restaurantId,
            int expectedVersion,
            String newName,
            String newDescription,
            String newWebsite,
            Integer typeIdOrNull
    ) {
        try {
            doInTxVoid(em -> {
                Restaurant r = em.find(Restaurant.class, restaurantId);
                if (r == null) {
                    return;
                }

                if (!Objects.equals(r.getVersion(), expectedVersion)) {
                    throw new ConcurrentModificationException(
                            "Conflit : ce restaurant a été modifié par un autre utilisateur. Recharge-le et réessaie."
                    );
                }

                r.setName(newName);
                r.setDescription(newDescription);
                r.setWebsite(newWebsite);

                if (typeIdOrNull != null) {
                    RestaurantType type = em.getReference(RestaurantType.class, typeIdOrNull);
                    r.setType(type);
                }
            });
        } catch (OptimisticLockException ex) {
            throw new ConcurrentModificationException(
                    "Conflit : mise à jour impossible car le restaurant a été modifié simultanément. Recharge-le et réessaie.",
                    ex
            );
        }
    }

    /**
     * Met à jour l'adresse d'un restaurant
     *
     * La mise à jour est protégée par vérification de version et par le mécanisme @Version au commit
     */
    public void updateRestaurantAddress(int restaurantId, int expectedVersion, String newStreet, int newCityId) {
        try {
            doInTxVoid(em -> {
                Restaurant r = em.find(Restaurant.class, restaurantId);
                if (r == null) {
                    return;
                }

                if (!Objects.equals(r.getVersion(), expectedVersion)) {
                    throw new ConcurrentModificationException(
                            "Conflit : ce restaurant a été modifié par un autre utilisateur. Recharge-le et réessaie."
                    );
                }

                r.getAddress().setStreet(newStreet);
                r.setCity(em.getReference(City.class, newCityId));
            });
        } catch (OptimisticLockException ex) {
            throw new ConcurrentModificationException(
                    "Conflit : mise à jour impossible car le restaurant a été modifié simultanément. Recharge-le et réessaie.",
                    ex
            );
        }
    }

    /**
     * Supprime un restaurant en contrôlant la version attendue
     *
     * Le contrôle évite la suppression d'un état obsolète et permet de remonter un message explicite en cas de conflit
     */
    public void deleteRestaurant(int restaurantId, int expectedVersion) {
        try {
            doInTxVoid(em -> {
                Restaurant r = em.find(Restaurant.class, restaurantId);
                if (r == null) {
                    return;
                }

                if (!Objects.equals(r.getVersion(), expectedVersion)) {
                    throw new ConcurrentModificationException(
                            "Conflit : ce restaurant a été modifié par un autre utilisateur. Recharge-le avant de le supprimer."
                    );
                }

                em.remove(r);
            });
        } catch (OptimisticLockException ex) {
            throw new ConcurrentModificationException(
                    "Conflit : suppression impossible car le restaurant a été modifié simultanément. Recharge-le et réessaie.",
                    ex
            );
        }
    }

    /**
     * Recharge un restaurant avec les associations nécessaires à l'affichage console
     *
     * Les jointures fetch préchargent la ville, le type et les évaluations
     * Les sous-graphes Grade et EvaluationCriteria sont initialisés afin d'éviter des LazyInitializationException hors transaction
     */
    public Restaurant loadRestaurantForDisplay(int restaurantId) {
        return doInTx(em -> {
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
