package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;

import java.util.List;

/**
 * Service applicatif dédié aux critères d'évaluation
 *
 * Fournit une opération de lecture centralisée pour récupérer la liste des critères
 * La requête est exécutée dans une transaction gérée par la couche service
 */
public class EvaluationCriteriaService extends AbstractService {

    /**
     * Retourne tous les critères d'évaluation triés par nom
     */
    public List<EvaluationCriteria> findAll() {
        return doInTx(em ->
                em.createQuery("select c from EvaluationCriteria c order by c.name", EvaluationCriteria.class)
                        .getResultList()
        );
    }
}
