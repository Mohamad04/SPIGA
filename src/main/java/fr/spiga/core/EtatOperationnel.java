package fr.spiga.core;

/**
 * Énumération représentant l'état opérationnel d'un actif mobile.
 * Définit les différents états possibles dans lesquels un actif peut se
 * trouver.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public enum EtatOperationnel {
    /**
     * L'actif est au sol ou à quai, prêt à être déployé
     */
    AU_SOL,

    /**
     * L'actif est actuellement en mission
     */
    EN_MISSION,

    /**
     * L'actif est en cours de maintenance
     */
    EN_MAINTENANCE,

    /**
     * L'actif est en panne et nécessite une réparation
     */
    EN_PANNE
}
