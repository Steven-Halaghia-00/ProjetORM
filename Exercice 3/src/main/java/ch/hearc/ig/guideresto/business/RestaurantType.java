package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité JPA représentant un type de cuisine
 * Le mapping est associé à la table TYPES_GASTRONOMIQUES
 */
@Entity
@Table(name = "TYPES_GASTRONOMIQUES")
public class RestaurantType implements IBusinessObject {

    /**
     * Identifiant technique généré par séquence Oracle
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
     * Libellé du type
     * Contrainte d'unicité côté base pour éviter les doublons
     */
    @Column(name = "LIBELLE", nullable = false, length = 100, unique = true)
    private String label;

    /**
     * Description textuelle potentiellement longue
     * Le type LOB permet de stocker un volume important selon le dialecte et le schéma
     */
    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    /**
     * Association inverse vers les restaurants
     * Le côté propriétaire de la relation est Restaurant.restaurantType (clé étrangère FK_TYPE)
     * Le chargement est lazy pour éviter de récupérer la collection systématiquement
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
     * Constructeur utilitaire sans identifiant
     * L'identifiant est laissé à null afin d'être généré lors de la persistance
     *
     * @param label libellé du type
     * @param description description du type
     */
    public RestaurantType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /**
     * Constructeur utilitaire avec identifiant
     * Utile pour des jeux de données en mémoire ou des tests
     *
     * @param id identifiant technique
     * @param label libellé du type
     * @param description description du type
     */
    public RestaurantType(Integer id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    /**
     * Représentation textuelle utilisée notamment dans les affichages console
     *
     * @return le libellé du type
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

    /**
     * Remplace la collection de restaurants associée au type
     * En pratique, cette méthode est surtout utile pour l'initialisation ou les tests
     *
     * @param restaurants nouvelle collection (peut être null)
     */
    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }
}
