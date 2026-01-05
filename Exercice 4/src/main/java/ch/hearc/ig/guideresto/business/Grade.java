package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

/**
 * Entité persistée représentant une note attribuée à un critère lors d'une évaluation complète
 * Mappée sur la table NOTES
 */
@Entity
@Table(name = "NOTES")
public class Grade implements IBusinessObject {

    /**
     * Identifiant technique de la note
     * Généré par la séquence SEQ_NOTES et stocké dans la colonne NUMERO
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_NOTES_GEN")
    @SequenceGenerator(
            name = "SEQ_NOTES_GEN",
            sequenceName = "SEQ_NOTES",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    /**
     * Valeur de la note
     * Stockée dans la colonne NOTE
     */
    @Column(name = "NOTE", nullable = false)
    private Integer grade;

    /**
     * Évaluation complète propriétaire de cette note
     * Association ManyToOne matérialisée par la colonne FK_COMM
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_COMM", nullable = false)
    private CompleteEvaluation evaluation;

    /**
     * Critère évalué par cette note
     * Association ManyToOne matérialisée par la colonne FK_CRIT
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_CRIT", nullable = false)
    private EvaluationCriteria criteria;

    /**
     * Constructeur sans argument requis par JPA
     */
    public Grade() {
        // Constructeur requis par JPA
    }

    /**
     * Constructeur pratique pour créer une note sans identifiant
     * L'identifiant est généré lors de la persistance
     */
    public Grade(Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this(null, grade, evaluation, criteria);
    }

    /**
     * Constructeur complet permettant d'instancier une note avec ou sans identifiant
     */
    public Grade(Integer id, Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this.id = id;
        this.grade = grade;
        this.evaluation = evaluation;
        this.criteria = criteria;
    }

    /**
     * Retourne l'identifiant technique de la note
     */
    public Integer getId() {
        return id;
    }

    /**
     * Met à jour l'identifiant technique de la note
     * À éviter en usage normal car l'identifiant est géré par la persistance
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne la valeur de la note
     */
    public Integer getGrade() {
        return grade;
    }

    /**
     * Met à jour la valeur de la note
     */
    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    /**
     * Retourne l'évaluation complète associée à cette note
     */
    public CompleteEvaluation getEvaluation() {
        return evaluation;
    }

    /**
     * Met à jour l'évaluation complète associée à cette note
     */
    public void setEvaluation(CompleteEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    /**
     * Retourne le critère évalué par cette note
     */
    public EvaluationCriteria getCriteria() {
        return criteria;
    }

    /**
     * Met à jour le critère évalué par cette note
     */
    public void setCriteria(EvaluationCriteria criteria) {
        this.criteria = criteria;
    }
}
