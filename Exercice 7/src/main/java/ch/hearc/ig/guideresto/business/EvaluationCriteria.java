package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité JPA représentant un critère d'évaluation persisté dans la table CRITERES_EVALUATION
 *
 * Un critère est identifié fonctionnellement par son nom, contraint unique en base
 * Les notes associées à un critère sont représentées par {@link Grade}
 */
@Entity
@Table(name = "CRITERES_EVALUATION")
public class EvaluationCriteria implements IBusinessObject {

    /**
     * Identifiant technique
     * Généré via la séquence SEQ_CRITERES_EVALUATION alignée sur le schéma
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
     * Nom du critère
     * Contrainte d'unicité en base pour éviter les doublons
     */
    @Column(name = "NOM", nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Description facultative du critère
     */
    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    /**
     * Association inverse vers les notes portant ce critère
     * Côté propriétaire de la relation : {@link Grade#criteria}
     * Chargement lazy afin d'éviter de charger toutes les notes d'un critère lors d'une simple consultation
     */
    @OneToMany(mappedBy = "criteria", fetch = FetchType.LAZY)
    private Set<Grade> grades = new HashSet<>();

    /**
     * Constructeur par défaut requis par JPA
     */
    public EvaluationCriteria() {
    }

    /**
     * Constructeur utilitaire avec identifiant généré
     */
    public EvaluationCriteria(String name, String description) {
        this(null, name, description);
    }

    /**
     * Constructeur complet
     * Peut être utile en tests ou lors de manipulations hors persistance
     */
    public EvaluationCriteria(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.grades = new HashSet<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retourne l'ensemble des notes associées au critère
     * En contexte hors transaction, l'accès peut nécessiter une initialisation préalable
     */
    public Set<Grade> getGrades() {
        return grades;
    }

    public void setGrades(Set<Grade> grades) {
        this.grades = grades;
    }
}
