package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité JPA représentant une ville
 * Mappe les colonnes de la table VILLES et expose la relation inverse vers les restaurants associés
 */
@Entity
@Table(name = "VILLES")
public class City implements IBusinessObject {

    /**
     * Identifiant technique de la ville
     * Généré via la séquence SEQ_VILLES
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
     * Relation inverse vers les restaurants rattachés à cette ville
     * Le chargement est différé et la clé étrangère est portée par Restaurant via le champ "city"
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
     * Constructeur utilitaire pour initialiser les champs métier hors identifiant
     *
     * @param zipCode code postal
     * @param cityName nom de la ville
     */
    public City(String zipCode, String cityName) {
        this.zipCode = zipCode;
        this.cityName = cityName;
    }

    /**
     * Constructeur utilitaire permettant de fournir un identifiant
     * Utile pour des scénarios hors JPA (tests, données factices)
     *
     * @param id identifiant technique
     * @param zipCode code postal
     * @param cityName nom de la ville
     */
    public City(Integer id, String zipCode, String cityName) {
        this.id = id;
        this.zipCode = zipCode;
        this.cityName = cityName;
    }

    /**
     * Retourne l’identifiant technique
     *
     * @return identifiant
     */
    public Integer getId() {
        return id;
    }

    /**
     * Met à jour l’identifiant technique
     * À éviter en usage JPA standard où l’identifiant est généré
     *
     * @param id identifiant
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne le code postal
     *
     * @return code postal
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Met à jour le code postal
     *
     * @param zipCode code postal
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * Retourne le nom de la ville
     *
     * @return nom de la ville
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * Met à jour le nom de la ville
     *
     * @param cityName nom de la ville
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * Retourne la collection des restaurants associés
     *
     * @return restaurants de la ville
     */
    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    /**
     * Remplace la collection des restaurants associés
     *
     * @param restaurants collection de restaurants
     */
    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }
}
