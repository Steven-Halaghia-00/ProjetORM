package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.*;
import jakarta.persistence.EntityManager;

import java.util.Date;
import java.util.List;

/**
 * Service applicatif dédié aux évaluations des restaurants
 *
 * Ce service regroupe les opérations d'écriture liées aux évaluations
 * Il garantit l'exécution transactionnelle et réalise les validations applicatives
 * Les associations sont persistées via les cascades définies dans le modèle JPA
 */
public class EvaluationService extends AbstractService {

    /**
     * Ajoute une évaluation simple de type like ou dislike sur un restaurant
     *
     * L'entité BasicEvaluation est rattachée au restaurant afin de profiter de la cascade
     * L'identifiant est laissé à null pour être généré par la base
     */
    public void addBasicEvaluation(int restaurantId, boolean like, Date date, String ipAddress) {
        doInTxVoid(em -> {
            Restaurant r = em.find(Restaurant.class, restaurantId);
            if (r == null) {
                throw new IllegalArgumentException("Restaurant introuvable id=" + restaurantId);
            }

            BasicEvaluation eval = new BasicEvaluation(null, date, r, like, ipAddress);

            // Ajout côté collection du restaurant afin de déclencher la cascade de persistance
            r.getBasicEvaluations().add(eval);
        });
    }

    /**
     * Ajoute une évaluation complète avec commentaire et notes sur critères
     *
     * La méthode délègue l'implémentation à une variante prenant un EntityManager afin de rester
     * composable dans un contexte transactionnel
     */
    public void addCompleteEvaluation(
            int restaurantId,
            Date visitDate,
            String comment,
            String username,
            List<GradeInput> grades
    ) {
        doInTxVoid(em -> addCompleteEvaluationTx(em, restaurantId, visitDate, comment, username, grades));
    }

    /**
     * Implémentation transactionnelle de l'ajout d'une évaluation complète
     *
     * Valide l'existence du restaurant et des critères, ainsi que la plage des notes
     * Crée l'évaluation et les notes associées, puis persiste l'ensemble en une seule transaction
     */
    private void addCompleteEvaluationTx(
            EntityManager em,
            int restaurantId,
            Date visitDate,
            String comment,
            String username,
            List<GradeInput> grades
    ) {
        Restaurant restaurant = em.find(Restaurant.class, restaurantId);
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant introuvable id=" + restaurantId);
        }

        if (grades == null || grades.isEmpty()) {
            throw new IllegalArgumentException("Une évaluation complète doit contenir au moins une note");
        }

        CompleteEvaluation eval = new CompleteEvaluation(visitDate, restaurant, comment, username);

        for (GradeInput gi : grades) {
            if (gi.grade() < 1 || gi.grade() > 5) {
                throw new IllegalArgumentException("Note invalide (" + gi.grade() + "), attendu entre 1 et 5");
            }

            EvaluationCriteria crit = em.find(EvaluationCriteria.class, gi.criteriaId());
            if (crit == null) {
                throw new IllegalArgumentException("Critère introuvable id=" + gi.criteriaId());
            }

            Grade g = new Grade(gi.grade(), eval, crit);
            eval.addGrade(g);
        }

        // Maintien de la cohérence de l'association côté Restaurant pour un graphe objet complet en mémoire
        restaurant.getCompleteEvaluations().add(eval);

        // Persistance de l'évaluation, les notes sont persistées via la cascade définie sur CompleteEvaluation
        em.persist(eval);
    }

    /**
     * Structure d'entrée représentant une note associée à un critère
     */
    public record GradeInput(int criteriaId, int grade) {}
}
