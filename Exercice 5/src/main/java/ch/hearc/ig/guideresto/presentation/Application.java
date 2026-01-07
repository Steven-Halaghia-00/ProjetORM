package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import ch.hearc.ig.guideresto.persistence.jpa.RestaurantTypeMapper;
import ch.hearc.ig.guideresto.persistence.jpa.RestaurantMapper;
import ch.hearc.ig.guideresto.persistence.jpa.CityMapper;
import jakarta.persistence.EntityManager;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Point d’entrée console de l’application GuideResto
 * Centralise l’interface CLI et orchestre les appels à la couche de persistance via des mappers JPA
 */
public class Application {

    /**
     * Accès persistance pour les restaurants
     * Encapsule les requêtes JPQL et opérations de recherche spécifiques
     */
    private static final RestaurantMapper restaurantMapper = new RestaurantMapper();

    /**
     * Accès persistance pour les villes
     * Sert au chargement de listes et à la création de nouvelles villes
     */
    private static final CityMapper cityMapper = new CityMapper();

    /**
     * Accès persistance pour les types de restaurant
     * Sert au chargement de listes et à la sélection de type côté interface
     */
    private static final RestaurantTypeMapper restaurantTypeMapper = new RestaurantTypeMapper();

    /**
     * Scanner partagé pour la lecture des entrées utilisateur
     */
    private static Scanner scanner;

    /**
     * Logger applicatif
     */
    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        runOrmBootstrapAndBasicCrudDemo(); // Démo de démarrage et vérification de l’accès ORM

        System.out.println("Bienvenue dans GuideResto ! Que souhaitez-vous faire ?");
        int choice;

        try {
            do {
                printMainMenu();
                choice = readInt();
                proceedMainMenu(choice);
            } while (choice != 0);
        } finally {
            JpaUtils.close(); // Libération propre de l’EntityManagerFactory
        }
    }

    /**
     * Vérifie le démarrage ORM et exécute une démonstration de lecture/écriture sur quelques entités
     * Sert à valider que la configuration JPA et les mappings principaux sont opérationnels
     */
    private static void runOrmBootstrapAndBasicCrudDemo() {

        // Instancie puis ferme un EntityManager pour valider l’initialisation Hibernate
        EntityManager em = null;
        try {
            em = JpaUtils.getEntityManager();
            logger.info("Bootstrap ORM effectué, EntityManager ouvert={}", em.isOpen());
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        // Démonstration minimale sur City pour valider lecture et insertion conditionnelle
        AtomicInteger createdCityId = new AtomicInteger(-1);

        JpaUtils.inTransaction(entityManager -> {
            City existingCity = entityManager.find(City.class, 1);
            logger.info("Lecture City id=1 -> {}",
                    (existingCity != null ? existingCity.getCityName() : "null"));

            Number cityCount = (Number) entityManager
                    .createNativeQuery("select count(*) from VILLES where NOM_VILLE = 'ORM Demo City'")
                    .getSingleResult();

            if (cityCount != null && cityCount.intValue() == 0) {
                City newCity = new City();
                newCity.setZipCode("9999");
                newCity.setCityName("ORM Demo City");
                entityManager.persist(newCity);

                createdCityId.set(newCity.getId());
                logger.info("Insertion City effectuée, id={}", newCity.getId());
            } else {
                logger.info("Insertion City ignorée car l’enregistrement existe déjà");
            }
        });

        // Recharge la ville si elle a été créée pour valider la persistance effective
        if (createdCityId.get() > 0) {
            JpaUtils.inTransaction(entityManager -> {
                City reloaded = entityManager.find(City.class, createdCityId.get());
                logger.info("Reload City id={}, name={}",
                        createdCityId.get(),
                        (reloaded != null ? reloaded.getCityName() : "null"));
            });
        }

        // Démonstration minimale sur Restaurant pour valider la lecture et l’insertion conditionnelle
        AtomicInteger createdRestaurantId = new AtomicInteger(-1);

        JpaUtils.inTransaction(entityManager -> {

            Restaurant existingRestaurant = entityManager.find(Restaurant.class, 1);
            logger.info("Lecture Restaurant id=1 -> {}",
                    (existingRestaurant != null ? existingRestaurant.getName() : "null"));

            Number restaurantCount = (Number) entityManager
                    .createNativeQuery("select count(*) from RESTAURANTS where NOM = 'ORM Demo Restaurant'")
                    .getSingleResult();

            if (restaurantCount != null && restaurantCount.intValue() == 0) {

                Integer anyCityId = ((Number) entityManager
                        .createNativeQuery("select numero from VILLES fetch first 1 rows only")
                        .getSingleResult()).intValue();

                Integer anyTypeId = ((Number) entityManager
                        .createNativeQuery("select numero from TYPES_GASTRONOMIQUES fetch first 1 rows only")
                        .getSingleResult()).intValue();

                City anyCity = entityManager.getReference(City.class, anyCityId);
                RestaurantType anyType = entityManager.getReference(RestaurantType.class, anyTypeId);

                Restaurant newRestaurant = new Restaurant();
                newRestaurant.setName("ORM Demo Restaurant");
                newRestaurant.setDescription("Enregistrement créé pour valider les associations");
                newRestaurant.setWebsite("https://example.invalid");

                newRestaurant.setAddress(new Localisation("Rue de démonstration 1"));
                newRestaurant.setCity(anyCity);
                newRestaurant.setType(anyType);

                entityManager.persist(newRestaurant);

                createdRestaurantId.set(newRestaurant.getId());
                logger.info("Insertion Restaurant effectuée, id={}", newRestaurant.getId());

            } else {
                logger.info("Insertion Restaurant ignorée car l’enregistrement existe déjà");
            }
        });

        // Recharge le restaurant si créé pour valider la persistance effective
        if (createdRestaurantId.get() > 0) {
            JpaUtils.inTransaction(entityManager -> {
                Restaurant reloaded = entityManager.find(Restaurant.class, createdRestaurantId.get());
                logger.info("Reload Restaurant id={}, name={}",
                        createdRestaurantId.get(),
                        (reloaded != null ? reloaded.getName() : "null"));
            });
        }
    }

    /**
     * Affiche le menu principal de l’application
     */
    private static void printMainMenu() {
        System.out.println("======================================================");
        System.out.println("Que voulez-vous faire ?");
        System.out.println("1. Afficher la liste de tous les restaurants");
        System.out.println("2. Rechercher un restaurant par son nom");
        System.out.println("3. Rechercher un restaurant par ville");
        System.out.println("4. Rechercher un restaurant par son type de cuisine");
        System.out.println("5. Saisir un nouveau restaurant");
        System.out.println("0. Quitter l'application");
    }

    /**
     * Redirige l’exécution en fonction du choix utilisateur
     */
    private static void proceedMainMenu(int choice) {
        switch (choice) {
            case 1:
                showRestaurantsList();
                break;
            case 2:
                searchRestaurantByName();
                break;
            case 3:
                searchRestaurantByCity();
                break;
            case 4:
                searchRestaurantByType();
                break;
            case 5:
                addNewRestaurant();
                break;
            case 0:
                System.out.println("Au revoir !");
                break;
            default:
                System.out.println("Erreur : saisie incorrecte. Veuillez réessayer");
                break;
        }
    }

    /**
     * Affiche une liste de restaurants et demande à l’utilisateur de sélectionner un élément par son nom exact
     */
    private static Restaurant pickRestaurant(Set<Restaurant> restaurants) {
        if (restaurants.isEmpty()) {
            System.out.println("Aucun restaurant n'a été trouvé !");
            return null;
        }

        for (Restaurant currentRest : restaurants) {
            StringBuilder result = new StringBuilder();
            result.append("\"").append(currentRest.getName()).append("\"")
                    .append(" - ")
                    .append(currentRest.getAddress().getStreet())
                    .append(" - ");

            City c = currentRest.getCity();
            if (c != null) {
                result.append(c.getZipCode()).append(" ").append(c.getCityName());
            } else {
                result.append("(ville non renseignée)");
            }

            System.out.println(result);
        }

        System.out.println("Veuillez saisir le nom exact du restaurant dont vous voulez voir le détail, ou appuyez sur Enter pour revenir en arrière");
        String choice = readString();

        return searchRestaurantByName(restaurants, choice);
    }

    /**
     * Affiche la liste complète des restaurants depuis la persistance
     */
    private static void showRestaurantsList() {
        System.out.println("Liste des restaurants : ");

        Restaurant restaurant = pickRestaurant(loadAllRestaurants());

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Filtre localement la liste des restaurants par recherche partielle sur le nom
     */
    private static void searchRestaurantByName() {
        System.out.println("Veuillez entrer une partie du nom recherché : ");
        String research = readString();

        Set<Restaurant> fullList = loadAllRestaurants();
        Set<Restaurant> filteredList = new LinkedHashSet<>();

        for (Restaurant currentRestaurant : fullList) {
            if (currentRestaurant.getName().toUpperCase().contains(research.toUpperCase())) {
                filteredList.add(currentRestaurant);
            }
        }

        Restaurant restaurant = pickRestaurant(filteredList);

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Filtre localement la liste des restaurants par recherche partielle sur le nom de la ville
     */
    private static void searchRestaurantByCity() {
        System.out.println("Veuillez entrer une partie du nom de la ville désirée : ");
        String research = readString();

        Set<Restaurant> fullList = loadAllRestaurants();
        Set<Restaurant> filteredList = new LinkedHashSet<>();

        for (Restaurant currentRestaurant : fullList) {
            City city = currentRestaurant.getCity();

            if (city != null && city.getCityName() != null
                    && city.getCityName().toUpperCase().contains(research.toUpperCase())) {
                filteredList.add(currentRestaurant);
            }
        }

        Restaurant restaurant = pickRestaurant(filteredList);

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Propose une sélection de villes basée sur une liste persistée
     * Permet la création d’une ville si l’utilisateur saisit NEW
     */
    private static City pickCity(Set<City> cities) {
        System.out.println("Voici la liste des villes possibles, veuillez entrer le NPA de la ville désirée : ");

        for (City currentCity : cities) {
            System.out.println(currentCity.getZipCode() + " " + currentCity.getCityName());
        }
        System.out.println("Entrez \"NEW\" pour créer une nouvelle ville");
        String choice = readString();

        if (choice.equals("NEW")) {
            City city = new City();
            System.out.println("Veuillez entrer le NPA de la nouvelle ville : ");
            city.setZipCode(readString());
            System.out.println("Veuillez entrer le nom de la nouvelle ville : ");
            city.setCityName(readString());
            JpaUtils.inTransaction(em -> em.persist(city));
            return city;
        }

        return searchCityByZipCode(cities, choice);
    }

    /**
     * Propose une sélection de types de restaurant depuis une liste persistée
     */
    private static RestaurantType pickRestaurantType(Set<RestaurantType> types) {
        System.out.println("Voici la liste des types possibles, veuillez entrer le libellé exact du type désiré : ");
        for (RestaurantType currentType : types) {
            System.out.println("\"" + currentType.getLabel() + "\" : " + currentType.getDescription());
        }
        String choice = readString();

        return searchTypeByLabel(types, choice);
    }

    /**
     * Filtre localement la liste des restaurants sur base de l’identifiant de type sélectionné
     */
    private static void searchRestaurantByType() {
        Set<Restaurant> fullList = loadAllRestaurants();
        Set<Restaurant> filteredList = new LinkedHashSet<>();

        RestaurantType chosenType = pickRestaurantType(loadAllRestaurantTypes());

        if (chosenType != null) {
            for (Restaurant currentRestaurant : fullList) {
                if (currentRestaurant.getType() != null
                        && Objects.equals(currentRestaurant.getType().getId(), chosenType.getId())) {
                    filteredList.add(currentRestaurant);
                }
            }
        }

        Restaurant restaurant = pickRestaurant(filteredList);

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Crée un restaurant persistant en chargeant des références gérées pour la ville et le type
     */
    private static void addNewRestaurant() {
        System.out.println("Vous allez ajouter un nouveau restaurant !");
        System.out.println("Quel est son nom ?");
        String name = readString();

        System.out.println("Veuillez entrer une courte description : ");
        String description = readString();

        System.out.println("Veuillez entrer l'adresse de son site internet : ");
        String website = readString();

        System.out.println("Rue : ");
        String street = readString();

        City selectedCity;
        do {
            selectedCity = pickCity(loadAllCities());
        } while (selectedCity == null);

        RestaurantType selectedType;
        do {
            selectedType = pickRestaurantType(loadAllRestaurantTypes());
        } while (selectedType == null);

        final Integer cityId = selectedCity.getId();
        final Integer typeId = selectedType.getId();

        AtomicInteger createdRestaurantId = new AtomicInteger(-1);

        JpaUtils.inTransaction(em -> {
            City managedCity = em.getReference(City.class, cityId);
            RestaurantType managedType = em.getReference(RestaurantType.class, typeId);

            Restaurant newRestaurant = new Restaurant(null, name, description, website, street, managedCity, managedType);
            em.persist(newRestaurant);

            createdRestaurantId.set(newRestaurant.getId());
        });

        if (createdRestaurantId.get() > 0) {
            JpaUtils.inTransaction(em -> {
                Restaurant reloaded = em.find(Restaurant.class, createdRestaurantId.get());
                if (reloaded != null) {
                    showRestaurant(reloaded);
                }
            });
        }
    }

    /**
     * Affiche le détail d’un restaurant en rechargeant un graphe prêt pour l’affichage
     */
    private static void showRestaurant(Restaurant restaurant) {
        restaurant = loadRestaurantForDisplay(restaurant.getId());

        System.out.println("Affichage d'un restaurant : ");
        StringBuilder sb = new StringBuilder();
        sb.append(restaurant.getName()).append("\n");
        sb.append(restaurant.getDescription()).append("\n");
        sb.append(restaurant.getType().getLabel()).append("\n");
        sb.append(restaurant.getWebsite()).append("\n");
        sb.append(restaurant.getAddress().getStreet()).append(", ");

        City c = restaurant.getCity();
        if (c != null) {
            sb.append(c.getZipCode()).append(" ").append(c.getCityName()).append("\n");
        } else {
            sb.append("(ville non renseignée)\n");
        }

        sb.append("Nombre de likes : ").append(countLikes(restaurant.getEvaluations(), true)).append("\n");
        sb.append("Nombre de dislikes : ").append(countLikes(restaurant.getEvaluations(), false)).append("\n");
        sb.append("\nEvaluations reçues : ").append("\n");

        for (Evaluation currentEval : restaurant.getEvaluations()) {
            String text = getCompleteEvaluationDescription(currentEval);
            if (text != null) {
                sb.append(text).append("\n");
            }
        }

        System.out.println(sb);

        int choice;
        do {
            showRestaurantMenu();
            choice = readInt();
            proceedRestaurantMenu(choice, restaurant);
        } while (choice != 0 && choice != 6);
    }

    /**
     * Compte les likes ou dislikes dans l’ensemble des évaluations
     */
    private static int countLikes(Set<Evaluation> evaluations, Boolean likeRestaurant) {
        int count = 0;
        for (Evaluation currentEval : evaluations) {
            if (currentEval instanceof BasicEvaluation && ((BasicEvaluation) currentEval).getLikeRestaurant() == likeRestaurant) {
                count++;
            }
        }
        return count;
    }

    /**
     * Produit le texte détaillé d’une évaluation complète
     * Retourne null pour une évaluation basique
     */
    private static String getCompleteEvaluationDescription(Evaluation eval) {
        StringBuilder result = new StringBuilder();

        if (eval instanceof CompleteEvaluation) {
            CompleteEvaluation ce = (CompleteEvaluation) eval;
            result.append("Evaluation de : ").append(ce.getUsername()).append("\n");
            result.append("Commentaire : ").append(ce.getComment()).append("\n");
            for (Grade currentGrade : ce.getGrades()) {
                result.append(currentGrade.getCriteria().getName()).append(" : ")
                        .append(currentGrade.getGrade()).append("/5").append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Affiche les actions disponibles sur un restaurant
     */
    private static void showRestaurantMenu() {
        System.out.println("======================================================");
        System.out.println("Que souhaitez-vous faire ?");
        System.out.println("1. J'aime ce restaurant !");
        System.out.println("2. Je n'aime pas ce restaurant !");
        System.out.println("3. Faire une évaluation complète de ce restaurant !");
        System.out.println("4. Editer ce restaurant");
        System.out.println("5. Editer l'adresse du restaurant");
        System.out.println("6. Supprimer ce restaurant");
        System.out.println("0. Revenir au menu principal");
    }

    /**
     * Route l’action utilisateur vers le traitement correspondant
     */
    private static void proceedRestaurantMenu(int choice, Restaurant restaurant) {
        switch (choice) {
            case 1:
                addBasicEvaluation(restaurant, true);
                break;
            case 2:
                addBasicEvaluation(restaurant, false);
                break;
            case 3:
                evaluateRestaurant(restaurant);
                break;
            case 4:
                editRestaurant(restaurant);
                break;
            case 5:
                editRestaurantAddress(restaurant);
                break;
            case 6:
                deleteRestaurant(restaurant);
                break;
            case 0:
                break;
            default:
                break;
        }
    }

    /**
     * Ajoute une évaluation basique au restaurant sélectionné
     */
    private static void addBasicEvaluation(Restaurant restaurant, Boolean like) {
        String ipAddress;
        try {
            ipAddress = Inet4Address.getLocalHost().toString();
        } catch (UnknownHostException ex) {
            logger.error("Error - Couldn't retreive host IP address");
            ipAddress = "Indisponible";
        }
        BasicEvaluation eval = new BasicEvaluation(1, new Date(), restaurant, like, ipAddress);
        restaurant.getEvaluations().add(eval);
        System.out.println("Votre vote a été pris en compte !");
    }

    /**
     * Crée une évaluation complète en demandant un commentaire et une note par critère
     */
    private static void evaluateRestaurant(Restaurant restaurant) {
        System.out.println("Merci d'évaluer ce restaurant !");
        System.out.println("Quel est votre nom d'utilisateur ? ");
        String username = readString();
        System.out.println("Quel commentaire aimeriez-vous publier ?");
        String comment = readString();

        CompleteEvaluation eval = new CompleteEvaluation(1, new Date(), restaurant, comment, username);
        restaurant.getEvaluations().add(eval);

        System.out.println("Veuillez svp donner une note entre 1 et 5 pour chacun de ces critères : ");
        for (EvaluationCriteria currentCriteria : loadAllEvaluationCriterias()) {
            System.out.println(currentCriteria.getName() + " : " + currentCriteria.getDescription());
            Integer note = readInt();
            Grade grade = new Grade(1, note, eval, currentCriteria);
            eval.getGrades().add(grade);
        }

        System.out.println("Votre évaluation a bien été enregistrée, merci !");
    }

    /**
     * Met à jour les attributs principaux d’un restaurant et applique un changement de type si demandé
     */
    private static void editRestaurant(Restaurant restaurant) {
        System.out.println("Edition d'un restaurant !");

        System.out.println("Nouveau nom : ");
        String newName = readString();

        System.out.println("Nouvelle description : ");
        String newDescription = readString();

        System.out.println("Nouveau site web : ");
        String newWebsite = readString();

        System.out.println("Nouveau type de restaurant : ");
        RestaurantType chosenType = pickRestaurantType(loadAllRestaurantTypes());
        Integer chosenTypeId = (chosenType != null ? chosenType.getId() : null);

        Integer restaurantId = restaurant.getId();

        JpaUtils.inTransaction(em -> {
            Restaurant managedRestaurant = em.find(Restaurant.class, restaurantId);
            if (managedRestaurant == null) {
                return;
            }

            managedRestaurant.setName(newName);
            managedRestaurant.setDescription(newDescription);
            managedRestaurant.setWebsite(newWebsite);

            if (chosenTypeId != null) {
                RestaurantType managedType = em.getReference(RestaurantType.class, chosenTypeId);
                managedRestaurant.setType(managedType);
            }
        });

        System.out.println("Le restaurant a bien été modifié.");
    }

    /**
     * Met à jour l’adresse et la ville d’un restaurant dans une transaction
     */
    private static void editRestaurantAddress(Restaurant restaurant) {
        System.out.println("Edition de l'adresse d'un restaurant !");

        System.out.println("Nouvelle rue : ");
        String newStreet = readString();

        City selectedCity = pickCity(loadAllCities());
        if (selectedCity == null) {
            return;
        }

        final Integer restaurantId = restaurant.getId();
        final Integer newCityId = selectedCity.getId();

        JpaUtils.inTransaction(em -> {
            Restaurant managedRestaurant = em.find(Restaurant.class, restaurantId);
            if (managedRestaurant == null) {
                return;
            }

            managedRestaurant.getAddress().setStreet(newStreet);
            managedRestaurant.setCity(em.getReference(City.class, newCityId));
        });

        System.out.println("L'adresse a bien été modifiée.");
    }

    /**
     * Supprime un restaurant après confirmation utilisateur
     */
    private static void deleteRestaurant(Restaurant restaurant) {
        System.out.println("Etes-vous sûr de vouloir supprimer ce restaurant ? (O/n)");
        String choice = readString();

        if (!choice.equalsIgnoreCase("o")) {
            return;
        }

        final Integer restaurantId = restaurant.getId();

        JpaUtils.inTransaction(em -> {
            Restaurant managedRestaurant = em.find(Restaurant.class, restaurantId);
            if (managedRestaurant != null) {
                em.remove(managedRestaurant);
            }
        });

        System.out.println("Le restaurant a bien été supprimé.");
    }

    /**
     * Recherche un restaurant par nom exact dans une collection en mémoire
     */
    private static Restaurant searchRestaurantByName(Set<Restaurant> restaurants, String name) {
        for (Restaurant current : restaurants) {
            if (current.getName().equalsIgnoreCase(name)) {
                return current;
            }
        }
        return null;
    }

    /**
     * Recherche une ville par NPA exact dans une collection en mémoire
     */
    private static City searchCityByZipCode(Set<City> cities, String zipCode) {
        for (City current : cities) {
            if (current.getZipCode().equalsIgnoreCase(zipCode)) {
                return current;
            }
        }
        return null;
    }

    /**
     * Recherche un type par libellé exact dans une collection en mémoire
     */
    private static RestaurantType searchTypeByLabel(Set<RestaurantType> types, String label) {
        for (RestaurantType current : types) {
            if (current.getLabel().equalsIgnoreCase(label)) {
                return current;
            }
        }
        return null;
    }

    /**
     * Lit un entier en gérant les erreurs de saisie utilisateur
     */
    private static int readInt() {
        int i = 0;
        boolean success = false;
        do {
            try {
                i = scanner.nextInt();
                success = true;
            } catch (InputMismatchException e) {
                System.out.println("Erreur ! Veuillez entrer un nombre entier s'il vous plaît !");
            } finally {
                scanner.nextLine();
            }

        } while (!success);

        return i;
    }

    /**
     * Lit une ligne complète saisie par l’utilisateur
     */
    private static String readString() {
        return scanner.nextLine();
    }

    /**
     * Charge tous les restaurants via le mapper dédié
     */
    private static Set<Restaurant> loadAllRestaurants() {
        return new LinkedHashSet<>(restaurantMapper.findAll());
    }

    /**
     * Charge toutes les villes via le mapper dédié
     */
    private static Set<City> loadAllCities() {
        return new LinkedHashSet<>(cityMapper.findAll());
    }

    /**
     * Charge tous les types de restaurants via le mapper dédié
     */
    private static Set<RestaurantType> loadAllRestaurantTypes() {
        return new LinkedHashSet<>(restaurantTypeMapper.findAll());
    }

    /**
     * Charge tous les critères d’évaluation via une requête JPQL simple
     * À terme ce chargement peut être déplacé dans un mapper dédié
     */
    private static Set<EvaluationCriteria> loadAllEvaluationCriterias() {
        Set<EvaluationCriteria> result = new LinkedHashSet<>();
        JpaUtils.inTransaction(em -> result.addAll(
                em.createQuery(
                        "select c from EvaluationCriteria c order by c.name",
                        EvaluationCriteria.class
                ).getResultList()
        ));
        return result;
    }

    /**
     * Recharge un restaurant et initialise les associations requises pour l’affichage console
     * Permet d’éviter les erreurs liées aux proxys lazy hors transaction
     */
    private static Restaurant loadRestaurantForDisplay(Integer restaurantId) {
        AtomicReference<Restaurant> ref = new AtomicReference<>();

        JpaUtils.inTransaction(em -> {
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

            ref.set(r);
        });

        return ref.get();
    }
}
