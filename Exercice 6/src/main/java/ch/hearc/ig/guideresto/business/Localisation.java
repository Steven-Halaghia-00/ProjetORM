package ch.hearc.ig.guideresto.business;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Localisation {

    // Valeur de rue stockée dans la colonne ADRESSE de l'entité propriétaire
    @Column(name = "ADRESSE", nullable = false, length = 100)
    private String street;

    // Constructeur sans argument requis par JPA
    public Localisation() {
        // Constructeur requis par JPA
    }

    // Constructeur utilitaire pour initialiser directement la rue
    public Localisation(String street) {
        this.street = street;
    }

    // Retourne la rue
    public String getStreet() {
        return street;
    }

    // Modifie la rue
    public void setStreet(String street) {
        this.street = street;
    }
}
