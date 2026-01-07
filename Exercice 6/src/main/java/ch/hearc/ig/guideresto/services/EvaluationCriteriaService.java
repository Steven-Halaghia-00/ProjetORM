package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;

import java.util.List;

public class EvaluationCriteriaService extends AbstractService {

    // Retourne la liste complète des critères d’évaluation triés par nom
    public List<EvaluationCriteria> findAll() {
        return doInTx(em ->
                em.createQuery("select c from EvaluationCriteria c order by c.name", EvaluationCriteria.class)
                        .getResultList()
        );
    }
}
