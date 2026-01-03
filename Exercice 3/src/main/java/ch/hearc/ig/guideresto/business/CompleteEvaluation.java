package ch.hearc.ig.guideresto.business;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Modèle représentant une évaluation détaillée associée à un restaurant
 * Contient un commentaire, un nom d’utilisateur et un ensemble de notes par critère
 */
public class CompleteEvaluation extends Evaluation {

    /**
     * Commentaire textuel saisi par l’utilisateur
     */
    private String comment;

    /**
     * Identifiant fonctionnel de l’auteur de l’évaluation
     */
    private String username;

    /**
     * Ensemble des notes associées à l’évaluation
     */
    private Set<Grade> grades;

    /**
     * Constructeur sans argument
     * Initialise l’instance avec des valeurs nulles et un ensemble de notes vide
     */
    public CompleteEvaluation() {
        this(null, null, null, null);
    }

    /**
     * Constructeur utilitaire sans identifiant
     * Délègue au constructeur complet en laissant l’identifiant à null
     *
     * @param visitDate date de visite ou date de l’évaluation
     * @param restaurant restaurant évalué
     * @param comment commentaire saisi
     * @param username nom d’utilisateur
     */
    public CompleteEvaluation(Date visitDate, Restaurant restaurant, String comment, String username) {
        this(null, visitDate, restaurant, comment, username);
    }

    /**
     * Constructeur complet
     * Initialise les champs métier et prépare la collection de notes
     *
     * @param id identifiant technique
     * @param visitDate date de visite ou date de l’évaluation
     * @param restaurant restaurant évalué
     * @param comment commentaire saisi
     * @param username nom d’utilisateur
     */
    public CompleteEvaluation(Integer id, Date visitDate, Restaurant restaurant, String comment, String username) {
        super(id, visitDate, restaurant);
        this.comment = comment;
        this.username = username;
        this.grades = new HashSet<>();
    }

    /**
     * Retourne le commentaire de l’évaluation
     *
     * @return commentaire saisi
     */
    public String getComment() {
        return comment;
    }

    /**
     * Met à jour le commentaire de l’évaluation
     *
     * @param comment nouveau commentaire
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Retourne le nom d’utilisateur associé à l’évaluation
     *
     * @return nom d’utilisateur
     */
    public String getUsername() {
        return username;
    }

    /**
     * Met à jour le nom d’utilisateur associé à l’évaluation
     *
     * @param username nouveau nom d’utilisateur
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retourne l’ensemble des notes associées à l’évaluation
     *
     * @return collection de notes
     */
    public Set<Grade> getGrades() {
        return grades;
    }

    /**
     * Remplace l’ensemble des notes associées à l’évaluation
     *
     * @param grades nouvelle collection de notes
     */
    public void setGrades(Set<Grade> grades) {
        this.grades = grades;
    }
}
