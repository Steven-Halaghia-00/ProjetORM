package ch.hearc.ig.guideresto.business;

/**
 * Représente une note attribuée à un critère dans le cadre d'une évaluation complète
 * L'instance référence l'évaluation parente et le critère évalué
 */
public class Grade implements IBusinessObject {

    /**
     * Identifiant technique de la note
     */
    private Integer id;

    /**
     * Valeur numérique de la note
     */
    private Integer grade;

    /**
     * Évaluation complète à laquelle la note est rattachée
     */
    private CompleteEvaluation evaluation;

    /**
     * Critère sur lequel porte la note
     */
    private EvaluationCriteria criteria;

    /**
     * Constructeur sans argument
     * Initialise l'instance avec des valeurs nulles
     */
    public Grade() {
        this(null, null, null);
    }

    /**
     * Constructeur utilitaire sans identifiant
     * Permet de créer une note en fournissant la valeur et les références métier
     *
     * @param grade valeur de la note
     * @param evaluation évaluation complète associée
     * @param criteria critère évalué
     */
    public Grade(Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this(null, grade, evaluation, criteria);
    }

    /**
     * Constructeur complet
     * Permet d'instancier une note avec ou sans identifiant selon le contexte
     *
     * @param id identifiant technique
     * @param grade valeur de la note
     * @param evaluation évaluation complète associée
     * @param criteria critère évalué
     */
    public Grade(Integer id, Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this.id = id;
        this.grade = grade;
        this.evaluation = evaluation;
        this.criteria = criteria;
    }

    /**
     * Retourne l'identifiant technique
     *
     * @return identifiant de la note
     */
    public Integer getId() {
        return id;
    }

    /**
     * Met à jour l'identifiant technique
     *
     * @param id nouvel identifiant
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne la valeur de la note
     *
     * @return valeur numérique de la note
     */
    public Integer getGrade() {
        return grade;
    }

    /**
     * Met à jour la valeur de la note
     *
     * @param grade nouvelle valeur
     */
    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    /**
     * Retourne l'évaluation complète associée
     *
     * @return évaluation parente
     */
    public CompleteEvaluation getEvaluation() {
        return evaluation;
    }

    /**
     * Met à jour l'évaluation complète associée
     *
     * @param evaluation nouvelle évaluation parente
     */
    public void setEvaluation(CompleteEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    /**
     * Retourne le critère évalué
     *
     * @return critère associé
     */
    public EvaluationCriteria getCriteria() {
        return criteria;
    }

    /**
     * Met à jour le critère évalué
     *
     * @param criteria nouveau critère
     */
    public void setCriteria(EvaluationCriteria criteria) {
        this.criteria = criteria;
    }
}
