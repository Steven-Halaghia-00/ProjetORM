package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Super-classe persistée pour les évaluations d’un restaurant
 * Centralise les attributs communs aux sous-classes concrètes
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Evaluation implements IBusinessObject {

    /**
     * Identifiant technique de l’évaluation
     * Généré par une séquence et stocké dans la colonne NUMERO
     *
     * Point d’attention : avec TABLE_PER_CLASS, l’usage d’une même séquence peut être délicat selon la base
     * Si ORACLE n’a pas une séquence SEQ_EVAL dédiée ou si les tables ont leurs propres séquences, il faudra aligner ce mapping sur le schéma réel
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EVAL_GEN")
    @SequenceGenerator(
            name = "SEQ_EVAL_GEN",
            sequenceName = "SEQ_EVAL",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    /**
     * Date de visite ou date de l’évaluation
     * Stockée dans la colonne DATE_EVAL, au format DATE
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_EVAL", nullable = false)
    private Date visitDate;

    /**
     * Restaurant évalué
     * Référence la clé étrangère FK_REST vers la table RESTAURANTS
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_REST", nullable = false)
    private Restaurant restaurant;

    /**
     * Constructeur sans argument requis par JPA
     */
    public Evaluation() {
        // Constructeur requis par JPA
    }

    /**
     * Constructeur complet permettant d’instancier une évaluation avec ou sans identifiant
     * L’identifiant est normalement géré par la persistance
     */
    public Evaluation(Integer id, Date visitDate, Restaurant restaurant) {
        this.id = id;
        this.visitDate = visitDate;
        this.restaurant = restaurant;
    }

    /**
     * Retourne l’identifiant technique de l’évaluation
     */
    public Integer getId() {
        return id;
    }

    /**
     * Met à jour l’identifiant technique de l’évaluation
     * À éviter en usage normal car l’identifiant est géré par la persistance
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne la date associée à l’évaluation
     */
    public Date getVisitDate() {
        return visitDate;
    }

    /**
     * Met à jour la date associée à l’évaluation
     */
    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    /**
     * Retourne le restaurant évalué
     */
    public Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     * Met à jour le restaurant évalué
     * À utiliser en veillant à maintenir la cohérence des associations côté Restaurant si nécessaire
     */
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
