package ch.hearc.ig.guideresto.business;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Objet valeur embarqué représentant l'adresse d'un restaurant
 * Mappe la colonne ADRESSE de la table RESTAURANTS via @Embedded
 */
@Embeddable
public class Localisation {

    /**
     * Rue et numéro du restaurant stockés dans la colonne ADRESSE
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
     * Constructeur utilitaire pour créer une localisation avec une rue
     */
    public Localisation(String street) {
        this.street = street;
    }

    /**
     * Retourne la rue
     */
    public String getStreet() {
        return street;
    }

    /**
     * Met à jour la rue
     */
    public void setStreet(String street) {
        this.street = street;
    }
}
