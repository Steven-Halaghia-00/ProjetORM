package ch.hearc.ig.guideresto.business;

/**
 * Représente un critère d’évaluation pouvant être noté lors d’une évaluation complète
 * Le critère porte un nom unique côté métier et une description associée
 */
public class EvaluationCriteria implements IBusinessObject {

    /**
     * Identifiant technique du critère
     */
    private Integer id;

    /**
     * Libellé du critère
     */
    private String name;

    /**
     * Description fonctionnelle du critère
     */
    private String description;

    /**
     * Constructeur sans argument
     * Initialise l'instance avec des valeurs nulles
     */
    public EvaluationCriteria() {
        this(null, null);
    }

    /**
     * Constructeur utilitaire sans identifiant
     * Permet de créer un critère à partir de ses informations métier
     *
     * @param name libellé du critère
     * @param description description du critère
     */
    public EvaluationCriteria(String name, String description) {
        this(null, name, description);
    }

    /**
     * Constructeur complet
     * Permet d'instancier un critère avec ou sans identifiant selon le contexte
     *
     * @param id identifiant technique
     * @param name libellé du critère
     * @param description description du critère
     */
    public EvaluationCriteria(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Retourne l'identifiant technique
     *
     * @return identifiant du critère
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
     * Retourne le libellé du critère
     *
     * @return libellé du critère
     */
    public String getName() {
        return name;
    }

    /**
     * Met à jour le libellé du critère
     *
     * @param name nouveau libellé
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retourne la description du critère
     *
     * @return description fonctionnelle
     */
    public String getDescription() {
        return description;
    }

    /**
     * Met à jour la description du critère
     *
     * @param description nouvelle description
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
