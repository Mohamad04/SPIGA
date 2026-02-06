package fr.spiga.interfaces;

/**
 * Interface définissant le contrat pour les entités pilotables.
 * Tout actif pouvant être démarré et éteint doit implémenter cette interface.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public interface Pilotable {

    /**
     * Démarre l'actif et le met en état opérationnel.
     * 
     * @return true si le démarrage a réussi, false sinon
     */
    boolean demarrer();

    /**
     * Éteint l'actif et le met hors service.
     * 
     * @return true si l'arrêt a réussi, false sinon
     */
    boolean eteindre();

    /**
     * Vérifie si l'actif est actuellement en marche.
     * 
     * @return true si l'actif est actif, false sinon
     */
    boolean estEnMarche();
}
