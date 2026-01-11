package ch.hearc.ig.guideresto.business;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Valeur embarquée représentant l'adresse d'un restaurant
 *
 * L'utilisation d'un type embarqué permet de regrouper des attributs liés
 * L'instance est persistée dans la table propriétaire via ses colonnes
 */
@Embeddable
public class Localisation {

    /**
     * Rue et numéro
     * Persisté dans la colonne ADRESSE de la table propriétaire
     */
    @Column(name = "ADRESSE", nullable = false, length = 100)
    private String street;

    /**
     * Constructeur par défaut requis par JPA
     */
    public Localisation() {
    }

    /**
     * Constructeur utilitaire
     *
     * @param street rue et numéro
     */
    public Localisation(String street) {
        this.street = street;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}
