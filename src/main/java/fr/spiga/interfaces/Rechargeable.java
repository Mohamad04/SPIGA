package fr.spiga.interfaces;

/**
 * Interface définissant le contrat pour les entités rechargeables.
 * Tout actif nécessitant un rechargement ou ravitaillement doit implémenter
 * cette interface.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public interface Rechargeable {

    /**
     * Recharge complètement la batterie de l'actif (pour les systèmes électriques).
     */
    void recharger();

    /**
     * Ravitaille l'actif en carburant (pour les systèmes thermiques).
     */
    void ravitailler();

    /**
     * Obtient l'autonomie restante en pourcentage.
     * 
     * @return l'autonomie restante (0.0 à 100.0)
     */
    double getAutonomieRestante();

    /**
     * Consomme de l'autonomie lors d'une opération.
     * 
     * @param quantite la quantité d'autonomie à consommer (en pourcentage)
     */
    void consommerAutonomie(double quantite);
}
