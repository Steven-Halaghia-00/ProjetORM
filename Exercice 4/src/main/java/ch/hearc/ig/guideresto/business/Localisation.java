package ch.hearc.ig.guideresto.business;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Objet valeur embarqué représentant l'adresse du restaurant
 * Persisté dans la table du propriétaire via la colonne ADRESSE
 */
@Embeddable
public class Localisation {

    /**
     * Rue et numéro du restaurant
     * Mappé sur la colonne ADRESSE de la table propriétaire
     */
    @Column(name = "ADRESSE", nullable = false, length = 100)
    private String street;

    /**
     * Constructeur sans argument requis par JPA
     */
    public Localisation() {
        // Constructeur requis par JPA
    }

    /**
     * Constructeur pratique pour initialiser l'adresse
     */
    public Localisation(String street) {
        this.street = street;
    }

    /**
     * Retourne la rue stockée dans l'objet valeur
     */
    public String getStreet() {
        return street;
    }

    /**
     * Met à jour la rue stockée dans l'objet valeur
     */
    public void setStreet(String street) {
        this.street = street;
    }
}
