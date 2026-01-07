package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

/**
 * Entité représentant une note attribuée à un critère dans le cadre d'une évaluation complète
 * Mappe la table NOTES et ses clés étrangères vers COMMENTAIRES et CRITERES_EVALUATION
 */
@Entity
@Table(name = "NOTES")
public class Grade implements IBusinessObject {

    /**
     * Identifiant technique de la note
     * Généré via la séquence SEQ_NOTES
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
     * Valeur de la note stockée dans la colonne NOTE
     * Correspond typiquement à une échelle métier (par exemple 1 à 5)
     */
    @Column(name = "NOTE", nullable = false)
    private Integer grade;

    /**
     * Évaluation complète à laquelle cette note appartient
     * Clé étrangère FK_COMM vers la table COMMENTAIRES
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_COMM", nullable = false)
    private CompleteEvaluation evaluation;

    /**
     * Critère évalué par cette note
     * Clé étrangère FK_CRIT vers la table CRITERES_EVALUATION
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
     * Constructeur utilitaire sans identifiant
     * L'identifiant est en général généré lors de la persistance
     */
    public Grade(Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this(null, grade, evaluation, criteria);
    }

    /**
     * Constructeur complet permettant d'initialiser tous les champs
     * Utile pour des jeux de données de test ou des cas où l'identifiant est déjà connu
     */
    public Grade(Integer id, Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this.id = id;
        this.grade = grade;
        this.evaluation = evaluation;
        this.criteria = criteria;
    }

    /**
     * Retourne l'identifiant de la note
     */
    public Integer getId() {
        return id;
    }

    /**
     * Met à jour l'identifiant de la note
     * À éviter en usage normal si l'identifiant est généré par la base
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
     * Retourne l'évaluation complète associée à la note
     */
    public CompleteEvaluation getEvaluation() {
        return evaluation;
    }

    /**
     * Met à jour l'évaluation associée à la note
     * En présence d'une relation bidirectionnelle, il est préférable d'utiliser une méthode métier côté parent
     */
    public void setEvaluation(CompleteEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    /**
     * Retourne le critère évalué
     */
    public EvaluationCriteria getCriteria() {
        return criteria;
    }

    /**
     * Met à jour le critère évalué
     */
    public void setCriteria(EvaluationCriteria criteria) {
        this.criteria = criteria;
    }
}
