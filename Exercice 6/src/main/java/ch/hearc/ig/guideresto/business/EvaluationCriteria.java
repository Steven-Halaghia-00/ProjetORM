package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "CRITERES_EVALUATION")
public class EvaluationCriteria implements IBusinessObject {

    // Identifiant technique généré pour le critère d'évaluation
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CRITERES_EVAL_GEN")
    @SequenceGenerator(
            name = "SEQ_CRITERES_EVAL_GEN",
            sequenceName = "SEQ_CRITERES_EVALUATION",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    // Libellé du critère, unique pour éviter les doublons fonctionnels
    @Column(name = "NOM", nullable = false, length = 100, unique = true)
    private String name;

    // Description optionnelle du critère, utilisée pour guider la saisie
    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    // Association inverse vers les notes rattachées à ce critère, chargée à la demande
    @OneToMany(mappedBy = "criteria", fetch = FetchType.LAZY)
    private Set<Grade> grades = new HashSet<>();

    // Constructeur sans argument requis par JPA
    public EvaluationCriteria() {
        // Constructeur requis par JPA
    }

    // Constructeur utilitaire pour créer un critère sans fournir d'identifiant
    public EvaluationCriteria(String name, String description) {
        this(null, name, description);
    }

    // Constructeur utilitaire complet, l'identifiant peut rester null si généré par JPA
    public EvaluationCriteria(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.grades = new HashSet<>();
    }

    // Retourne l'identifiant du critère
    public Integer getId() {
        return id;
    }

    // Définit l'identifiant, principalement utile hors génération automatique
    public void setId(Integer id) {
        this.id = id;
    }

    // Retourne le nom du critère
    public String getName() {
        return name;
    }

    // Définit le nom du critère
    public void setName(String name) {
        this.name = name;
    }

    // Retourne la description du critère
    public String getDescription() {
        return description;
    }

    // Définit la description du critère
    public void setDescription(String description) {
        this.description = description;
    }

    // Retourne les notes associées à ce critère
    public Set<Grade> getGrades() {
        return grades;
    }

    // Remplace la collection des notes associées
    public void setGrades(Set<Grade> grades) {
        this.grades = grades;
    }
}
