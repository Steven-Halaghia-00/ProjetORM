package ch.hearc.ig.guideresto.business;

import ch.hearc.ig.guideresto.persistence.jpa.BooleanConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;

@Entity
@Table(name = "LIKES")
public class BasicEvaluation extends Evaluation {

    // Mappe la colonne CHAR(1) APPRECIATION vers un Boolean via un converter applicatif
    @Convert(converter = BooleanConverter.class)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "APPRECIATION", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private Boolean likeRestaurant;

    // Stocke l'adresse IP associée au vote, utile pour la traçabilité et limiter d'éventuels abus
    @Column(name = "ADRESSE_IP", nullable = false, length = 100)
    private String ipAddress;

    // Constructeur sans argument requis par JPA
    public BasicEvaluation() {
        super();
    }

    // Constructeur utilitaire pour créer une évaluation sans identifiant explicite
    public BasicEvaluation(Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        this(null, visitDate, restaurant, likeRestaurant, ipAddress);
    }

    // Constructeur complet, l'identifiant est normalement généré côté base
    public BasicEvaluation(Integer id, Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        super(id, visitDate, restaurant);
        this.likeRestaurant = likeRestaurant;
        this.ipAddress = ipAddress;
    }

    // Retourne l'appréciation sous forme booléenne
    public Boolean getLikeRestaurant() {
        return likeRestaurant;
    }

    // Définit l'appréciation du restaurant
    public void setLikeRestaurant(Boolean likeRestaurant) {
        this.likeRestaurant = likeRestaurant;
    }

    // Retourne l'adresse IP associée à l'évaluation
    public String getIpAddress() {
        return ipAddress;
    }

    // Définit l'adresse IP associée à l'évaluation
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
