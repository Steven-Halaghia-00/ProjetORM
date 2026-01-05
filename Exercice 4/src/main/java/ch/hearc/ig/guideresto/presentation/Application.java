package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.EntityManager;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Point d’entrée console de l’application
 * Responsabilités principales
 * - Gestion des entrées utilisateur et navigation dans les menus
 * - Appels aux opérations de persistance via JPA (transactions gérées via JpaUtils)
 * - Chargement explicite des données nécessaires à l’affichage pour éviter le lazy loading hors transaction
 */
public class Application {

    private static Scanner scanner;
    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        // Vérifie au démarrage que JPA/Hibernate est bien initialisé et que le mapping minimal fonctionne
        runOrmBootstrapAndBasicCrudDemo();

        System.out.println("Bienvenue dans GuideResto ! Que souhaitez-vous faire ?");
        int choice;

        try {
            do {
                printMainMenu();
                choice = readInt();
                proceedMainMenu(choice);
            } while (choice != 0);
        } finally {
            // Libère proprement la factory JPA et les ressources associées
            JpaUtils.close();
        }
    }

    /**
     * Démarre JPA et exécute quelques opérations simples de lecture et d’insertion
     * Objectif
     * - Valider l’accès à la base via EntityManager
     * - Valider la persistance d’entités et des associations nécessaires au restaurant
     * - Éviter les doublons en base en contrôlant l’existence préalable d’un enregistrement de démo
     */
    private static void runOrmBootstrapAndBasicCrudDemo() {

        // Initialise JPA et force la création de l’EntityManagerFactory si nécessaire
        EntityManager em = null;
        try {
            em = JpaUtils.getEntityManager();
            logger.info("Bootstrap JPA effectué. EntityManager open={}", em.isOpen());
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        // Insert de démonstration sur City avec garde-fou contre les doublons
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
                logger.info("City de démo persistée. id={}", newCity.getId());
            } else {
                logger.info("City de démo déjà présente, insertion ignorée");
            }
        });

        // Recharge après insertion pour vérifier l’écriture en base
        if (createdCityId.get() > 0) {
            JpaUtils.inTransaction(entityManager -> {
                City reloaded = entityManager.find(City.class, createdCityId.get());
                logger.info("City rechargée. id={}, name={}",
                        createdCityId.get(),
                        (reloaded != null ? reloaded.getCityName() : "null"));
            });
        }

        // Insert de démonstration sur Restaurant en réutilisant des références existantes (ville et type)
        AtomicInteger createdRestaurantId = new AtomicInteger(-1);

        JpaUtils.inTransaction(entityManager -> {

            Restaurant existingRestaurant = entityManager.find(Restaurant.class, 1);
            logger.info("Lecture Restaurant id=1 -> {}",
                    (existingRestaurant != null ? existingRestaurant.getName() : "null"));

            Number restaurantCount = (Number) entityManager
                    .createNativeQuery("select count(*) from RESTAURANTS where NOM = 'ORM Demo Restaurant'")
                    .getSingleResult();

            if (restaurantCount != null && restaurantCount.intValue() == 0) {

                // Récupère des identifiants existants pour satisfaire les contraintes de clés étrangères
                Integer anyCityId = ((Number) entityManager
                        .createNativeQuery("select numero from VILLES fetch first 1 rows only")
                        .getSingleResult()).intValue();

                Integer anyTypeId = ((Number) entityManager
                        .createNativeQuery("select numero from TYPES_GASTRONOMIQUES fetch first 1 rows only")
                        .getSingleResult()).intValue();

                // getReference évite une requête immédiate et crée un proxy, utile si seule la FK est nécessaire
                City anyCity = entityManager.getReference(City.class, anyCityId);
                RestaurantType anyType = entityManager.getReference(RestaurantType.class, anyTypeId);

                Restaurant newRestaurant = new Restaurant();
                newRestaurant.setName("ORM Demo Restaurant");
                newRestaurant.setDescription("Enregistrement créé pour valider les associations");
                newRestaurant.setWebsite("https://example.invalid");

                // Les champs address, city et type étant obligatoires, ils sont initialisés avant persist
                newRestaurant.setAddress(new Localisation("Rue de démonstration 1"));
                newRestaurant.setCity(anyCity);
                newRestaurant.setType(anyType);

                entityManager.persist(newRestaurant);

                createdRestaurantId.set(newRestaurant.getId());
                logger.info("Restaurant de démo persisté. id={}", newRestaurant.getId());

            } else {
                logger.info("Restaurant de démo déjà présent, insertion ignorée");
            }
        });

        // Recharge après insertion pour vérifier l’écriture en base
        if (createdRestaurantId.get() > 0) {
            JpaUtils.inTransaction(entityManager -> {
                Restaurant reloaded = entityManager.find(Restaurant.class, createdRestaurantId.get());
                logger.info("Restaurant rechargé. id={}, name={}",
                        createdRestaurantId.get(),
                        (reloaded != null ? reloaded.getName() : "null"));
            });
        }
    }

    /**
     * Affiche le menu principal
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
     * Route le choix utilisateur vers l’action correspondante
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
     * Affiche une liste et permet de sélectionner un restaurant par son nom exact
     * La sélection par nom exact évite d’introduire une numérotation et une logique de mapping index -> entité
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
     * Charge et affiche tous les restaurants
     * Le chargement utilise join fetch pour éviter des proxys non initialisés lors de l’affichage
     */
    private static void showRestaurantsList() {
        System.out.println("Liste des restaurants : ");

        Restaurant restaurant = pickRestaurant(loadAllRestaurants());

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Recherche côté client sur une liste déjà chargée
     * Cette approche reste simple mais n’est pas optimale pour de grands volumes de données
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
     * Recherche côté client sur la ville du restaurant
     * Le cityName est consulté uniquement si l’association City est initialisée via le chargement initial
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
     * Permet de sélectionner une ville existante ou d’en créer une nouvelle
     * La création est persistée immédiatement dans une transaction dédiée
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
     * Permet de sélectionner un type de restaurant existant
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
     * Recherche côté client par type
     * Le type est comparé via son identifiant pour éviter les erreurs liées à des instances détachées
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
     * Crée un nouveau restaurant
     * La persistance utilise getReference afin d’éviter de charger entièrement la ville et le type
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

        // Recharge après création pour afficher un état complet et cohérent
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
     * Affiche le détail d’un restaurant puis propose un menu d’actions
     * Le restaurant est rechargé avec ses associations nécessaires à l’affichage
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

        String text;
        for (Evaluation currentEval : restaurant.getEvaluations()) {
            text = getCompleteEvaluationDescription(currentEval);
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
     * Compte les votes basiques positifs ou négatifs
     * Seules les BasicEvaluation sont prises en compte pour le comptage like/dislike
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
     * Construit une description pour une évaluation complète
     * Retourne une chaîne vide pour une évaluation basique afin de ne pas l’afficher dans le détail
     */
    private static String getCompleteEvaluationDescription(Evaluation eval) {
        StringBuilder result = new StringBuilder();

        if (eval instanceof CompleteEvaluation) {
            CompleteEvaluation ce = (CompleteEvaluation) eval;
            result.append("Evaluation de : ").append(ce.getUsername()).append("\n");
            result.append("Commentaire : ").append(ce.getComment()).append("\n");
            for (Grade currentGrade : ce.getGrades()) {
                result.append(currentGrade.getCriteria().getName()).append(" : ").append(currentGrade.getGrade()).append("/5").append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Affiche le menu d’actions sur un restaurant
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
     * Route l’action sélectionnée vers la méthode correspondante
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
     * Ajoute un vote basique au restaurant
     * Remarque : ici l’évaluation est ajoutée à la collection en mémoire sans persistance explicite
     * La persistance complète des évaluations est gérée à un autre niveau dans la version finale
     */
    private static void addBasicEvaluation(Restaurant restaurant, Boolean like) {
        String ipAddress;
        try {
            ipAddress = Inet4Address.getLocalHost().toString();
        } catch (UnknownHostException ex) {
            logger.error("Impossible de récupérer l’adresse IP locale");
            ipAddress = "Indisponible";
        }
        BasicEvaluation eval = new BasicEvaluation(1, new Date(), restaurant, like, ipAddress);
        restaurant.getEvaluations().add(eval);
        System.out.println("Votre vote a été pris en compte !");
    }

    /**
     * Saisit une évaluation complète et l’ajoute à la collection en mémoire
     * Remarque : la persistance des grades et de l’évaluation complète est gérée dans la version finalisée
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
     * Met à jour les informations d’un restaurant
     * La mise à jour est persistée via merge dans une transaction
     */
    private static void editRestaurant(Restaurant restaurant) {
        System.out.println("Edition d'un restaurant !");

        System.out.println("Nouveau nom : ");
        restaurant.setName(readString());
        System.out.println("Nouvelle description : ");
        restaurant.setDescription(readString());
        System.out.println("Nouveau site web : ");
        restaurant.setWebsite(readString());
        System.out.println("Nouveau type de restaurant : ");

        RestaurantType newType = pickRestaurantType(loadAllRestaurantTypes());
        if (newType != null && newType != restaurant.getType()) {
            restaurant.getType().getRestaurants().remove(restaurant);
            restaurant.setType(newType);
            newType.getRestaurants().add(restaurant);

            // Merge appliqué sur un objet potentiellement détaché issu d’un chargement précédent
            JpaUtils.inTransaction(em -> em.merge(restaurant));
        }

        System.out.println("Merci, le restaurant a bien été modifié !");
    }

    /**
     * Met à jour l’adresse et la ville d’un restaurant
     * Le restaurant est rechargé en transaction afin de garantir un contexte de persistance actif
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
     * Supprime un restaurant après confirmation
     * La suppression est faite sur une entité managée pour respecter les règles JPA
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
     * Recherche un restaurant par nom exact dans un ensemble
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
     * Recherche une ville par code postal exact dans un ensemble
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
     * Recherche un type par libellé exact dans un ensemble
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
     * Lit un entier en sécurisant la saisie utilisateur
     * Le scanner est repositionné en fin de ligne afin de préserver la cohérence avec les lectures de chaînes
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
     * Lit une ligne de texte
     */
    private static String readString() {
        return scanner.nextLine();
    }

    /**
     * Charge tous les restaurants avec leurs associations nécessaires à l’affichage de listes
     * join fetch évite une rafale de requêtes lors de l’accès à city et restaurantType
     */
    private static Set<Restaurant> loadAllRestaurants() {
        Set<Restaurant> result = new LinkedHashSet<>();
        JpaUtils.inTransaction(em -> result.addAll(
                em.createQuery(
                        "select distinct r " +
                                "from Restaurant r " +
                                "join fetch r.city " +
                                "join fetch r.restaurantType " +
                                "order by r.name",
                        Restaurant.class
                ).getResultList()
        ));
        return result;
    }

    /**
     * Charge toutes les villes
     */
    private static Set<City> loadAllCities() {
        Set<City> result = new LinkedHashSet<>();
        JpaUtils.inTransaction(em -> result.addAll(
                em.createQuery(
                        "select c from City c order by c.zipCode, c.cityName",
                        City.class
                ).getResultList()
        ));
        return result;
    }

    /**
     * Charge tous les types de restaurants
     */
    private static Set<RestaurantType> loadAllRestaurantTypes() {
        Set<RestaurantType> result = new LinkedHashSet<>();
        JpaUtils.inTransaction(em -> result.addAll(
                em.createQuery(
                        "select t from RestaurantType t order by t.label",
                        RestaurantType.class
                ).getResultList()
        ));
        return result;
    }

    /**
     * Charge tous les critères d’évaluation
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
     * Recharge un restaurant et initialise le graphe nécessaire à l’affichage détaillé
     * Objectif
     * - Éviter l’accès à des associations lazy après la fermeture de la transaction
     * - Charger city, restaurantType et les collections d’évaluations nécessaires
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

            // Force l’initialisation des sous-collections nécessaires à l’affichage des évaluations complètes
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
