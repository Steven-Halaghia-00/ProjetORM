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
 * Représente une évaluation simple d'un restaurant (like/dislike) persistée dans la table LIKES
 *
 * Cette entité hérite de {@link Evaluation} afin de factoriser les attributs communs à toutes les évaluations
 * (identifiant, date de visite/évaluation, restaurant évalué)
 */
@Entity
@Table(name = "LIKES")
public class BasicEvaluation extends Evaluation {

    /**
     * Indique si l'évaluation est positive ou négative.
     *
     * La colonne APPRECIATION est stockée en base sous forme de CHAR(1) (p. ex. 'T'/'F' ou équivalent),
     * et convertie en {@link Boolean} via {@link BooleanConverter}.
     *
     * Note : {@link JdbcTypeCode} force le type JDBC CHAR côté Hibernate pour coller au schéma
     */
    @Convert(converter = BooleanConverter.class)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "APPRECIATION", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private Boolean likeRestaurant;

    /**
     * Adresse IP associée au vote.
     * Cette information est utile pour tracer la provenance ou limiter des abus selon les règles métier
     */
    @Column(name = "ADRESSE_IP", nullable = false, length = 100)
    private String ipAddress;

    /**
     * Constructeur par défaut requis par JPA
     */
    public BasicEvaluation() {
        super();
    }

    /**
     * Constructeur utilitaire (identifiant généré).
     *
     * @param visitDate       date de l'évaluation
     * @param restaurant      restaurant évalué (doit être non null côté métier)
     * @param likeRestaurant  true pour un like, false pour un dislike
     * @param ipAddress       adresse IP de l'évaluateur
     */
    public BasicEvaluation(Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        this(null, visitDate, restaurant, likeRestaurant, ipAddress);
    }

    /**
     * Constructeur complet.
     *
     * @param id              identifiant (peut être null pour une insertion avec génération)
     * @param visitDate       date de l'évaluation
     * @param restaurant      restaurant évalué
     * @param likeRestaurant  true pour un like, false pour un dislike
     * @param ipAddress       adresse IP de l'évaluateur
     */
    public BasicEvaluation(Integer id, Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        super(id, visitDate, restaurant);
        this.likeRestaurant = likeRestaurant;
        this.ipAddress = ipAddress;
    }

    /**
     * @return true si l'évaluation est positive, false si elle est négative
     */
    public Boolean getLikeRestaurant() {
        return likeRestaurant;
    }

    public void setLikeRestaurant(Boolean likeRestaurant) {
        this.likeRestaurant = likeRestaurant;
    }

    /**
     * @return adresse IP associée au vote
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
