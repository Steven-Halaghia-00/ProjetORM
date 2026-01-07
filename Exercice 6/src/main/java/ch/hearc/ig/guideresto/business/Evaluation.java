package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Evaluation implements IBusinessObject {

    // Identifiant technique généré, commun à toutes les sous-classes d'évaluation
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EVAL_GEN")
    @SequenceGenerator(
            name = "SEQ_EVAL_GEN",
            sequenceName = "SEQ_EVAL",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    // Date de la visite utilisée pour l'évaluation, persistée en DATE sans composante heure
    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_EVAL", nullable = false)
    private Date visitDate;

    // Restaurant associé à l'évaluation via une clé étrangère, chargé en lazy par défaut
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_REST", nullable = false)
    private Restaurant restaurant;

    // Constructeur sans argument requis par JPA
    public Evaluation() {
        // Constructeur requis par JPA
    }

    // Constructeur utilitaire pour initialiser les champs communs
    public Evaluation(Integer id, Date visitDate, Restaurant restaurant) {
        this.id = id;
        this.visitDate = visitDate;
        this.restaurant = restaurant;
    }

    // Retourne l'identifiant
    public Integer getId() {
        return id;
    }

    // Définit l'identifiant, principalement utile hors génération automatique
    public void setId(Integer id) {
        this.id = id;
    }

    // Retourne la date de visite
    public Date getVisitDate() {
        return visitDate;
    }

    // Définit la date de visite
    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    // Retourne le restaurant associé
    public Restaurant getRestaurant() {
        return restaurant;
    }

    // Définit le restaurant associé
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
