package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Exercice 2 (mapping simple) :
 * - Mapping de l'identifiant et des colonnes simples.
 * - Les associations (restaurants) sont exclues du mapping à ce stade.
 *
 * Référence SQL :
 * VILLES(numero, code_postal, nom_ville)
 */
@Entity
@Table(name = "VILLES")
public class City implements IBusinessObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VILLES_GEN")
    @SequenceGenerator(
            name = "SEQ_VILLES_GEN",
            sequenceName = "SEQ_VILLES",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    @Column(name = "CODE_POSTAL", nullable = false, length = 100)
    private String zipCode;

    @Column(name = "NOM_VILLE", nullable = false, length = 100)
    private String cityName;

    /**
     * Exercice 2 : association non mappée.
     */
    @Transient
    private Set<Restaurant> restaurants;

    public City() {
        this(null, null);
    }

    public City(String zipCode, String cityName) {
        this(null, zipCode, cityName);
    }

    public City(Integer id, String zipCode, String cityName) {
        this.id = id;
        this.zipCode = zipCode;
        this.cityName = cityName;
        this.restaurants = new HashSet<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String city) {
        this.cityName = city;
    }

    public Set<Restaurant> getRestaurants() {
        if (restaurants == null) {
            restaurants = new HashSet<>();
        }
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }
}
