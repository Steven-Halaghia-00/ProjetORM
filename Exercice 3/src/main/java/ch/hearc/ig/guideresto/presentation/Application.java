package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.FakeItems;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.EntityManager;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Point d’entrée console de l’application
 * Gère l’interaction utilisateur, l’affichage des menus et l’orchestration des opérations
 */
public class Application {

    /** Scanner partagé pour toutes les lectures clavier */
    private static Scanner scanner;

    /** Logger applicatif */
    private static final Logger logger = LogManager.getLogger(Application.class);

    /**
     * Démarre l’application, initialise les composants techniques et lance la boucle du menu principal
     */
    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        // Vérifie l’initialisation JPA et exécute une démonstration minimale de lecture/écriture
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
            // Libère proprement les ressources JPA
            JpaUtils.close();
        }
    }

    /**
     * Valide le démarrage JPA et exécute des opérations CRUD simples sur des entités
     * Les insertions sont conditionnelles afin d’éviter les doublons à chaque lancement
     */
    private static void runOrmBootstrapAndBasicCrudDemo() {

        // Instancie et ferme un EntityManager pour vérifier l’initialisation Hibernate
        EntityManager em = null;
        try {
            em = JpaUtils.getEntityManager();
            logger.info("EntityManager initialisé. isOpen={}", em.isOpen());
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        // Conserve l’identifiant créé afin de recharger l’entité après persistance
        AtomicInteger createdCityId = new AtomicInteger(-1);

        // Transaction de démonstration sur City
        JpaUtils.inTransaction(entityManager -> {

            // Lecture simple par identifiant
            City existingCity = entityManager.find(City.class, 1);
            logger.info("City.find(1) -> {}",
                    (existingCity != null ? existingCity.getCityName() : "null"));

            // Vérification via requête SQL native pour éviter des insertions multiples
            Number cityCount = (Number) entityManager
                    .createNativeQuery("select count(*) from VILLES where NOM_VILLE = 'ORM Demo City'")
                    .getSingleResult();

            if (cityCount != null && cityCount.intValue() == 0) {
                City newCity = new City();
                newCity.setZipCode("9999");
                newCity.setCityName("ORM Demo City");
                entityManager.persist(newCity);

                createdCityId.set(newCity.getId());
                logger.info("City persistée. id={}", newCity.getId());
            } else {
                logger.info("City persist non exécuté (enregistrement déjà présent)");
            }
        });

        // Recharge l’entité créée afin de valider la persistance
        if (createdCityId.get() > 0) {
            JpaUtils.inTransaction(entityManager -> {
                City reloaded = entityManager.find(City.class, createdCityId.get());
                logger.info("City rechargée. id={}, name={}",
                        createdCityId.get(),
                        (reloaded != null ? reloaded.getCityName() : "null"));
            });
        }

        // Conserve l’identifiant créé afin de recharger l’entité après persistance
        AtomicInteger createdRestaurantId = new AtomicInteger(-1);

        // Transaction de démonstration sur Restaurant avec références existantes
        JpaUtils.inTransaction(entityManager -> {

            // Lecture simple par identifiant
            Restaurant existingRestaurant = entityManager.find(Restaurant.class, 1);
            logger.info("Restaurant.find(1) -> {}",
                    (existingRestaurant != null ? existingRestaurant.getName() : "null"));

            // Vérification via requête SQL native pour éviter des insertions multiples
            Number restaurantCount = (Number) entityManager
                    .createNativeQuery("select count(*) from RESTAURANTS where NOM = 'ORM Demo Restaurant'")
                    .getSingleResult();

            if (restaurantCount != null && restaurantCount.intValue() == 0) {

                // Récupère des identifiants existants afin de renseigner les relations obligatoires
                Integer anyCityId = ((Number) entityManager
                        .createNativeQuery("select numero from VILLES fetch first 1 rows only")
                        .getSingleResult()).intValue();

                Integer anyTypeId = ((Number) entityManager
                        .createNativeQuery("select numero from TYPES_GASTRONOMIQUES fetch first 1 rows only")
                        .getSingleResult()).intValue();

                // Utilise des références JPA pour éviter le chargement complet des entités
                City anyCity = entityManager.getReference(City.class, anyCityId);
                RestaurantType anyType = entityManager.getReference(RestaurantType.class, anyTypeId);

                // Création d’un restaurant de démonstration avec associations renseignées
                Restaurant newRestaurant = new Restaurant();
                newRestaurant.setName("ORM Demo Restaurant");
                newRestaurant.setDescription("Enregistrement créé pour valider les associations (Ex3).");
                newRestaurant.setWebsite("https://example.invalid");

                newRestaurant.setAddress(new Localisation("Rue de démonstration 1"));
                newRestaurant.setCity(anyCity);
                newRestaurant.setType(anyType);

                entityManager.persist(newRestaurant);

                createdRestaurantId.set(newRestaurant.getId());
                logger.info("Restaurant persisté. id={}", newRestaurant.getId());

            } else {
                logger.info("Restaurant persist non exécuté (enregistrement déjà présent)");
            }
        });

        // Recharge l’entité créée afin de valider la persistance
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
     *
     * @param choice valeur saisie comprise entre 0 et 5
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
     * Affiche une liste de restaurants et retourne celui sélectionné par saisie du nom exact
     *
     * @param restaurants collection à afficher
     * @return restaurant correspondant au nom saisi ou null si abandon ou introuvable
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
     * Affiche tous les restaurants disponibles puis ouvre le détail du restaurant sélectionné
     */
    private static void showRestaurantsList() {
        System.out.println("Liste des restaurants : ");

        Restaurant restaurant = pickRestaurant(FakeItems.getAllRestaurants());

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Recherche des restaurants par inclusion sur le nom, puis ouvre le détail du restaurant sélectionné
     */
    private static void searchRestaurantByName() {
        System.out.println("Veuillez entrer une partie du nom recherché : ");
        String research = readString();

        Set<Restaurant> fullList = FakeItems.getAllRestaurants();
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
     * Recherche des restaurants par inclusion sur le nom de la ville, puis ouvre le détail du restaurant sélectionné
     */
    private static void searchRestaurantByCity() {
        System.out.println("Veuillez entrer une partie du nom de la ville désirée : ");
        String research = readString();

        Set<Restaurant> fullList = FakeItems.getAllRestaurants();
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
     * Permet de sélectionner une ville via le code postal ou d’en créer une nouvelle
     *
     * @param cities collection de villes disponibles
     * @return ville sélectionnée ou créée, null si aucune sélection
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

            // Attribution manuelle d’identifiant utilisée uniquement dans le mode données factices
            city.setId(1);

            System.out.println("Veuillez entrer le NPA de la nouvelle ville : ");
            city.setZipCode(readString());
            System.out.println("Veuillez entrer le nom de la nouvelle ville : ");
            city.setCityName(readString());
            FakeItems.getCities().add(city);
            return city;
        }

        return searchCityByZipCode(cities, choice);
    }

    /**
     * Permet de sélectionner un type de restaurant à partir de son libellé
     *
     * @param types collection de types disponibles
     * @return type sélectionné ou null si introuvable
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
     * Filtre les restaurants par type puis ouvre le détail du restaurant sélectionné
     */
    private static void searchRestaurantByType() {
        Set<Restaurant> fullList = FakeItems.getAllRestaurants();
        Set<Restaurant> filteredList = new LinkedHashSet<>();

        RestaurantType chosenType = pickRestaurantType(FakeItems.getRestaurantTypes());

        if (chosenType != null) {
            for (Restaurant currentRestaurant : fullList) {
                if (currentRestaurant.getType() == chosenType) {
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
     * Saisit les informations de base puis crée un restaurant dans le référentiel en mémoire
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

        City city;
        do {
            city = pickCity(FakeItems.getCities());
        } while (city == null);

        RestaurantType restaurantType;
        do {
            restaurantType = pickRestaurantType(FakeItems.getRestaurantTypes());
        } while (restaurantType == null);

        Restaurant restaurant = new Restaurant(null, name, description, website, street, city, restaurantType);

        // Maintien des associations bidirectionnelles dans le référentiel factice
        city.getRestaurants().add(restaurant);
        restaurantType.getRestaurants().add(restaurant);
        FakeItems.getAllRestaurants().add(restaurant);

        showRestaurant(restaurant);
    }

    /**
     * Affiche le détail d’un restaurant et propose un menu d’actions
     *
     * @param restaurant restaurant dont les informations sont affichées
     */
    private static void showRestaurant(Restaurant restaurant) {
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
     * Compte les évaluations de type BasicEvaluation correspondant à une appréciation donnée
     *
     * @param evaluations ensemble des évaluations
     * @param likeRestaurant true pour likes, false pour dislikes
     * @return nombre d’occurrences correspondantes
     */
    private static int countLikes(Set<Evaluation> evaluations, Boolean likeRestaurant) {
        int count = 0;
        for (Evaluation currentEval : evaluations) {
            if (currentEval instanceof BasicEvaluation
                    && ((BasicEvaluation) currentEval).getLikeRestaurant() == likeRestaurant) {
                count++;
            }
        }
        return count;
    }

    /**
     * Construit une description détaillée pour une évaluation complète
     *
     * @param eval évaluation à décrire
     * @return description multi-lignes ou null si l’évaluation est basique
     */
    private static String getCompleteEvaluationDescription(Evaluation eval) {
        StringBuilder result = new StringBuilder();

        if (eval instanceof CompleteEvaluation) {
            CompleteEvaluation ce = (CompleteEvaluation) eval;
            result.append("Evaluation de : ").append(ce.getUsername()).append("\n");
            result.append("Commentaire : ").append(ce.getComment()).append("\n");
            for (Grade currentGrade : ce.getGrades()) {
                result.append(currentGrade.getCriteria().getName())
                        .append(" : ")
                        .append(currentGrade.getGrade())
                        .append("/5")
                        .append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Affiche les actions possibles sur un restaurant sélectionné
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
     * Exécute l’action sélectionnée dans le menu d’un restaurant
     *
     * @param choice numéro d’action
     * @param restaurant restaurant cible
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
     * Ajoute une évaluation binaire au restaurant courant
     * L’adresse IP locale est enregistrée à des fins de traçabilité
     *
     * @param restaurant restaurant évalué
     * @param like appréciation binaire
     */
    private static void addBasicEvaluation(Restaurant restaurant, Boolean like) {
        String ipAddress;
        try {
            // Récupère l’adresse IP locale
            ipAddress = Inet4Address.getLocalHost().toString();
        } catch (UnknownHostException ex) {
            logger.error("Error - Couldn't retreive host IP address");
            ipAddress = "Indisponible";
        }

        // Identifiant fixé à 1 dans le référentiel factice
        BasicEvaluation eval = new BasicEvaluation(1, new Date(), restaurant, like, ipAddress);
        restaurant.getEvaluations().add(eval);

        System.out.println("Votre vote a été pris en compte !");
    }

    /**
     * Crée une évaluation complète avec un commentaire et une note par critère
     *
     * @param restaurant restaurant évalué
     */
    private static void evaluateRestaurant(Restaurant restaurant) {
        System.out.println("Merci d'évaluer ce restaurant !");
        System.out.println("Quel est votre nom d'utilisateur ? ");
        String username = readString();
        System.out.println("Quel commentaire aimeriez-vous publier ?");
        String comment = readString();

        // Identifiant fixé à 1 dans le référentiel factice
        CompleteEvaluation eval = new CompleteEvaluation(1, new Date(), restaurant, comment, username);
        restaurant.getEvaluations().add(eval);

        System.out.println("Veuillez svp donner une note entre 1 et 5 pour chacun de ces critères : ");
        for (EvaluationCriteria currentCriteria : FakeItems.getEvaluationCriterias()) {
            System.out.println(currentCriteria.getName() + " : " + currentCriteria.getDescription());
            Integer note = readInt();

            // Identifiant fixé à 1 dans le référentiel factice
            Grade grade = new Grade(1, note, eval, currentCriteria);
            eval.getGrades().add(grade);
        }

        System.out.println("Votre évaluation a bien été enregistrée, merci !");
    }

    /**
     * Met à jour les informations principales d’un restaurant
     *
     * @param restaurant restaurant à modifier
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

        RestaurantType newType = pickRestaurantType(FakeItems.getRestaurantTypes());
        if (newType != null && newType != restaurant.getType()) {
            // Synchronise les associations bidirectionnelles dans le référentiel factice
            restaurant.getType().getRestaurants().remove(restaurant);
            restaurant.setType(newType);
            newType.getRestaurants().add(restaurant);
        }

        System.out.println("Merci, le restaurant a bien été modifié !");
    }

    /**
     * Met à jour l’adresse d’un restaurant et éventuellement la ville associée
     *
     * @param restaurant restaurant à modifier
     */
    private static void editRestaurantAddress(Restaurant restaurant) {
        System.out.println("Edition de l'adresse d'un restaurant !");

        System.out.println("Nouvelle rue : ");
        restaurant.getAddress().setStreet(readString());

        City newCity = pickCity(FakeItems.getCities());
        City oldCity = restaurant.getCity();

        if (newCity != null && newCity != oldCity) {
            // Synchronise les associations bidirectionnelles dans le référentiel factice
            if (oldCity != null) {
                oldCity.getRestaurants().remove(restaurant);
            }
            restaurant.setCity(newCity);
            newCity.getRestaurants().add(restaurant);
        }

        System.out.println("L'adresse a bien été modifiée ! Merci !");
    }

    /**
     * Supprime un restaurant après confirmation et nettoie les références associées
     *
     * @param restaurant restaurant à supprimer
     */
    private static void deleteRestaurant(Restaurant restaurant) {
        System.out.println("Etes-vous sûr de vouloir supprimer ce restaurant ? (O/n)");
        String choice = readString();

        if (choice.equals("o") || choice.equals("O")) {
            FakeItems.getAllRestaurants().remove(restaurant);

            City city = restaurant.getCity();
            if (city != null) {
                city.getRestaurants().remove(restaurant);
            }

            restaurant.getType().getRestaurants().remove(restaurant);

            System.out.println("Le restaurant a bien été supprimé !");
        }
    }

    /**
     * Recherche un restaurant par égalité sur le nom
     *
     * @param restaurants ensemble de restaurants
     * @param name nom recherché
     * @return restaurant trouvé ou null
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
     * Recherche une ville par égalité sur le code postal
     *
     * @param cities ensemble de villes
     * @param zipCode code postal recherché
     * @return ville trouvée ou null
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
     * Recherche un type de restaurant par égalité sur le libellé
     *
     * @param types ensemble de types
     * @param label libellé recherché
     * @return type trouvé ou null
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
     * Lit un entier depuis l’entrée standard en gérant les erreurs de saisie
     *
     * @return entier saisi
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
                // Vide le buffer afin de permettre la saisie d’une ligne complète ensuite
                scanner.nextLine();
            }
        } while (!success);

        return i;
    }

    /**
     * Lit une ligne depuis l’entrée standard
     *
     * @return chaîne saisie
     */
    private static String readString() {
        return scanner.nextLine();
    }
}
