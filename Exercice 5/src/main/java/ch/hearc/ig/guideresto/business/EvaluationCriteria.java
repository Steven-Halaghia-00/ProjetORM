package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un critère d'évaluation
 * Mappe la table CRITERES_EVALUATION et expose l'association inverse vers les notes
 */
@Entity
@Table(name = "CRITERES_EVALUATION")
public class EvaluationCriteria implements IBusinessObject {

    /**
     * Identifiant technique du critère
     * Généré via la séquence SEQ_CRITERES_EVALUATION
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CRITERES_EVAL_GEN")
    @SequenceGenerator(
            name = "SEQ_CRITERES_EVAL_GEN",
            sequenceName = "SEQ_CRITERES_EVALUATION",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    /**
     * Nom fonctionnel du critère
     * Contrainte d'unicité pour éviter les doublons en base
     */
    @Column(name = "NOM", nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Description textuelle du critère
     * Longueur volontairement limitée pour correspondre au schéma
     */
    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    /**
     * Association inverse vers les notes liées à ce critère
     * Relation bidirectionnelle avec Grade.criteria
     */
    @OneToMany(mappedBy = "criteria", fetch = FetchType.LAZY)
    private Set<Grade> grades = new HashSet<>();

    /**
     * Constructeur sans argument requis par JPA
     */
    public EvaluationCriteria() {
        // Constructeur requis par JPA
    }

    /**
     * Constructeur utilitaire sans identifiant
     * L'identifiant est en général généré lors de la persistance
     */
    public EvaluationCriteria(String name, String description) {
        this(null, name, description);
    }

    /**
     * Constructeur complet permettant d'initialiser tous les champs
     * Utile pour des jeux de données de test ou des cas où l'identifiant est déjà connu
     */
    public EvaluationCriteria(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.grades = new HashSet<>();
    }

    /**
     * Retourne l'identifiant du critère
     */
    public Integer getId() {
        return id;
    }

    /**
     * Met à jour l'identifiant du critère
     * À éviter en usage normal si l'identifiant est généré par la base
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne le nom du critère
     */
    public String getName() {
        return name;
    }

    /**
     * Met à jour le nom du critère
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retourne la description du critère
     */
    public String getDescription() {
        return description;
    }

    /**
     * Met à jour la description du critère
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retourne l'ensemble des notes associées à ce critère
     * Le chargement est différé, l'accès hors transaction peut déclencher une LazyInitializationException
     */
    public Set<Grade> getGrades() {
        return grades;
    }

    /**
     * Remplace l'ensemble des notes associées à ce critère
     * En relation bidirectionnelle, il est recommandé de synchroniser aussi la référence côté Grade
     */
    public void setGrades(Set<Grade> grades) {
        this.grades = (grades != null) ? grades : new HashSet<>();
    }
}
