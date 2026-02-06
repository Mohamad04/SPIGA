package fr.spiga.interfaces;

/**
 * Interface définissant le contrat pour les entités capables d'émettre des
 * alertes.
 * Permet à un actif de signaler des états critiques.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public interface Alertable {

    /**
     * Notifie un état critique via une alerte.
     * 
     * @param message le message d'alerte à émettre
     */
    void notifierEtatCritique(String message);

    /**
     * Vérifie si l'actif est dans un état critique.
     * 
     * @return true si l'actif est en état critique, false sinon
     */
    boolean estEnEtatCritique();
}
