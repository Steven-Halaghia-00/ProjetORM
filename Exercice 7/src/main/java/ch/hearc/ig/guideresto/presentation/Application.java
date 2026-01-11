package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.services.ConcurrentModificationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import ch.hearc.ig.guideresto.services.*;

/**
 * Point d'entrée et couche de présentation console
 * Responsable de l'interaction utilisateur, de l'affichage et de la navigation dans les menus
 * Les opérations métiers et la gestion transactionnelle sont déléguées à la couche de services
 *
 * @author cedric.baudet
 * @author alain.matile
 */
public class Application {

    /*
        Services applicatifs
        Centralisent la logique métier et la gestion des transactions
     */
    private static final RestaurantService restaurantService = new RestaurantService();
    private static final CityService cityService = new CityService();
    private static final RestaurantTypeService restaurantTypeService = new RestaurantTypeService();
    private static final EvaluationCriteriaService criteriaService = new EvaluationCriteriaService();
    private static final EvaluationService evaluationService = new EvaluationService();
    private static final BootstrapService bootstrapService = new BootstrapService();

    /*
        Ressources de présentation
        Scanner pour la saisie console et logger pour les traces techniques
     */
    private static Scanner scanner;
    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        // Initialisation et démonstration de démarrage
        // Les transactions nécessaires sont gérées côté service
        bootstrapService.runOrmBootstrapAndBasicCrudDemo(logger);

        System.out.println("Bienvenue dans GuideResto ! Que souhaitez-vous faire ?");
        int choice;

        try {
            do {
                printMainMenu();
                choice = readInt();
                proceedMainMenu(choice);
            } while (choice != 0);
        } finally {
            // Libération des ressources JPA en fin d'application
            // Fermeture de l'EntityManagerFactory partagée
            JpaUtils.close();

            // Libération de la ressource d'entrée standard
            scanner.close();
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
     * Route le choix utilisateur vers l'action correspondante
     *
     * @param choice choix saisi dans le menu principal
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
     * Affiche une liste de restaurants et permet d'en sélectionner un par son nom exact
     *
     * @param restaurants restaurants à afficher
     * @return restaurant sélectionné ou null si aucun choix
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
     * Affiche la liste complète des restaurants puis ouvre la fiche d'un restaurant sélectionné
     */
    private static void showRestaurantsList() {
        System.out.println("Liste des restaurants : ");

        Restaurant restaurant = pickRestaurant(loadAllRestaurants());
        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Recherche des restaurants par sous-chaîne sur le nom puis ouvre la fiche d'un restaurant sélectionné
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
     * Recherche des restaurants par sous-chaîne sur le nom de ville puis ouvre la fiche d'un restaurant sélectionné
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
     * Permet de sélectionner une ville existante par NPA ou d'en créer une nouvelle
     *
     * @param cities villes disponibles
     * @return ville sélectionnée ou créée, ou null si aucun choix valide
     */
    private static City pickCity(Set<City> cities) {
        System.out.println("Voici la liste des villes possibles, veuillez entrer le NPA de la ville désirée : ");

        for (City currentCity : cities) {
            System.out.println(currentCity.getZipCode() + " " + currentCity.getCityName());
        }
        System.out.println("Entrez \"NEW\" pour créer une nouvelle ville");
        String choice = readString();

        if (choice.equals("NEW")) {
            System.out.println("Veuillez entrer le NPA de la nouvelle ville : ");
            String zip = readString();
            System.out.println("Veuillez entrer le nom de la nouvelle ville : ");
            String name = readString();
            return cityService.createCity(zip, name);
        }

        return searchCityByZipCode(cities, choice);
    }

    /**
     * Permet de sélectionner un type de restaurant existant par libellé
     *
     * @param types types disponibles
     * @return type sélectionné ou null si aucun choix valide
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
     * Recherche de restaurants par type sélectionné puis ouverture de la fiche d'un restaurant
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
     * Saisie console des champs et création d'un restaurant
     * La création est déléguée au service qui gère la transaction
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

        Restaurant created = restaurantService.createRestaurantWithRefs(
                name,
                street,
                description,
                website,
                selectedCity.getId(),
                selectedType.getId()
        );

        showRestaurant(created);
    }

    /**
     * Affiche la fiche détaillée d'un restaurant et son menu d'actions
     * Le restaurant est rechargé à chaque itération afin d'afficher des données à jour
     *
     * @param restaurant restaurant à afficher
     */
    private static void showRestaurant(Restaurant restaurant) {

        int choice;
        do {
            // Rechargement systématique pour obtenir la version courante et les associations nécessaires
            restaurant = restaurantService.loadRestaurantForDisplay(restaurant.getId());

            System.out.println("Affichage d'un restaurant : ");
            StringBuilder sb = new StringBuilder();
            sb.append(restaurant.getName()).append("\n");
            sb.append("Version: ").append(restaurant.getVersion()).append("\n");
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

            showRestaurantMenu();
            choice = readInt();

            // Passage de l'instance rechargée pour disposer de la version courante
            proceedRestaurantMenu(choice, restaurant);

        } while (choice != 0 && choice != 6);
    }

    /**
     * Compte les évaluations basiques en fonction de la valeur likeRestaurant
     *
     * @param evaluations ensemble d'évaluations
     * @param likeRestaurant filtre like ou dislike
     * @return nombre d'évaluations correspondantes
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
     * Construit le texte d'une évaluation complète
     *
     * @param eval évaluation à décrire
     * @return description de l'évaluation complète ou chaîne vide si non applicable
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
     * Affiche le menu d'actions disponible sur la fiche restaurant
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
     * Route le choix utilisateur du menu restaurant vers l'action correspondante
     *
     * @param choice choix saisi
     * @param restaurant restaurant courant
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
     * Ajoute un like ou un dislike au restaurant
     * L'adresse IP locale est utilisée pour tracer la provenance de l'évaluation
     *
     * @param restaurant restaurant évalué
     * @param like true pour like, false pour dislike
     */
    private static void addBasicEvaluation(Restaurant restaurant, Boolean like) {
        String ipAddress;
        try {
            ipAddress = Inet4Address.getLocalHost().toString();
        } catch (UnknownHostException ex) {
            logger.error("Error - Couldn't retrieve host IP address");
            ipAddress = "Indisponible";
        }

        evaluationService.addBasicEvaluation(restaurant.getId(), like, new Date(), ipAddress);
        System.out.println("Votre vote a été pris en compte !");
    }

    /**
     * Saisie d'une évaluation complète et création associée en base
     *
     * @param restaurant restaurant évalué
     */
    private static void evaluateRestaurant(Restaurant restaurant) {
        System.out.println("Merci d'évaluer ce restaurant !");
        System.out.println("Quel est votre nom d'utilisateur ? ");
        String username = readString();

        System.out.println("Quel commentaire aimeriez-vous publier ?");
        String comment = readString();

        List<EvaluationService.GradeInput> inputs = new ArrayList<>();

        System.out.println("Veuillez svp donner une note entre 1 et 5 pour chacun de ces critères : ");
        for (EvaluationCriteria c : loadAllEvaluationCriterias()) {
            System.out.println(c.getName() + " : " + c.getDescription());
            int note = readInt();
            inputs.add(new EvaluationService.GradeInput(c.getId(), note));
        }

        evaluationService.addCompleteEvaluation(restaurant.getId(), new Date(), comment, username, inputs);
        System.out.println("Votre évaluation a bien été enregistrée, merci !");
    }

    /**
     * Modification des informations générales d'un restaurant
     * Le contrôle de concurrence est effectué via une version attendue
     *
     * @param restaurant restaurant à modifier
     */
    private static void editRestaurant(Restaurant restaurant) {
        System.out.println("Edition d'un restaurant !");

        int expectedVersion = restaurant.getVersion();

        System.out.println("Nouveau nom : ");
        String newName = readString();

        System.out.println("Nouvelle description : ");
        String newDescription = readString();

        System.out.println("Nouveau site web : ");
        String newWebsite = readString();

        System.out.println("Nouveau type de restaurant : ");
        RestaurantType chosenType = pickRestaurantType(loadAllRestaurantTypes());
        Integer chosenTypeId = (chosenType != null ? chosenType.getId() : null);

        try {
            restaurantService.updateRestaurantDetails(
                    restaurant.getId(),
                    expectedVersion,
                    newName,
                    newDescription,
                    newWebsite,
                    chosenTypeId
            );
            System.out.println("Le restaurant a bien été modifié.");
        } catch (ConcurrentModificationException ex) {
            System.out.println("\n*** " + ex.getMessage() + " ***\n");
            System.out.println("Appuie sur Entrée pour recharger...");
            readString();
        }
    }

    /**
     * Modification de l'adresse d'un restaurant
     * Le contrôle de concurrence est effectué via une version attendue
     *
     * @param restaurant restaurant à modifier
     */
    private static void editRestaurantAddress(Restaurant restaurant) {
        System.out.println("Edition de l'adresse d'un restaurant !");

        int expectedVersion = restaurant.getVersion();

        System.out.println("Nouvelle rue : ");
        String newStreet = readString();

        City selectedCity = pickCity(loadAllCities());
        if (selectedCity == null) {
            return;
        }

        try {
            restaurantService.updateRestaurantAddress(
                    restaurant.getId(),
                    expectedVersion,
                    newStreet,
                    selectedCity.getId()
            );
            System.out.println("L'adresse a bien été modifiée.");
        } catch (ConcurrentModificationException ex) {
            System.out.println("\n*** " + ex.getMessage() + " ***\n");
            System.out.println("Appuie sur Entrée pour recharger...");
            readString();
        }
    }

    /**
     * Suppression d'un restaurant après confirmation
     * Le contrôle de concurrence est effectué via une version attendue
     *
     * @param restaurant restaurant à supprimer
     */
    private static void deleteRestaurant(Restaurant restaurant) {
        System.out.println("Etes-vous sûr de vouloir supprimer ce restaurant ? (O/n)");
        String choice = readString();

        if (!choice.equalsIgnoreCase("o")) {
            return;
        }

        int expectedVersion = restaurant.getVersion();

        try {
            restaurantService.deleteRestaurant(restaurant.getId(), expectedVersion);
            System.out.println("Le restaurant a bien été supprimé.");
        } catch (ConcurrentModificationException ex) {
            System.out.println("\n*** " + ex.getMessage() + " ***\n");
            System.out.println("Appuie sur Entrée pour recharger...");
            readString();
        }
    }

    /**
     * Recherche un restaurant par nom exact dans un ensemble
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
     * Recherche une ville par NPA dans un ensemble
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
     * Recherche un type par libellé exact dans un ensemble
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
     * Lecture robuste d'un entier depuis la console
     * Réinitialise la lecture de ligne après un nextInt
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
                scanner.nextLine();
            }
        } while (!success);

        return i;
    }

    /**
     * Lecture d'une ligne complète depuis la console
     *
     * @return texte saisi
     */
    private static String readString() {
        return scanner.nextLine();
    }

    /**
     * Charge les restaurants depuis la couche de services
     *
     * @return ensemble ordonné de restaurants
     */
    private static Set<Restaurant> loadAllRestaurants() {
        return new LinkedHashSet<>(restaurantService.findAll());
    }

    /**
     * Charge les villes depuis la couche de services
     *
     * @return ensemble ordonné de villes
     */
    private static Set<City> loadAllCities() {
        return new LinkedHashSet<>(cityService.findAll());
    }

    /**
     * Charge les types de restaurants depuis la couche de services
     *
     * @return ensemble ordonné de types
     */
    private static Set<RestaurantType> loadAllRestaurantTypes() {
        return new LinkedHashSet<>(restaurantTypeService.findAll());
    }

    /**
     * Charge les critères d'évaluation depuis la couche de services
     *
     * @return ensemble ordonné de critères
     */
    private static Set<EvaluationCriteria> loadAllEvaluationCriterias() {
        return new LinkedHashSet<>(criteriaService.findAll());
    }
}
