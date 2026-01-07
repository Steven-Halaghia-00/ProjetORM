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
 * Entité persistée représentant une évaluation binaire d'un restaurant
 * Stockée dans la table LIKES et reliée au restaurant via la clé étrangère définie dans la super-classe Evaluation
 */
@Entity
@Table(name = "LIKES")
public class BasicEvaluation extends Evaluation {

    /**
     * Appréciation binaire du restaurant
     * Persistée en CHAR(1) en base avec une conversion applicative via BooleanConverter
     * Les valeurs attendues sont typiquement "T" pour vrai et "F" pour faux
     */
    @Convert(converter = BooleanConverter.class)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "APPRECIATION", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private Boolean likeRestaurant;

    /**
     * Adresse IP associée au vote
     * Permet de tracer l'origine d'une évaluation dans un contexte applicatif
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
     * Constructeur utilitaire pour créer une évaluation sans identifiant
     * L'identifiant peut rester null lorsque la base le génère
     */
    public BasicEvaluation(Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        this(null, visitDate, restaurant, likeRestaurant, ipAddress);
    }

    /**
     * Constructeur complet permettant de définir l'identifiant et les champs métier
     */
    public BasicEvaluation(Integer id, Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        super(id, visitDate, restaurant);
        this.likeRestaurant = likeRestaurant;
        this.ipAddress = ipAddress;
    }

    /**
     * Retourne l'appréciation binaire associée au restaurant
     */
    public Boolean getLikeRestaurant() {
        return likeRestaurant;
    }

    /**
     * Met à jour l'appréciation binaire associée au restaurant
     */
    public void setLikeRestaurant(Boolean likeRestaurant) {
        this.likeRestaurant = likeRestaurant;
    }

    /**
     * Retourne l'adresse IP associée à l'évaluation
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Met à jour l'adresse IP associée à l'évaluation
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
