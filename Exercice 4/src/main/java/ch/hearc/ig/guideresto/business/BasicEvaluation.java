package ch.hearc.ig.guideresto.business;

import ch.hearc.ig.guideresto.persistence.jpa.BooleanConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;

/**
 * Entité JPA associée à la table LIKES
 * Représente une évaluation binaire (like/dislike) rattachée à un restaurant
 */
@Entity
@Table(name = "LIKES")
public class BasicEvaluation extends Evaluation {

    /**
     * Appréciation binaire du restaurant
     * Stockée en base sous forme CHAR(1) avec la convention T/F
     * Convertie côté Java en Boolean via BooleanConverter
     */
    @Convert(converter = BooleanConverter.class)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "APPRECIATION", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private Boolean likeRestaurant;

    /**
     * Adresse IP associée au vote
     * Permet d’identifier l’origine du vote dans un contexte simple
     */
    @Column(name = "ADRESSE_IP", nullable = false, length = 100)
    private String ipAddress;

    /**
     * Constructeur sans argument requis par JPA
     */
    public BasicEvaluation() {
        super();
    }

    /**
     * Constructeur utilitaire sans identifiant
     * L’identifiant est géré par la persistance
     */
    public BasicEvaluation(Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        this(null, visitDate, restaurant, likeRestaurant, ipAddress);
    }

    /**
     * Constructeur utilitaire complet
     * Permet d’instancier l’entité avec ou sans identifiant selon le contexte
     */
    public BasicEvaluation(Integer id, Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        super(id, visitDate, restaurant);
        this.likeRestaurant = likeRestaurant;
        this.ipAddress = ipAddress;
    }

    /**
     * Retourne l’appréciation binaire
     */
    public Boolean getLikeRestaurant() {
        return likeRestaurant;
    }

    /**
     * Met à jour l’appréciation binaire
     */
    public void setLikeRestaurant(Boolean likeRestaurant) {
        this.likeRestaurant = likeRestaurant;
    }

    /**
     * Retourne l’adresse IP associée à l’évaluation
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Met à jour l’adresse IP associée à l’évaluation
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
