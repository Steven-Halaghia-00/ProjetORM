package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.business.Localisation;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.Logger;

public class BootstrapService extends AbstractService {

    /**
     * Vérifie le démarrage JPA et exécute quelques opérations simples de lecture et d'insertion
     * Centralise ce bootstrap dans la couche service pour éviter de disperser la logique de persistance
     */
    public void runOrmBootstrapAndBasicCrudDemo(Logger logger) {

        // Vérifie que la création d'un EntityManager fonctionne et que JPA est correctement initialisé
        EntityManager em = null;
        try {
            em = ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager();
            logger.info("Ex1 - EntityManager initialisé. isOpen={}", em.isOpen());
        } finally {
            if (em != null && em.isOpen()) em.close();
        }

        // Regroupe les opérations de lecture et d'insertion dans une transaction gérée par AbstractService
        doInTxVoid(entityManager -> {

            // Test de lecture simple pour valider l'accès aux données City
            City existingCity = entityManager.find(City.class, 1);
            logger.info("Ex2 - City.find(1) -> {}", (existingCity != null ? existingCity.getCityName() : "null"));

            // Vérifie l'existence de la ville de démonstration pour éviter les doublons à chaque démarrage
            Number cityCount = (Number) entityManager
                    .createNativeQuery("select count(*) from VILLES where NOM_VILLE = 'ORM Demo City'")
                    .getSingleResult();

            // Insère une ville de test si elle n'existe pas déjà
            if (cityCount != null && cityCount.intValue() == 0) {
                City newCity = new City();
                newCity.setZipCode("9999");
                newCity.setCityName("ORM Demo City");
                entityManager.persist(newCity);
                logger.info("Ex2 - City persistée. id={}", newCity.getId());
            } else {
                logger.info("Ex2 - City persist non exécuté (déjà présent).");
            }

            // Test de lecture simple pour valider l'accès aux données Restaurant
            Restaurant existingRestaurant = entityManager.find(Restaurant.class, 1);
            logger.info("Ex3 - Restaurant.find(1) -> {}", (existingRestaurant != null ? existingRestaurant.getName() : "null"));

            // Vérifie l'existence du restaurant de démonstration pour éviter les doublons à chaque démarrage
            Number restaurantCount = (Number) entityManager
                    .createNativeQuery("select count(*) from RESTAURANTS where NOM = 'ORM Demo Restaurant'")
                    .getSingleResult();

            // Insère un restaurant de test si absent, en réutilisant des références existantes City et RestaurantType
            if (restaurantCount != null && restaurantCount.intValue() == 0) {

                // Sélectionne arbitrairement une ville existante afin de satisfaire la contrainte FK_VILL
                Integer anyCityId = ((Number) entityManager
                        .createNativeQuery("select numero from VILLES fetch first 1 rows only")
                        .getSingleResult()).intValue();

                // Sélectionne arbitrairement un type existant afin de satisfaire la contrainte FK_TYPE
                Integer anyTypeId = ((Number) entityManager
                        .createNativeQuery("select numero from TYPES_GASTRONOMIQUES fetch first 1 rows only")
                        .getSingleResult()).intValue();

                // Utilise getReference pour éviter un chargement complet quand seul l'identifiant est nécessaire
                City anyCity = entityManager.getReference(City.class, anyCityId);
                RestaurantType anyType = entityManager.getReference(RestaurantType.class, anyTypeId);

                // Crée un restaurant de test avec ses associations minimales
                Restaurant r = new Restaurant();
                r.setName("ORM Demo Restaurant");
                r.setDescription("Enregistrement créé pour valider les associations (Ex3).");
                r.setWebsite("https://example.invalid");
                r.setAddress(new Localisation("Rue de démonstration 1"));
                r.setCity(anyCity);
                r.setType(anyType);

                // Persiste le restaurant, l'identifiant est généré au flush/commit selon la stratégie de génération
                entityManager.persist(r);
                logger.info("Ex3 - Restaurant persisté. id={}", r.getId());
            } else {
                logger.info("Ex3 - Restaurant persist non exécuté (déjà présent).");
            }
        });
    }
}
