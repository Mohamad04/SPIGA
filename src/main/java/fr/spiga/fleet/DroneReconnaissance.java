package fr.spiga.fleet;

import fr.spiga.core.ActifAerien;
import fr.spiga.core.Position3D;

/**
 * Drone de reconnaissance spécialisé en surveillance haute altitude.
 * Caractérisé par une vitesse élevée et d'excellentes capacités de
 * surveillance.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class DroneReconnaissance extends ActifAerien {

    private static final double VITESSE_MAX_DEFAUT = 80.0; // m/s (288 km/h)
    private static final double AUTONOMIE_MAX_DEFAUT = 4.0; // heures
    private static final double ALTITUDE_MAX_DEFAUT = 5000.0; // mètres
    private static final double SENSIBILITE_VENT_DEFAUT = 0.8;

    /** Portée de surveillance en mètres */
    private final double porteeSurveillance;

    /** Capacité de transport (par défaut 1 item pour les multis-trips) */
    private final int capaciteTransport = 1;

    /**
     * Constructeur avec paramètres par défaut.
     * 
     * @param position la position initiale
     */
    public DroneReconnaissance(Position3D position) {
        this(position, VITESSE_MAX_DEFAUT, AUTONOMIE_MAX_DEFAUT);
    }

    /**
     * Constructeur avec paramètres personnalisés.
     * 
     * @param position     la position initiale
     * @param vitesseMax   la vitesse maximale en m/s
     * @param autonomieMax l'autonomie maximale en heures
     */
    public DroneReconnaissance(Position3D position, double vitesseMax, double autonomieMax) {
        super(position, vitesseMax, autonomieMax, ALTITUDE_MAX_DEFAUT, SENSIBILITE_VENT_DEFAUT);
        this.porteeSurveillance = 2000.0; // 2 km de portée
    }

    public double getPorteeSurveillance() {
        return porteeSurveillance;
    }

    public int getCapaciteTransport() {
        return capaciteTransport;
    }

    /**
     * Désactive un actif cible si à proximité.
     * 
     * @param cible l'actif à désactiver
     * @return true si réussi, false sinon
     */
    public boolean desactiverActif(fr.spiga.core.ActifMobile cible) {
        if (cible == null || cible == this)
            return false;

        double distance = getPosition().distanceVers(cible.getPosition());
        if (distance < 100.0) { // Portée de désactivation 100m
            cible.setEtatOperationnel(fr.spiga.core.EtatOperationnel.EN_PANNE);
            cible.notifierEtatCritique("PANNE_SYSTEME");
            System.out.println("Drone " + getId() + " a désactivé " + cible.getId());
            return true;
        }
        return false;
    }

    @Override
    public String getType() {
        return "DroneReconnaissance";
    }
}
