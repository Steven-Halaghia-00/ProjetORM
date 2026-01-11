package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.business.Localisation;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.Logger;

/**
 * Service utilitaire exécutant un scénario de démarrage et de validation minimale
 *
 * Ce service vérifie le bon démarrage de JPA et réalise quelques opérations simples de lecture et d'insertion
 * Les insertions sont conditionnelles afin d'éviter la création de doublons à chaque exécution
 */
public class BootstrapService extends AbstractService {

    /**
     * Exécute une initialisation de contrôle
     * - Vérifie l'obtention d'un EntityManager
     * - Effectue des lectures simples via find
     * - Insère des enregistrements de démonstration si absents
     */
    public void runOrmBootstrapAndBasicCrudDemo(Logger logger) {
        // Vérification du bootstrap JPA via création et fermeture d'un EntityManager
        EntityManager em = null;
        try {
            em = ch.hearc.ig.guideresto.persistence.jpa.JpaUtils.getEntityManager();
            logger.info("EntityManager initialisé. isOpen={}", em.isOpen());
        } finally {
            if (em != null && em.isOpen()) em.close();
        }

        // Scénario de contrôle exécuté dans une transaction gérée par la couche service
        doInTxVoid(entityManager -> {
            // Lecture simple d'une ville
            City existingCity = entityManager.find(City.class, 1);
            logger.info("City.find(1) -> {}", (existingCity != null ? existingCity.getCityName() : "null"));

            // Insertion conditionnelle d'une ville de démonstration
            Number cityCount = (Number) entityManager
                    .createNativeQuery("select count(*) from VILLES where NOM_VILLE = 'ORM Demo City'")
                    .getSingleResult();

            if (cityCount != null && cityCount.intValue() == 0) {
                City newCity = new City();
                newCity.setZipCode("9999");
                newCity.setCityName("ORM Demo City");
                entityManager.persist(newCity);
                logger.info("City persistée. id={}", newCity.getId());
            } else {
                logger.info("City persist non exécuté (déjà présent)");
            }

            // Lecture simple d'un restaurant
            Restaurant existingRestaurant = entityManager.find(Restaurant.class, 1);
            logger.info("Restaurant.find(1) -> {}", (existingRestaurant != null ? existingRestaurant.getName() : "null"));

            // Insertion conditionnelle d'un restaurant de démonstration
            Number restaurantCount = (Number) entityManager
                    .createNativeQuery("select count(*) from RESTAURANTS where NOM = 'ORM Demo Restaurant'")
                    .getSingleResult();

            if (restaurantCount != null && restaurantCount.intValue() == 0) {
                // Récupération d'une ville et d'un type existants pour satisfaire les clés étrangères
                Integer anyCityId = ((Number) entityManager
                        .createNativeQuery("select numero from VILLES fetch first 1 rows only")
                        .getSingleResult()).intValue();

                Integer anyTypeId = ((Number) entityManager
                        .createNativeQuery("select numero from TYPES_GASTRONOMIQUES fetch first 1 rows only")
                        .getSingleResult()).intValue();

                City anyCity = entityManager.getReference(City.class, anyCityId);
                RestaurantType anyType = entityManager.getReference(RestaurantType.class, anyTypeId);

                Restaurant r = new Restaurant();
                r.setName("ORM Demo Restaurant");
                r.setDescription("Enregistrement créé pour valider les associations");
                r.setWebsite("https://example.invalid");
                r.setAddress(new Localisation("Rue de démonstration 1"));
                r.setCity(anyCity);
                r.setType(anyType);

                entityManager.persist(r);
                logger.info("Restaurant persisté. id={}", r.getId());
            } else {
                logger.info("Restaurant persist non exécuté (déjà présent)");
            }
        });
    }
}
