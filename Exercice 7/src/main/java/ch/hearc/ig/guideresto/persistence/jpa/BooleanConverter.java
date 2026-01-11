package ch.hearc.ig.guideresto.persistence.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convertisseur JPA pour persister un booléen sous forme de caractère
 *
 * La représentation en base utilise les valeurs
 * - T pour vrai
 * - F pour faux
 *
 * Le convertisseur est utilisé via @Convert sur les champs concernés
 */
@Converter
public class BooleanConverter implements AttributeConverter<Boolean, String> {

    /**
     * Convertit la valeur Java vers la représentation attendue en base
     * Toute valeur différente de TRUE est persistée comme F
     */
    @Override
    public String convertToDatabaseColumn(Boolean value) {
        return Boolean.TRUE.equals(value) ? "T" : "F";
    }

    /**
     * Convertit la valeur base vers un booléen Java
     * La comparaison est insensible à la casse
     */
    @Override
    public Boolean convertToEntityAttribute(String s) {
        return s != null && s.equalsIgnoreCase("T");
    }
}
