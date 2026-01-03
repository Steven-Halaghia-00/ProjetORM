package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité JPA représentant un restaurant
 * Le mapping est associé à la table RESTAURANTS
 */
@Entity
@Table(name = "RESTAURANTS")
public class Restaurant implements IBusinessObject {

    /**
     * Identifiant technique généré par séquence Oracle
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RESTAURANTS_GEN")
    @SequenceGenerator(
            name = "SEQ_RESTAURANTS_GEN",
            sequenceName = "SEQ_RESTAURANTS",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    /**
     * Nom du restaurant
     */
    @Column(name = "NOM", nullable = false, length = 100)
    private String name;

    /**
     * Description potentiellement longue
     */
    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    /**
     * URL du site web du restaurant
     */
    @Column(name = "SITE_WEB", length = 100)
    private String website;

    /**
     * Valeur embarquée représentant l'adresse du restaurant
     * Le champ street est persisté dans la colonne ADRESSE via Localisation
     */
    @Embedded
    private Localisation address;

    /**
     * Ville associée au restaurant
     * La clé étrangère est stockée dans la colonne FK_VILL
     * Le chargement est lazy pour éviter une jointure systématique
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_VILL", nullable = false)
    private City city;

    /**
     * Type gastronomique associé au restaurant
     * La clé étrangère est stockée dans la colonne FK_TYPE
     * Le chargement est lazy pour éviter une jointure systématique
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_TYPE", nullable = false)
    private RestaurantType restaurantType;

    /**
     * Collection d'évaluations utilisée côté application
     * Le champ est transitoire afin d'être exclu du mapping JPA à ce stade
     */
    @Transient
    private Set<Evaluation> evaluations = new HashSet<>();

    /**
     * Constructeur sans argument requis par JPA
     */
    public Restaurant() {
        // Constructeur requis par JPA
    }

    /**
     * Constructeur utilitaire
     * Initialise l'adresse via l'objet embarqué Localisation
     *
     * @param id identifiant technique (null si génération attendue)
     * @param name nom du restaurant
     * @param description description du restaurant
     * @param website site web
     * @param street rue de l'adresse
     * @param city ville associée
     * @param restaurantType type gastronomique associé
     */
    public Restaurant(Integer id,
                      String name,
                      String description,
                      String website,
                      String street,
                      City city,
                      RestaurantType restaurantType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.address = new Localisation(street);
        this.city = city;
        this.restaurantType = restaurantType;
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

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    /**
     * Accès métier au type gastronomique
     *
     * @return le type gastronomique associé
     */
    public RestaurantType getType() {
        return restaurantType;
    }

    public void setType(RestaurantType restaurantType) {
        this.restaurantType = restaurantType;
    }

    /**
     * Retourne la collection d'évaluations gérée côté application
     * Cette collection n'est pas persistée via JPA dans cette version
     */
    public Set<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(Set<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    /**
     * Indique si le restaurant possède au moins une évaluation côté application
     *
     * @return true si la collection contient des éléments, sinon false
     */
    public boolean hasEvaluations() {
        return CollectionUtils.isNotEmpty(evaluations);
    }
}
