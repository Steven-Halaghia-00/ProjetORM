package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import ch.hearc.ig.guideresto.services.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Point d’entrée console de l’application GuideResto
 * Gère l’interface utilisateur et délègue la logique métier aux services
 *
 * @author cedric.baudet
 * @author alain.matile
 */
public class Application {

    /**
     * Services applicatifs utilisés par la couche présentation
     * Les services encapsulent les transactions et l’accès aux données
     */
    private static final RestaurantService restaurantService = new RestaurantService();
    private static final CityService cityService = new CityService();
    private static final RestaurantTypeService restaurantTypeService = new RestaurantTypeService();
    private static final EvaluationCriteriaService criteriaService = new EvaluationCriteriaService();
    private static final EvaluationService evaluationService = new EvaluationService();
    private static final BootstrapService bootstrapService = new BootstrapService();

    /**
     * Scanner unique pour toutes les saisies console
     */
    private static Scanner scanner;

    /**
     * Logger applicatif
     */
    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        // Initialisation technique et vérifications de base gérées par le service
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
            // Libération des ressources JPA au moment de quitter l’application
            JpaUtils.close();
            scanner.close();
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
     * Exécute l’action associée au choix saisi dans le menu principal
     *
     * @param choice nombre attendu entre 0 et 5
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
     * Affiche une liste de restaurants et laisse l’utilisateur en sélectionner un par son nom exact
     *
     * @param restaurants liste à afficher
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
     * Charge et affiche la liste de tous les restaurants
     */
    private static void showRestaurantsList() {
        System.out.println("Liste des restaurants : ");

        Restaurant restaurant = pickRestaurant(loadAllRestaurants());

        // Si l’utilisateur choisit un restaurant, on affiche le détail, sinon retour au menu principal
        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Recherche des restaurants dont le nom contient une chaîne saisie par l’utilisateur
     * Le filtrage est fait en mémoire après chargement de la liste
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
     * Recherche des restaurants dont le nom de la ville contient une chaîne saisie par l’utilisateur
     * Le filtrage est fait en mémoire après chargement de la liste
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
     * Propose à l’utilisateur de choisir une ville existante via son NPA ou de créer une nouvelle ville
     *
     * @param cities liste des villes disponibles
     * @return ville sélectionnée ou créée, ou null si aucune sélection
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
     * Propose à l’utilisateur de choisir un type de restaurant via son libellé exact
     *
     * @param types liste des types disponibles
     * @return type sélectionné ou null si non trouvé
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
     * Recherche des restaurants en fonction du type choisi par l’utilisateur
     * Le filtrage est fait en mémoire après chargement de la liste
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
     * Demande les informations nécessaires puis crée un nouveau restaurant via le service
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

        // Création et rattachement via identifiants pour limiter le couplage à l’état des entités côté présentation
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
     * Affiche le détail d’un restaurant et propose le menu d’actions associé
     *
     * @param restaurant restaurant à afficher
     */
    private static void showRestaurant(Restaurant restaurant) {
        // Rechargement complet pour garantir l’accès aux associations nécessaires à l’affichage
        restaurant = restaurantService.loadRestaurantForDisplay(restaurant.getId());

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
     * Compte les évaluations binaires positives ou négatives selon le paramètre likeRestaurant
     *
     * @param evaluations ensemble des évaluations
     * @param likeRestaurant true pour likes, false pour dislikes
     * @return nombre d’évaluations correspondantes
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
     * Construit le texte détaillé d’une évaluation complète
     * Retourne null si l’évaluation est binaire
     *
     * @param eval évaluation à décrire
     * @return description multi-lignes ou null
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
     * Affiche le menu des actions disponibles sur un restaurant
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
     * Exécute l’action associée au choix saisi dans le menu restaurant
     *
     * @param choice numéro d’action attendu entre 0 et 6
     * @param restaurant restaurant concerné
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
            default:
                break;
        }
    }

    /**
     * Ajoute une évaluation binaire au restaurant
     * L’adresse IP locale est utilisée pour un contexte console
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
     * Crée une évaluation complète avec commentaire et notes
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
     * Met à jour les informations principales d’un restaurant
     *
     * @param restaurant restaurant à modifier
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

        restaurantService.updateRestaurantDetails(
                restaurant.getId(),
                newName,
                newDescription,
                newWebsite,
                chosenTypeId
        );

        System.out.println("Le restaurant a bien été modifié.");
    }

    /**
     * Met à jour l’adresse d’un restaurant et sa ville
     *
     * @param restaurant restaurant à modifier
     */
    private static void editRestaurantAddress(Restaurant restaurant) {
        System.out.println("Edition de l'adresse d'un restaurant !");

        System.out.println("Nouvelle rue : ");
        String newStreet = readString();

        City selectedCity = pickCity(loadAllCities());
        if (selectedCity == null) {
            return;
        }

        restaurantService.updateRestaurantAddress(
                restaurant.getId(),
                newStreet,
                selectedCity.getId()
        );

        System.out.println("L'adresse a bien été modifiée.");
    }

    /**
     * Supprime un restaurant après confirmation utilisateur
     *
     * @param restaurant restaurant à supprimer
     */
    private static void deleteRestaurant(Restaurant restaurant) {
        System.out.println("Etes-vous sûr de vouloir supprimer ce restaurant ? (O/n)");
        String choice = readString();

        if (!choice.equalsIgnoreCase("o")) {
            return;
        }

        restaurantService.deleteRestaurant(restaurant.getId());
        System.out.println("Le restaurant a bien été supprimé.");
    }

    /**
     * Recherche un restaurant par nom exact dans un ensemble donné
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
     * Recherche une ville par code postal exact dans un ensemble donné
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
     * Recherche un type par libellé exact dans un ensemble donné
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
     * Lit un entier au clavier avec gestion des erreurs de saisie
     * Le scanner est repositionné en fin de ligne après lecture
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
     * Lit une chaîne de caractères au clavier
     *
     * @return chaîne saisie
     */
    private static String readString() {
        return scanner.nextLine();
    }

    /**
     * Charge tous les restaurants via le service
     *
     * @return ensemble de restaurants
     */
    private static Set<Restaurant> loadAllRestaurants() {
        return new LinkedHashSet<>(restaurantService.findAll());
    }

    /**
     * Charge toutes les villes via le service
     *
     * @return ensemble de villes
     */
    private static Set<City> loadAllCities() {
        return new LinkedHashSet<>(cityService.findAll());
    }

    /**
     * Charge tous les types de restaurant via le service
     *
     * @return ensemble de types
     */
    private static Set<RestaurantType> loadAllRestaurantTypes() {
        return new LinkedHashSet<>(restaurantTypeService.findAll());
    }

    /**
     * Charge tous les critères d’évaluation via le service
     *
     * @return ensemble de critères
     */
    private static Set<EvaluationCriteria> loadAllEvaluationCriterias() {
        return new LinkedHashSet<>(criteriaService.findAll());
    }
}
