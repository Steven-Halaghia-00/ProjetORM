package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité JPA représentant un type gastronomique persisté dans la table TYPES_GASTRONOMIQUES
 *
 * Un type gastronomique est identifié fonctionnellement par son libellé, contraint unique en base
 * La relation inverse vers les restaurants est exposée à titre de navigation, le propriétaire étant {@link Restaurant#restaurantType}
 *
 * Des requêtes nommées sont définies afin de centraliser les accès usuels
 */
@Entity
@Table(name = "TYPES_GASTRONOMIQUES")
@NamedQueries({
        @NamedQuery(
                name = "RestaurantType.findAll",
                query = "select t from RestaurantType t order by t.label"
        ),
        @NamedQuery(
                name = "RestaurantType.findByLabel",
                query = "select t from RestaurantType t where upper(t.label) = upper(:label)"
        )
})
public class RestaurantType implements IBusinessObject {

    /**
     * Identifiant technique
     * Généré via la séquence SEQ_TYPES_GASTRONOMIQUES alignée sur le schéma
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
     * Libellé du type gastronomique
     * Contrainte d'unicité en base pour éviter les doublons
     */
    @Column(name = "LIBELLE", nullable = false, length = 100, unique = true)
    private String label;

    /**
     * Description du type gastronomique
     * Stockée en CLOB côté base
     */
    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    /**
     * Association inverse vers les restaurants de ce type
     *
     * Le propriétaire de la relation est {@link Restaurant#restaurantType}
     * Chargement lazy afin d'éviter de charger tous les restaurants lors de la lecture d'un type
     */
    @OneToMany(mappedBy = "restaurantType", fetch = FetchType.LAZY)
    private Set<Restaurant> restaurants = new HashSet<>();

    /**
     * Constructeur par défaut requis par JPA
     */
    public RestaurantType() {
    }

    /**
     * Constructeur utilitaire avec identifiant généré
     */
    public RestaurantType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /**
     * Constructeur complet
     * Peut être utile en tests ou lors de manipulations hors persistance
     */
    public RestaurantType(Integer id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    /**
     * Représentation textuelle orientée affichage
     * Utilisée notamment dans les menus de sélection
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

    /**
     * Retourne les restaurants associés
     * En contexte hors transaction, l'accès peut nécessiter une initialisation préalable
     */
    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }
}
