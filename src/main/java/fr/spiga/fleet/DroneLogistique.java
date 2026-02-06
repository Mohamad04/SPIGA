package fr.spiga.fleet;

import fr.spiga.core.ActifAerien;
import fr.spiga.core.Position3D;

/**
 * Drone logistique spécialisé en transport de charge utile.
 * Caractérisé par une autonomie élevée et une consommation optimisée,
 * mais une vitesse réduite due à la charge.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class DroneLogistique extends ActifAerien {

    private static final double VITESSE_MAX_DEFAUT = 35.0; // m/s (126 km/h)
    private static final double AUTONOMIE_MAX_DEFAUT = 12.0; // heures
    private static final double ALTITUDE_MAX_DEFAUT = 3000.0; // mètres
    private static final double SENSIBILITE_VENT_DEFAUT = 1.2; // Plus sensible au vent

    /** Capacité de charge en kg */
    private final double capaciteCharge;

    /** Charge actuelle transportée en kg */
    private double chargeActuelle;

    /**
     * Constructeur avec paramètres par défaut.
     * 
     * @param position la position initiale
     */
    public DroneLogistique(Position3D position) {
        this(position, VITESSE_MAX_DEFAUT, AUTONOMIE_MAX_DEFAUT);
    }

    /**
     * Constructeur avec paramètres personnalisés.
     * 
     * @param position     la position initiale
     * @param vitesseMax   la vitesse maximale en m/s
     * @param autonomieMax l'autonomie maximale en heures
     */
    public DroneLogistique(Position3D position, double vitesseMax, double autonomieMax) {
        super(position, vitesseMax, autonomieMax, ALTITUDE_MAX_DEFAUT, SENSIBILITE_VENT_DEFAUT);
        this.capaciteCharge = 50.0; // 50 kg de capacité
        this.chargeActuelle = 0.0;
    }

    public double getCapaciteCharge() {
        return capaciteCharge;
    }

    public double getChargeActuelle() {
        return chargeActuelle;
    }

    /**
     * Charge du matériel sur le drone.
     * 
     * @param poids le poids à charger en kg
     * @return true si le chargement a réussi, false sinon
     */
    public boolean charger(double poids) {
        if (poids < 0 || chargeActuelle + poids > capaciteCharge) {
            return false;
        }
        chargeActuelle += poids;
        return true;
    }

    /**
     * Décharge du matériel du drone.
     * 
     * @param poids le poids à décharger en kg
     * @return true si le déchargement a réussi, false sinon
     */
    public boolean decharger(double poids) {
        if (poids < 0 || poids > chargeActuelle) {
            return false;
        }
        chargeActuelle -= poids;
        return true;
    }

    @Override
    protected double calculerConsommation(double distance) {
        double consommationBase = super.calculerConsommation(distance);

        // Optimisation énergétique du drone logistique (-20%)
        consommationBase *= 0.8;

        // La charge augmente la consommation
        if (capaciteCharge > 0) {
            double facteurCharge = 1.0 + (chargeActuelle / capaciteCharge) * 0.5;
            consommationBase *= facteurCharge;
        }

        return consommationBase;
    }

    @Override
    public String getType() {
        return "DroneLogistique";
    }
}
