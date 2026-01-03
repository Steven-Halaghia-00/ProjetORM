package ch.hearc.ig.guideresto.business;

import java.util.Date;

/**
 * Évaluation simple de type binaire (like / dislike) associée à un restaurant
 * Cette classe contient uniquement les informations nécessaires à une appréciation rapide
 */
public class BasicEvaluation extends Evaluation {

    /**
     * Indique si le restaurant est apprécié
     * true pour un like, false pour un dislike
     */
    private Boolean likeRestaurant;

    /**
     * Adresse IP associée à l’évaluation
     * Sert à tracer l’origine d’un vote dans un contexte console
     */
    private String ipAddress;

    /**
     * Constructeur sans argument
     * Permet l’instanciation par défaut et la compatibilité avec certains frameworks
     */
    public BasicEvaluation() {
        this(null, null, null, null);
    }

    /**
     * Constructeur utilitaire sans identifiant
     * L’identifiant peut être généré ultérieurement selon le contexte de persistance
     *
     * @param visitDate date de visite
     * @param restaurant restaurant évalué
     * @param likeRestaurant appréciation binaire
     * @param ipAddress adresse IP associée
     */
    public BasicEvaluation(Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        this(null, visitDate, restaurant, likeRestaurant, ipAddress);
    }

    /**
     * Constructeur complet
     * Permet d’instancier une évaluation avec ou sans identifiant selon le contexte
     *
     * @param id identifiant technique
     * @param visitDate date de visite
     * @param restaurant restaurant évalué
     * @param likeRestaurant appréciation binaire
     * @param ipAddress adresse IP associée
     */
    public BasicEvaluation(Integer id, Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        super(id, visitDate, restaurant);
        this.likeRestaurant = likeRestaurant;
        this.ipAddress = ipAddress;
    }

    /**
     * Retourne l’appréciation binaire associée au restaurant
     *
     * @return true si like, false si dislike, null si non renseigné
     */
    public Boolean getLikeRestaurant() {
        return likeRestaurant;
    }

    /**
     * Met à jour l’appréciation binaire associée au restaurant
     *
     * @param likeRestaurant appréciation binaire
     */
    public void setLikeRestaurant(Boolean likeRestaurant) {
        this.likeRestaurant = likeRestaurant;
    }

    /**
     * Retourne l’adresse IP associée à l’évaluation
     *
     * @return adresse IP
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Met à jour l’adresse IP associée à l’évaluation
     *
     * @param ipAddress adresse IP
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
