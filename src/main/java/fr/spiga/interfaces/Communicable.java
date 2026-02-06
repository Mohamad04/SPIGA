package fr.spiga.interfaces;

import fr.spiga.core.ActifMobile;

/**
 * Interface définissant le contrat pour les entités capables de communiquer.
 * Permet la transmission d'alertes entre actifs.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public interface Communicable {

    /**
     * Transmet une alerte à un autre actif.
     * 
     * @param message    le message d'alerte à transmettre
     * @param actifCible l'actif destinataire de l'alerte
     * @return true si la transmission a réussi, false sinon
     */
    boolean transmettreAlerte(String message, ActifMobile actifCible);

    /**
     * Reçoit un message d'alerte d'un autre actif.
     * 
     * @param message  le message reçu
     * @param emetteur l'actif émetteur du message
     */
    void recevoirAlerte(String message, ActifMobile emetteur);
}
