package ch.hearc.ig.guideresto.business;

/**
 * Contrat minimal pour les objets métiers manipulés par l'application
 *
 * L'interface permet d'unifier l'accès à l'identifiant technique des entités
 * Cela facilite certains traitements génériques côté présentation ou persistance
 */
public interface IBusinessObject {

    /**
     * Retourne l'identifiant technique de l'objet
     * La valeur peut être null avant persistance lorsque l'identifiant est généré
     */
    Integer getId();
}
