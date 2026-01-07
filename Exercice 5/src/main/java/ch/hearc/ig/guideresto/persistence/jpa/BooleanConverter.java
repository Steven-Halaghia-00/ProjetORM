package ch.hearc.ig.guideresto.persistence.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convertisseur JPA pour persister un Boolean sous forme de CHAR(1)
 * Valeurs attendues en base : "T" pour true et "F" pour false
 */
@Converter
public class BooleanConverter implements AttributeConverter<Boolean, String> {

    /**
     * Convertit la valeur métier Boolean vers la représentation en base
     * Retourne "T" si la valeur est true, sinon "F"
     */
    @Override
    public String convertToDatabaseColumn(Boolean value) {
        return Boolean.TRUE.equals(value) ? "T" : "F";
    }

    /**
     * Convertit la valeur lue en base vers un Boolean
     * Interprète "T" (insensible à la casse) comme true, toute autre valeur comme false
     */
    @Override
    public Boolean convertToEntityAttribute(String s) {
        return s != null && s.equalsIgnoreCase("T");
    }
}
