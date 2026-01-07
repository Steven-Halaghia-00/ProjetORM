package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

@Entity
@Table(name = "NOTES")
public class Grade implements IBusinessObject {

    // Identifiant technique généré pour la note
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_NOTES_GEN")
    @SequenceGenerator(
            name = "SEQ_NOTES_GEN",
            sequenceName = "SEQ_NOTES",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    // Valeur de la note attribuée au critère
    @Column(name = "NOTE", nullable = false)
    private Integer grade;

    // Référence vers l'évaluation complète à laquelle la note appartient
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_COMM", nullable = false)
    private CompleteEvaluation evaluation;

    // Référence vers le critère évalué par cette note
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_CRIT", nullable = false)
    private EvaluationCriteria criteria;

    // Constructeur sans argument requis par JPA
    public Grade() {
        // Constructeur requis par JPA
    }

    // Constructeur utilitaire pour créer une note sans fournir d'identifiant
    public Grade(Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this(null, grade, evaluation, criteria);
    }

    // Constructeur utilitaire complet, l'identifiant peut rester null si généré par JPA
    public Grade(Integer id, Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this.id = id;
        this.grade = grade;
        this.evaluation = evaluation;
        this.criteria = criteria;
    }

    // Retourne l'identifiant de la note
    public Integer getId() {
        return id;
    }

    // Définit l'identifiant, principalement utile hors génération automatique
    public void setId(Integer id) {
        this.id = id;
    }

    // Retourne la valeur de la note
    public Integer getGrade() {
        return grade;
    }

    // Définit la valeur de la note
    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    // Retourne l'évaluation complète associée
    public CompleteEvaluation getEvaluation() {
        return evaluation;
    }

    // Définit l'évaluation complète associée
    public void setEvaluation(CompleteEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    // Retourne le critère associé
    public EvaluationCriteria getCriteria() {
        return criteria;
    }

    // Définit le critère associé
    public void setCriteria(EvaluationCriteria criteria) {
        this.criteria = criteria;
    }
}
