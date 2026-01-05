package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité persistée représentant un critère d’évaluation
 * Mappée sur la table CRITERES_EVALUATION
 */
@Entity
@Table(name = "CRITERES_EVALUATION")
public class EvaluationCriteria implements IBusinessObject {

    /**
     * Identifiant technique du critère
     * Généré par la séquence SEQ_CRITERES_EVALUATION et stocké dans la colonne NUMERO
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
     * Nom métier du critère
     * Stocké dans la colonne NOM, contraint à être unique
     */
    @Column(name = "NOM", nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Description libre du critère
     * Stockée dans la colonne DESCRIPTION
     */
    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    /**
     * Ensemble des notes associées à ce critère
     * Association inverse OneToMany basée sur la propriété criteria côté Grade
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
     * Constructeur pratique pour créer un critère sans identifiant
     * L'identifiant est généré lors de la persistance
     */
    public EvaluationCriteria(String name, String description) {
        this(null, name, description);
    }

    /**
     * Constructeur complet permettant d'instancier un critère avec ou sans identifiant
     */
    public EvaluationCriteria(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.grades = new HashSet<>();
    }

    /**
     * Retourne l'identifiant technique du critère
     */
    public Integer getId() {
        return id;
    }

    /**
     * Met à jour l'identifiant technique du critère
     * À éviter en usage normal car l'identifiant est géré par la persistance
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
     * Retourne la liste des notes associées à ce critère
     */
    public Set<Grade> getGrades() {
        return grades;
    }

    /**
     * Remplace l'ensemble des notes associées à ce critère
     */
    public void setGrades(Set<Grade> grades) {
        this.grades = grades;
    }
}
