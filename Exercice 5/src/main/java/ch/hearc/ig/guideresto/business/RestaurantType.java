package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité JPA représentant un type de restaurant
 * Mappe la table TYPES_GASTRONOMIQUES et expose des requêtes nommées pour les recherches usuelles
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
     * Identifiant technique généré par séquence
     * Mappe la colonne NUMERO
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
     * Libellé du type de cuisine
     * Contrainte d'unicité au niveau base via unique=true
     */
    @Column(name = "LIBELLE", nullable = false, length = 100, unique = true)
    private String label;

    /**
     * Description potentiellement longue stockée en LOB
     */
    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    /**
     * Restaurants associés à ce type via la clé étrangère RESTAURANTS.FK_TYPE
     * Relation inverse, chargée à la demande
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
     * Constructeur métier pour créer un type avant persistance
     */
    public RestaurantType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /**
     * Constructeur utilitaire permettant d'instancier avec un identifiant existant
     * Utile pour les jeux de données ou tests, l'id restant normalement géré par JPA
     */
    public RestaurantType(Integer id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    /**
     * Affichage lisible du type en contexte UI ou logs
     */
    @Override
    public String toString() {
        return label;
    }

    /**
     * Retourne l'identifiant du type
     */
    public Integer getId() {
        return id;
    }

    /**
     * Met à jour l'identifiant
     * À éviter en usage normal car l'id est généré côté persistance
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne le libellé du type
     */
    public String getLabel() {
        return label;
    }

    /**
     * Met à jour le libellé du type
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Retourne la description du type
     */
    public String getDescription() {
        return description;
    }

    /**
     * Met à jour la description du type
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retourne l'ensemble des restaurants associés
     * Collection chargée en lazy, l'accès doit se faire dans un contexte transactionnel si nécessaire
     */
    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    /**
     * Remplace l'ensemble des restaurants associés
     */
    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }
}
