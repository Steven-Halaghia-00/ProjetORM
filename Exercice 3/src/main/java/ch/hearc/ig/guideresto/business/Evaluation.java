package ch.hearc.ig.guideresto.business;

import java.util.Date;

/**
 * Modèle abstrait représentant une évaluation associée à un restaurant
 * Regroupe les attributs communs aux différentes formes d’évaluation
 */
public abstract class Evaluation implements IBusinessObject {

    /**
     * Identifiant technique de l’évaluation
     */
    private Integer id;

    /**
     * Date de visite ou date de saisie de l’évaluation
     */
    private Date visitDate;

    /**
     * Restaurant concerné par l’évaluation
     */
    private Restaurant restaurant;

    /**
     * Constructeur sans argument
     * Initialise l’instance avec des valeurs nulles
     */
    public Evaluation() {
        this(null, null, null);
    }

    /**
     * Constructeur complet
     * Permet d’instancier une évaluation avec ou sans identifiant selon le contexte
     *
     * @param id identifiant technique
     * @param visitDate date de visite ou date de l’évaluation
     * @param restaurant restaurant évalué
     */
    public Evaluation(Integer id, Date visitDate, Restaurant restaurant) {
        this.id = id;
        this.visitDate = visitDate;
        this.restaurant = restaurant;
    }

    /**
     * Retourne l’identifiant technique
     *
     * @return identifiant de l’évaluation
     */
    public Integer getId() {
        return id;
    }

    /**
     * Met à jour l’identifiant technique
     *
     * @param id nouvel identifiant
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne la date associée à l’évaluation
     *
     * @return date de visite ou date de l’évaluation
     */
    public Date getVisitDate() {
        return visitDate;
    }

    /**
     * Met à jour la date associée à l’évaluation
     *
     * @param visitDate nouvelle date
     */
    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    /**
     * Retourne le restaurant évalué
     *
     * @return restaurant associé
     */
    public Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     * Met à jour le restaurant évalué
     *
     * @param restaurant nouveau restaurant associé
     */
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

}
