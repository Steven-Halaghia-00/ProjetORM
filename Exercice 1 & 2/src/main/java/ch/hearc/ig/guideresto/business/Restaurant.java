package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Exercice 2 (mapping simple) :
 * - Mapping des champs simples et de l'identifiant.
 * - Les associations sont exclues du mapping à ce stade.
 *
 * Référence SQL :
 * RESTAURANTS(numero, nom, adresse, description, site_web, fk_type, fk_vill)
 */
@Entity
@Table(name = "RESTAURANTS")
public class Restaurant implements IBusinessObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RESTAURANTS_GEN")
    @SequenceGenerator(
            name = "SEQ_RESTAURANTS_GEN",
            sequenceName = "SEQ_RESTAURANTS",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    @Column(name = "NOM", nullable = false, length = 100)
    private String name;

    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SITE_WEB", length = 100)
    private String website;

    /**
     * Exercice 2 : les informations d'adresse (ADRESSE + FK_VILL) sont stockées dans un objet valeur embarqué.
     */
    @Embedded
    private Localisation address;

    /**
     * Exercice 2 : la relation vers TYPES_GASTRONOMIQUES n'est pas mappée.
     * Seul l'identifiant du type est stocké.
     */
    @Column(name = "FK_TYPE", nullable = false)
    private Integer typeId;

    /**
     * Exercice 2 : associations exclues du mapping.
     */
    @Transient
    private Set<Evaluation> evaluations = new HashSet<>();

    @Transient
    private RestaurantType type;

    public Restaurant() {
        // Constructeur requis par JPA
    }

    /**
     * Constructeur utilitaire (mapping simple).
     *
     * Remarque : Localisation prend en charge ADRESSE + FK_VILL.
     */
    public Restaurant(Integer id,
                      String name,
                      String description,
                      String website,
                      String street,
                      City city,
                      RestaurantType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.address = new Localisation(street, city);
        this.type = type;
        this.typeId = (type != null ? type.getId() : null);
        this.evaluations = new HashSet<>();
    }

    public Restaurant(Integer id, String name, String description, String website, Localisation address, RestaurantType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.address = address;
        this.type = type;
        this.typeId = (type != null ? type.getId() : null);
        this.evaluations = new HashSet<>();
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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Localisation getAddress() {
        return address;
    }

    public void setAddress(Localisation address) {
        this.address = address;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Set<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(Set<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public RestaurantType getType() {
        return type;
    }

    /**
     * Mise à jour du type côté métier et synchronisation de l'identifiant simple (FK_TYPE).
     */
    public void setType(RestaurantType type) {
        this.type = type;
        this.typeId = (type != null ? type.getId() : null);
    }

    public boolean hasEvaluations() {
        return CollectionUtils.isNotEmpty(evaluations);
    }
}
