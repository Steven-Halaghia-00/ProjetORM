package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité JPA représentant une évaluation complète persistée dans la table COMMENTAIRES
 *
 * Cette évaluation complète contient un commentaire, un nom d'utilisateur et un ensemble de notes
 * Les notes sont représentées par {@link Grade} et sont persistées dans la table NOTES
 *
 * La relation avec {@link Grade} est configurée en cascade afin de garantir une création et une suppression atomiques
 */
@Entity
@Table(name = "COMMENTAIRES")
public class CompleteEvaluation extends Evaluation {

    /**
     * Commentaire textuel associé à l'évaluation
     * Stocké en CLOB côté base
     */
    @Lob
    @Column(name = "COMMENTAIRE", nullable = false)
    private String comment;

    /**
     * Nom d'utilisateur ayant soumis l'évaluation
     */
    @Column(name = "NOM_UTILISATEUR", nullable = false, length = 100)
    private String username;

    /**
     * Notes associées à l'évaluation
     *
     * mappedBy indique que {@link Grade} porte la clé étrangère via l'attribut evaluation
     * cascade ALL permet de persister et supprimer les notes via l'évaluation
     * orphanRemoval supprime les notes retirées de la collection côté modèle
     *
     * Chargement lazy pour éviter de charger toutes les notes lors d'une simple consultation
     */
    @OneToMany(
            mappedBy = "evaluation",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Grade> grades = new HashSet<>();

    /**
     * Constructeur par défaut requis par JPA
     */
    public CompleteEvaluation() {
    }

    /**
     * Constructeur utilitaire avec identifiant généré
     */
    public CompleteEvaluation(Date visitDate, Restaurant restaurant, String comment, String username) {
        this(null, visitDate, restaurant, comment, username);
    }

    /**
     * Constructeur complet
     *
     * Le lien vers le restaurant est initialisé via la classe mère {@link Evaluation}
     */
    public CompleteEvaluation(Integer id, Date visitDate, Restaurant restaurant, String comment, String username) {
        super(id, visitDate, restaurant);
        this.comment = comment;
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retourne l'ensemble des notes associées
     * En contexte hors transaction, l'accès peut nécessiter une initialisation préalable
     */
    public Set<Grade> getGrades() {
        return grades;
    }

    /**
     * Remplace l'ensemble des notes
     * La valeur null est normalisée en ensemble vide pour éviter les NullPointerException
     */
    public void setGrades(Set<Grade> grades) {
        this.grades = (grades != null ? grades : new HashSet<>());
    }

    /**
     * Ajoute une note et maintient la cohérence bidirectionnelle
     *
     * L'attribut {@link Grade#evaluation} est positionné afin de garantir une clé étrangère correcte en base
     */
    public void addGrade(Grade grade) {
        if (grade == null) {
            return;
        }
        grades.add(grade);
        grade.setEvaluation(this);
    }

    /**
     * Retire une note et maintient la cohérence bidirectionnelle
     *
     * orphanRemoval étant activé, retirer une note de la collection entraîne sa suppression en base lors du flush
     */
    public void removeGrade(Grade grade) {
        if (grade == null) {
            return;
        }
        grades.remove(grade);
        grade.setEvaluation(null);
    }
}
