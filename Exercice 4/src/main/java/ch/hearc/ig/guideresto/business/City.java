package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité JPA associée à la table VILLES
 * Représente une ville identifiée par un identifiant technique, un code postal et un nom
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
     * Restaurants rattachés à cette ville
     * Relation inverse, la clé étrangère est portée par RESTAURANTS.FK_VILL
     *
     * Fetch LAZY pour éviter de charger les restaurants tant qu’ils ne sont pas nécessaires
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
     * Constructeur utilitaire sans identifiant
     * L’identifiant est géré par la persistance
     */
    public City(String zipCode, String cityName) {
        this.zipCode = zipCode;
        this.cityName = cityName;
    }

    /**
     * Constructeur utilitaire avec identifiant
     * Utile pour des scénarios de tests ou import, l’identifiant JPA peut rester null en usage standard
     */
    public City(Integer id, String zipCode, String cityName) {
        this.id = id;
        this.zipCode = zipCode;
        this.cityName = cityName;
    }

    /**
     * Retourne l’identifiant technique de la ville
     */
    public Integer getId() {
        return id;
    }

    /**
     * Met à jour l’identifiant technique
     * À éviter en usage standard lorsque l’identifiant est généré par la base
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne le code postal
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Met à jour le code postal
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
     * Retourne la collection des restaurants associés
     * La collection peut être chargée à la demande selon la configuration LAZY
     */
    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    /**
     * Remplace la collection des restaurants associés
     * Garantit une collection non nulle afin d’éviter des NullPointerException
     *
     * Point d’attention : remplacer la collection ne met pas à jour automatiquement le côté propriétaire (Restaurant.city)
     * Si nécessaire, resynchroniser aussi la référence côté Restaurant
     */
    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = (restaurants != null ? restaurants : new HashSet<>());
    }
}
