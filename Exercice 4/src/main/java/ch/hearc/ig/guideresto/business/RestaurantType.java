package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité persistée correspondant à un type gastronomique
 * Reliée à la table TYPES_GASTRONOMIQUES
 */
@Entity
@Table(name = "TYPES_GASTRONOMIQUES")
public class RestaurantType implements IBusinessObject {

    /**
     * Identifiant technique généré via une séquence Oracle
     * Mappé sur la colonne NUMERO
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TYPES_GASTRO_GEN")
    @SequenceGenerator(
            name = "SEQ_TYPES_GASTRO_GEN",
            sequenceName = "SEQ_TYPES_GASTRONOMIQUES",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    /**
     * Libellé unique du type gastronomique
     * Mappé sur la colonne LIBELLE
     */
    @Column(name = "LIBELLE", nullable = false, length = 100, unique = true)
    private String label;

    /**
     * Description textuelle du type gastronomique
     * Stockée en LOB pour permettre une longueur importante
     */
    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    /**
     * Association inverse vers les restaurants de ce type
     * mappedBy pointe sur l'attribut Restaurant.restaurantType
     * LAZY pour éviter de charger la collection tant qu'elle n'est pas utilisée
     */
    @OneToMany(mappedBy = "restaurantType", fetch = FetchType.LAZY)
    private Set<Restaurant> restaurants = new HashSet<>();

    /**
     * Constructeur sans argument requis par JPA
     */
    public RestaurantType() {
        // Constructeur requis par JPA
    }

    /**
     * Constructeur pratique pour créer un type côté métier
     * L'identifiant reste null et sera généré à la persistance
     */
    public RestaurantType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /**
     * Constructeur utilitaire permettant de fournir un id explicitement
     * Utile pour des jeux de données ou tests hors JPA
     */
    public RestaurantType(Integer id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    /**
     * Représentation textuelle basée sur le libellé
     * Pratique pour l'affichage dans la console ou les logs
     */
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
