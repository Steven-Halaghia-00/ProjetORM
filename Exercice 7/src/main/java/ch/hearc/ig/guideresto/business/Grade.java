package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

/**
 * Entité JPA représentant une note persistée dans la table NOTES
 *
 * Une note relie une évaluation complète à un critère d'évaluation et porte une valeur numérique
 * Le schéma impose deux clés étrangères
 * - FK_COMM vers COMMENTAIRES
 * - FK_CRIT vers CRITERES_EVALUATION
 */
@Entity
@Table(name = "NOTES")
public class Grade implements IBusinessObject {

    /**
     * Identifiant technique
     * Généré via la séquence SEQ_NOTES alignée sur le schéma
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
     * La validation de domaine métier (plage 1..5) est généralement réalisée au niveau service ou présentation
     */
    @Column(name = "NOTE", nullable = false)
    private Integer grade;

    /**
     * Évaluation complète associée à la note
     * Côté propriétaire de la relation ManyToOne vers {@link CompleteEvaluation}
     *
     * Chargement lazy afin d'éviter de charger systématiquement l'évaluation lors de la lecture d'une note
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_COMM", nullable = false)
    private CompleteEvaluation evaluation;

    /**
     * Critère évalué par cette note
     * Côté propriétaire de la relation ManyToOne vers {@link EvaluationCriteria}
     *
     * Chargement lazy afin d'éviter de charger systématiquement le critère lors de la lecture d'une note
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_CRIT", nullable = false)
    private EvaluationCriteria criteria;

    /**
     * Constructeur par défaut requis par JPA
     */
    public Grade() {
    }

    /**
     * Constructeur utilitaire avec identifiant généré
     */
    public Grade(Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this(null, grade, evaluation, criteria);
    }

    /**
     * Constructeur complet
     * L'identifiant peut rester null lorsque la génération est déléguée au moteur de persistance
     */
    public Grade(Integer id, Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this.id = id;
        this.grade = grade;
        this.evaluation = evaluation;
        this.criteria = criteria;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    /**
     * Retourne l'évaluation complète associée
     * En contexte hors transaction, l'accès peut nécessiter une initialisation préalable
     */
    public CompleteEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(CompleteEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    /**
     * Retourne le critère évalué
     * En contexte hors transaction, l'accès peut nécessiter une initialisation préalable
     */
    public EvaluationCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(EvaluationCriteria criteria) {
        this.criteria = criteria;
    }
}
