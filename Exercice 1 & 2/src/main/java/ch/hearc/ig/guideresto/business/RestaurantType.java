package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Exercice 2 (mapping simple) :
 * - Mapping des champs simples et de l'identifiant.
 * - Les associations sont exclues du mapping à ce stade.
 *
 * Référence SQL :
 * TYPES_GASTRONOMIQUES(numero, libelle, description)
 */
@Entity
@Table(name = "TYPES_GASTRONOMIQUES")
public class RestaurantType implements IBusinessObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TYPES_GASTRO_GEN")
    @SequenceGenerator(
            name = "SEQ_TYPES_GASTRO_GEN",
            sequenceName = "SEQ_TYPES_GASTRONOMIQUES",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    @Column(name = "LIBELLE", nullable = false, length = 100, unique = true)
    private String label;

    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    /**
     * Exercice 2 : association non mappée.
     */
    @Transient
    private Set<Restaurant> restaurants = new HashSet<>();

    public RestaurantType() {
        // Constructeur requis par JPA
    }

    public RestaurantType(String label, String description) {
        this(null, label, description);
    }

    public RestaurantType(Integer id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.restaurants = new HashSet<>();
    }

    @Override
    public String toString() {
        return label;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }
}
