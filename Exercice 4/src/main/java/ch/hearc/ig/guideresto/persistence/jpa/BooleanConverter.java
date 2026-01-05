package ch.hearc.ig.guideresto.persistence.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanConverter implements AttributeConverter<Boolean, String> {

    /**
     * Convertit un Boolean Java vers la représentation en base
     * Retourne "T" pour true et "F" pour false ou null
     */
    @Override
    public String convertToDatabaseColumn(Boolean value) {
        return Boolean.TRUE.equals(value) ? "T" : "F";
    }

    /**
     * Convertit la valeur stockée en base vers un Boolean Java
     * Interprète "T" (insensible à la casse) comme true, le reste comme false
     */
    @Override
    public Boolean convertToEntityAttribute(String s) {
        return s != null && s.equalsIgnoreCase("T");
    }
}
