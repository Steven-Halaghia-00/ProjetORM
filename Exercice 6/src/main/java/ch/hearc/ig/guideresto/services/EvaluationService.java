package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.*;
import jakarta.persistence.EntityManager;

import java.util.Date;
import java.util.List;

public class EvaluationService extends AbstractService {

    // Ajoute une évaluation basique (like ou dislike) pour un restaurant donné
    public void addBasicEvaluation(int restaurantId, boolean like, Date date, String ipAddress) {
        doInTxVoid(em -> {
            // Charge le restaurant dans le contexte de persistance
            Restaurant r = em.find(Restaurant.class, restaurantId);
            if (r == null) throw new IllegalArgumentException("Restaurant introuvable id=" + restaurantId);

            // Construit l’entité avec un identifiant null afin de laisser la base générer la clé primaire
            BasicEvaluation eval = new BasicEvaluation(null, date, r, like, ipAddress);

            // Ajoute l’évaluation à la collection côté Restaurant pour déclencher la cascade et maintenir la cohérence
            r.getBasicEvaluations().add(eval);

            // Persist direct non requis si la relation est bien en cascade depuis Restaurant
        });
    }

    // Ajoute une évaluation complète et ses notes associées à un restaurant
    public void addCompleteEvaluation(
            int restaurantId,
            Date visitDate,
            String comment,
            String username,
            List<GradeInput> grades
    ) {
        // Encapsule l’opération dans une transaction et délègue à une méthode interne pour factoriser la logique
        doInTxVoid(em -> addCompleteEvaluationTx(em, restaurantId, visitDate, comment, username, grades));
    }

    // Réalise l’ajout de l’évaluation complète en utilisant un EntityManager déjà transactionnel
    private void addCompleteEvaluationTx(
            EntityManager em,
            int restaurantId,
            Date visitDate,
            String comment,
            String username,
            List<GradeInput> grades
    ) {
        // Charge le restaurant cible et valide son existence
        Restaurant restaurant = em.find(Restaurant.class, restaurantId);
        if (restaurant == null) throw new IllegalArgumentException("Restaurant introuvable id=" + restaurantId);

        // Valide la présence d’au moins une note afin d’éviter une évaluation complète vide
        if (grades == null || grades.isEmpty()) {
            throw new IllegalArgumentException("Une évaluation complète doit contenir au moins une note.");
        }

        // Crée l’évaluation complète en laissant la clé primaire être générée
        CompleteEvaluation eval = new CompleteEvaluation(visitDate, restaurant, comment, username);

        // Construit les notes et associe chaque note au critère correspondant
        for (GradeInput gi : grades) {
            // Charge le critère et valide son existence
            EvaluationCriteria crit = em.find(EvaluationCriteria.class, gi.criteriaId());
            if (crit == null) throw new IllegalArgumentException("Critère introuvable id=" + gi.criteriaId());

            // Crée la note et l’associe à l’évaluation et au critère
            Grade g = new Grade(gi.grade(), eval, crit);

            // Ajoute la note via la méthode métier pour synchroniser la relation bidirectionnelle
            eval.addGrade(g);
        }

        // Persiste l’évaluation complète, la cascade se charge de persister les notes associées
        em.persist(eval);
    }

    // Structure d’entrée simple pour transporter l’identifiant d’un critère et la note associée
    public record GradeInput(int criteriaId, int grade) {}
}
