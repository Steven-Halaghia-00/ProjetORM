package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité JPA associée à la table TYPES_GASTRONOMIQUES
 *
 * Table : TYPES_GASTRONOMIQUES(numero, libelle, description)
 */
@Entity
@Table(name = "TYPES_GASTRONOMIQUES")
@NamedQueries({
        // Requête pour récupérer tous les types triés par libellé
        @NamedQuery(
                name = "RestaurantType.findAll",
                query = "select t from RestaurantType t order by t.label"
        ),
        // Requête pour rechercher un type par libellé sans sensibilité à la casse
        @NamedQuery(
                name = "RestaurantType.findByLabel",
                query = "select t from RestaurantType t where upper(t.label) = upper(:label)"
        )
})
public class RestaurantType implements IBusinessObject {

    // Identifiant technique généré par séquence côté base
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TYPES_GASTRO_GEN")
    @SequenceGenerator(
            name = "SEQ_TYPES_GASTRO_GEN",
            sequenceName = "SEQ_TYPES_GASTRONOMIQUES",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    // Libellé du type, unique et obligatoire
    @Column(name = "LIBELLE", nullable = false, length = 100, unique = true)
    private String label;

    // Description longue stockée en LOB
    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    // Restaurants rattachés à ce type, côté inverse de l’association ManyToOne de Restaurant
    @OneToMany(mappedBy = "restaurantType", fetch = FetchType.LAZY)
    private Set<Restaurant> restaurants = new HashSet<>();

    // Constructeur sans argument requis par JPA
    public RestaurantType() {
        // Constructeur requis par JPA
    }

    // Constructeur utilitaire pour créer un type métier sans identifiant
    public RestaurantType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    // Constructeur utilitaire permettant de fournir un identifiant dans des cas spécifiques
    public RestaurantType(Integer id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    // Retourne le libellé pour faciliter l’affichage dans la présentation
    @Override
    public String toString() {
        return label;
    }

    // Retourne l’identifiant
    public Integer getId() {
        return id;
    }

    // Modifie l’identifiant, utile uniquement hors stratégie de génération automatique
    public void setId(Integer id) {
        this.id = id;
    }

    // Retourne le libellé
    public String getLabel() {
        return label;
    }

    // Modifie le libellé
    public void setLabel(String label) {
        this.label = label;
    }

    // Retourne la description
    public String getDescription() {
        return description;
    }

    // Modifie la description
    public void setDescription(String description) {
        this.description = description;
    }

    // Retourne l’ensemble des restaurants associés
    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    // Remplace l’ensemble des restaurants associés
    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }
}
