package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "COMMENTAIRES")
public class CompleteEvaluation extends Evaluation {

    // Commentaire textuel potentiellement long stocké en LOB
    @Lob
    @Column(name = "COMMENTAIRE", nullable = false)
    private String comment;

    // Nom d'utilisateur de l'auteur du commentaire
    @Column(name = "NOM_UTILISATEUR", nullable = false, length = 100)
    private String username;

    // Notes associées à cette évaluation complète, persistées et supprimées en cascade
    @OneToMany(
            mappedBy = "evaluation",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Grade> grades = new HashSet<>();

    // Constructeur sans argument requis par JPA
    public CompleteEvaluation() {
        // Constructeur requis par JPA
    }

    // Constructeur utilitaire sans identifiant explicite
    public CompleteEvaluation(Date visitDate, Restaurant restaurant, String comment, String username) {
        this(null, visitDate, restaurant, comment, username);
    }

    // Constructeur utilitaire avec identifiant, principalement utile pour tests ou scénarios non JPA
    public CompleteEvaluation(Integer id, Date visitDate, Restaurant restaurant, String comment, String username) {
        super(id, visitDate, restaurant);
        this.comment = comment;
        this.username = username;
    }

    // Retourne le commentaire
    public String getComment() {
        return comment;
    }

    // Définit le commentaire
    public void setComment(String comment) {
        this.comment = comment;
    }

    // Retourne le nom d'utilisateur
    public String getUsername() {
        return username;
    }

    // Définit le nom d'utilisateur
    public void setUsername(String username) {
        this.username = username;
    }

    // Retourne l'ensemble des notes associées
    public Set<Grade> getGrades() {
        return grades;
    }

    // Remplace l'ensemble des notes en garantissant une collection non nulle
    public void setGrades(Set<Grade> grades) {
        this.grades = (grades != null ? grades : new HashSet<>());
    }

    // Ajoute une note et maintient la cohérence de l'association bidirectionnelle
    public void addGrade(Grade grade) {
        if (grade == null) {
            return;
        }
        grades.add(grade);
        grade.setEvaluation(this);
    }

    // Retire une note et maintient la cohérence de l'association bidirectionnelle
    public void removeGrade(Grade grade) {
        if (grade == null) {
            return;
        }
        grades.remove(grade);
        grade.setEvaluation(null);
    }
}
