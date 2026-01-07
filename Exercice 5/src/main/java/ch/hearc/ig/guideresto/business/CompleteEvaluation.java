package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité persistée représentant une évaluation complète avec commentaire et notes
 * Stockée dans la table COMMENTAIRES et liée aux notes via la table NOTES
 */
@Entity
@Table(name = "COMMENTAIRES")
public class CompleteEvaluation extends Evaluation {

    /**
     * Commentaire textuel associé à l'évaluation
     * Stocké en LOB pour accepter des textes potentiellement longs
     */
    @Lob
    @Column(name = "COMMENTAIRE", nullable = false)
    private String comment;

    /**
     * Nom d'utilisateur ayant saisi l'évaluation
     */
    @Column(name = "NOM_UTILISATEUR", nullable = false, length = 100)
    private String username;

    /**
     * Notes associées à cette évaluation
     * La relation est gérée côté Grade via le champ evaluation
     * Cascade et orphanRemoval assurent la persistance et la suppression cohérentes des notes
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
     * Constructeur utilitaire pour créer une évaluation complète sans identifiant
     * L'identifiant peut rester null lorsque la base le génère
     */
    public CompleteEvaluation(Date visitDate, Restaurant restaurant, String comment, String username) {
        this(null, visitDate, restaurant, comment, username);
    }

    /**
     * Constructeur complet permettant d'initialiser tous les champs
     */
    public CompleteEvaluation(Integer id, Date visitDate, Restaurant restaurant, String comment, String username) {
        super(id, visitDate, restaurant);
        this.comment = comment;
        this.username = username;
    }

    /**
     * Retourne le commentaire associé à l'évaluation
     */
    public String getComment() {
        return comment;
    }

    /**
     * Met à jour le commentaire associé à l'évaluation
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Retourne le nom d'utilisateur ayant saisi l'évaluation
     */
    public String getUsername() {
        return username;
    }

    /**
     * Met à jour le nom d'utilisateur ayant saisi l'évaluation
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retourne l'ensemble des notes associées
     * L'accès hors transaction peut déclencher une LazyInitializationException
     */
    public Set<Grade> getGrades() {
        return grades;
    }

    /**
     * Remplace l'ensemble des notes associées
     * Initialise une collection vide si la valeur fournie est null
     */
    public void setGrades(Set<Grade> grades) {
        this.grades = (grades != null ? grades : new HashSet<>());
    }

    /**
     * Ajoute une note à l'évaluation et maintient la cohérence bidirectionnelle
     */
    public void addGrade(Grade grade) {
        if (grade == null) {
            return;
        }
        grades.add(grade);
        grade.setEvaluation(this);
    }

    /**
     * Retire une note de l'évaluation et maintient la cohérence bidirectionnelle
     */
    public void removeGrade(Grade grade) {
        if (grade == null) {
            return;
        }
        grades.remove(grade);
        grade.setEvaluation(null);
    }
}
