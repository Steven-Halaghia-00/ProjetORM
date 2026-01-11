package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Super-classe des entités représentant une évaluation d'un restaurant
 *
 * Le modèle utilise un héritage JPA avec une table par sous-classe concrète
 * Les colonnes communes sont dupliquées dans chaque table de sous-classe
 *
 * Tables concernées
 * - LIKES
 * - COMMENTAIRES
 *
 * Colonnes communes présentes dans ces tables
 * - NUMERO
 * - DATE_EVAL
 * - FK_REST
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Evaluation implements IBusinessObject {

    /**
     * Identifiant technique de l'évaluation
     * Généré via la séquence SEQ_EVAL, partagée par les différentes tables d'évaluation
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
     * Date de l'évaluation
     * Le stockage est configuré au niveau DATE, sans composante horaire
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_EVAL", nullable = false)
    private Date visitDate;

    /**
     * Restaurant évalué
     *
     * Relation ManyToOne car plusieurs évaluations peuvent concerner un même restaurant
     * Chargement lazy pour éviter un chargement systématique du restaurant lors de la lecture d'une évaluation
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_REST", nullable = false)
    private Restaurant restaurant;

    /**
     * Constructeur par défaut requis par JPA
     */
    public Evaluation() {
    }

    /**
     * Constructeur utilitaire
     *
     * L'identifiant peut rester null lorsque la génération est déléguée au moteur de persistance
     */
    public Evaluation(Integer id, Date visitDate, Restaurant restaurant) {
        this.id = id;
        this.visitDate = visitDate;
        this.restaurant = restaurant;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    /**
     * Retourne le restaurant évalué
     * En contexte hors transaction, l'accès peut nécessiter une initialisation préalable
     */
    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
