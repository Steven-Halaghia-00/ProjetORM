package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Évaluation complète persistée dans la table COMMENTAIRES
 * Porte un commentaire, un nom d’utilisateur, et une collection de notes associées
 */
@Entity
@Table(name = "COMMENTAIRES")
public class CompleteEvaluation extends Evaluation {

    /**
     * Commentaire textuel de l’utilisateur
     * Mappé en LOB pour permettre un contenu potentiellement long
     */
    @Lob
    @Column(name = "COMMENTAIRE", nullable = false)
    private String comment;

    /**
     * Nom d’utilisateur ayant rédigé le commentaire
     */
    @Column(name = "NOM_UTILISATEUR", nullable = false, length = 100)
    private String username;

    /**
     * Notes associées à cette évaluation complète
     * Relation inverse, la clé étrangère est portée par NOTES.FK_COMM
     *
     * Cascade ALL pour propager persist, merge, remove vers les notes
     * orphanRemoval pour supprimer automatiquement les notes retirées de la collection
     * Fetch LAZY pour éviter de charger les notes tant qu’elles ne sont pas utilisées
     */
    @OneToMany(
            mappedBy = "evaluation",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Grade> grades = new HashSet<>();

    /**
     * Constructeur sans argument requis par JPA
     */
    public CompleteEvaluation() {
        // Constructeur requis par JPA
    }

    /**
     * Constructeur utilitaire sans identifiant
     * L’identifiant est géré par la persistance
     */
    public CompleteEvaluation(Date visitDate, Restaurant restaurant, String comment, String username) {
        this(null, visitDate, restaurant, comment, username);
    }

    /**
     * Constructeur complet permettant d’instancier l’entité avec ou sans identifiant
     * L’usage avec identifiant est surtout utile pour des scénarios de tests ou import
     */
    public CompleteEvaluation(Integer id, Date visitDate, Restaurant restaurant, String comment, String username) {
        super(id, visitDate, restaurant);
        this.comment = comment;
        this.username = username;
    }

    /**
     * Retourne le commentaire de l’évaluation
     */
    public String getComment() {
        return comment;
    }

    /**
     * Met à jour le commentaire de l’évaluation
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Retourne le nom d’utilisateur
     */
    public String getUsername() {
        return username;
    }

    /**
     * Met à jour le nom d’utilisateur
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retourne les notes associées
     * La collection peut être chargée à la demande selon la configuration LAZY
     */
    public Set<Grade> getGrades() {
        return grades;
    }

    /**
     * Remplace la collection des notes associées
     * Garantit une collection non nulle afin d’éviter des NullPointerException
     *
     * Point d’attention : remplacer la collection sans resynchroniser le côté propriétaire (Grade.evaluation)
     * peut laisser des références incohérentes si l’appelant ne passe pas par addGrade/removeGrade
     */
    public void setGrades(Set<Grade> grades) {
        this.grades = (grades != null ? grades : new HashSet<>());
    }

    /**
     * Ajoute une note et maintient la cohérence bidirectionnelle
     * Assure que Grade.evaluation référence bien cette instance
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
     * Détache Grade.evaluation pour refléter la suppression côté collection
     */
    public void removeGrade(Grade grade) {
        if (grade == null) {
            return;
        }
        grades.remove(grade);
        grade.setEvaluation(null);
    }
}
