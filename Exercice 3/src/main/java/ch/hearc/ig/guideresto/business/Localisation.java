package ch.hearc.ig.guideresto.business;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Objet valeur embarqué représentant une localisation
 * Le champ street est persisté dans la colonne ADRESSE de l'entité propriétaire
 */
@Embeddable
public class Localisation {

    /**
     * Rue associée à la localisation
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
     * Constructeur utilitaire
     *
     * @param street rue à associer à la localisation
     */
    public Localisation(String street) {
        this.street = street;
    }

    /**
     * Accès à la rue de la localisation
     *
     * @return la rue enregistrée
     */
    public String getStreet() {
        return street;
    }

    /**
     * Mise à jour de la rue de la localisation
     *
     * @param street nouvelle rue
     */
    public void setStreet(String street) {
        this.street = street;
    }
}
