package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité persistée représentant une ville
 * Stockée dans la table VILLES et référencée par les restaurants via une clé étrangère
 */
@Entity
@Table(name = "VILLES")
@NamedQueries({
        @NamedQuery(
                name = "City.findAll",
                query = "select c from City c order by c.zipCode, c.cityName"
        ),
        @NamedQuery(
                name = "City.findByZipCode",
                query = "select c from City c where c.zipCode = :zipCode"
        )
})
public class City implements IBusinessObject {

    /**
     * Identifiant technique de la ville
     * Généré par séquence en base
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VILLES_GEN")
    @SequenceGenerator(
            name = "SEQ_VILLES_GEN",
            sequenceName = "SEQ_VILLES",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    /**
     * Code postal de la ville
     */
    @Column(name = "CODE_POSTAL", nullable = false, length = 100)
    private String zipCode;

    /**
     * Nom de la ville
     */
    @Column(name = "NOM_VILLE", nullable = false, length = 100)
    private String cityName;

    /**
     * Restaurants associés à la ville
     * Relation inverse basée sur le champ city dans l'entité Restaurant
     * Chargement LAZY pour éviter de charger la collection systématiquement
     */
    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY)
    private Set<Restaurant> restaurants = new HashSet<>();

    /**
     * Constructeur sans argument requis par JPA
     */
    public City() {
        // Constructeur requis par JPA
    }

    /**
     * Constructeur utilitaire pour créer une ville sans identifiant
     * L'identifiant peut rester null lorsque la base le génère
     */
    public City(String zipCode, String cityName) {
        this.zipCode = zipCode;
        this.cityName = cityName;
    }

    /**
     * Constructeur utilitaire permettant de fournir un identifiant
     * Utile pour compatibilité avec des jeux de données ou des tests
     */
    public City(Integer id, String zipCode, String cityName) {
        this.id = id;
        this.zipCode = zipCode;
        this.cityName = cityName;
    }

    /**
     * Retourne l'identifiant de la ville
     */
    public Integer getId() {
        return id;
    }

    /**
     * Met à jour l'identifiant de la ville
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne le code postal de la ville
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Met à jour le code postal de la ville
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * Retourne le nom de la ville
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * Met à jour le nom de la ville
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * Retourne la collection des restaurants associés à la ville
     * L'accès hors transaction peut déclencher une LazyInitializationException
     */
    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    /**
     * Remplace la collection des restaurants associés à la ville
     */
    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }
}
