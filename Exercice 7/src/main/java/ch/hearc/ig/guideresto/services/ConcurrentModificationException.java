package ch.hearc.ig.guideresto.services;

/**
 * Exception applicative signalant un conflit de concurrence lors d'une modification
 *
 * Utilisée pour remonter une information métier claire à la couche de présentation
 * En pratique, elle encapsule typiquement un conflit de verrou optimiste détecté par JPA
 * ou une divergence de version attendue lors d'une mise à jour
 */
public class ConcurrentModificationException extends RuntimeException {

    /**
     * Construit une exception avec un message explicite
     */
    public ConcurrentModificationException(String message) {
        super(message);
    }

    /**
     * Construit une exception avec un message explicite et une cause technique
     */
    public ConcurrentModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
