package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Super-classe persistée pour les différentes formes d'évaluations
 * Définit les champs communs partagés par les sous-classes et la stratégie d'héritage
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Evaluation implements IBusinessObject {

    /**
     * Identifiant technique de l'évaluation
     * Généré via une séquence dédiée
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
     * Date de la visite associée à l'évaluation
     * Persistée uniquement comme une date sans information d'heure
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_EVAL", nullable = false)
    private Date visitDate;

    /**
     * Restaurant évalué
     * Relation obligatoire, chargée en différé
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
     * Constructeur complet permettant d'initialiser les champs communs
     * L'identifiant peut rester null lorsque la base le génère
     */
    public Evaluation(Integer id, Date visitDate, Restaurant restaurant) {
        this.id = id;
        this.visitDate = visitDate;
        this.restaurant = restaurant;
    }

    /**
     * Retourne l'identifiant de l'évaluation
     */
    public Integer getId() {
        return id;
    }

    /**
     * Met à jour l'identifiant de l'évaluation
     * À éviter en usage normal si l'identifiant est généré par la base
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne la date de visite associée à l'évaluation
     */
    public Date getVisitDate() {
        return visitDate;
    }

    /**
     * Met à jour la date de visite associée à l'évaluation
     */
    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    /**
     * Retourne le restaurant évalué
     * L'accès hors transaction peut déclencher une LazyInitializationException
     */
    public Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     * Met à jour le restaurant évalué
     */
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
