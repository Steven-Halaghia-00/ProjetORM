package ch.hearc.ig.guideresto.business;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;

/**
 * Exercice 2 (mapping simple) :
 * - Objet valeur embarqué dans RESTAURANTS.
 * - Champs persistés : adresse (ADRESSE) et identifiant ville (FK_VILL).
 * - L'objet City n'est pas persisté à ce stade (associations traitées plus tard).
 */
@Embeddable
public class Localisation {

    @Column(name = "ADRESSE", nullable = false, length = 100)
    private String street;

    @Column(name = "FK_VILL", nullable = false)
    private Integer cityId;

    /**
     * Exercice 2 : association non mappée.
     */
    @Transient
    private City city;

    public Localisation() {
        // Constructeur requis par JPA
    }

    public Localisation(String street, City city) {
        this.street = street;
        setCity(city);
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public City getCity() {
        return city;
    }

    /**
     * Définit la ville côté métier et synchronise l'identifiant simple (cityId) si disponible.
     */
    public void setCity(City city) {
        this.city = city;
        this.cityId = (city != null ? city.getId() : null);
    }
}
