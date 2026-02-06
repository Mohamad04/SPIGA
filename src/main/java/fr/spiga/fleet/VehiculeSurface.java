package fr.spiga.fleet;

import fr.spiga.core.ActifMarin;
import fr.spiga.core.Position3D;

/**
 * Véhicule de surface autonome (ASV - Autonomous Surface Vehicle).
 * Opère à la surface de l'eau (Z=0) et est sensible au vent et à l'état de la
 * mer.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class VehiculeSurface extends ActifMarin {

    private static final double VITESSE_MAX_DEFAUT = 25.0; // m/s (90 km/h)
    private static final double AUTONOMIE_MAX_DEFAUT = 12.0; // heures
    private static final double PROFONDEUR_MAX_DEFAUT = 0.0; // Surface uniquement
    private static final double SENSIBILITE_COURANT_DEFAUT = 1.5;

    /** Stabilité en mer (facteur de résistance aux vagues) */
    private final double stabilite;

    /**
     * Constructeur avec paramètres par défaut.
     * 
     * @param position la position initiale (Z doit être 0)
     */
    public VehiculeSurface(Position3D position) {
        this(position, VITESSE_MAX_DEFAUT, AUTONOMIE_MAX_DEFAUT);
    }

    /**
     * Constructeur avec paramètres personnalisés.
     * 
     * @param position     la position initiale (Z doit être 0)
     * @param vitesseMax   la vitesse maximale en m/s
     * @param autonomieMax l'autonomie maximale en heures
     */
    public VehiculeSurface(Position3D position, double vitesseMax, double autonomieMax) {
        super(position, vitesseMax, autonomieMax, PROFONDEUR_MAX_DEFAUT, SENSIBILITE_COURANT_DEFAUT);

        // Forcer Z=0 pour véhicule de surface
        if (position.getZ() != 0) {
            setPosition(new Position3D(position.getX(), position.getY(), 0.0));
        }

        this.stabilite = 0.8;
    }

    public double getStabilite() {
        return stabilite;
    }

    @Override
    public boolean deplacer(Position3D cible) {
        // Les véhicules de surface doivent rester en surface
        Position3D cibleSurface = new Position3D(cible.getX(), cible.getY(), 0.0);
        return super.deplacer(cibleSurface);
    }

    @Override
    protected double calculerConsommation(double distance) {
        double conso = super.calculerConsommation(distance);

        // Sensibilité au vent (État de la mer)
        // Le vent crée des vagues qui ralentissent/augmentent la consommation
        if (zoneOperation != null && zoneOperation.getVent() != null) {
            double facteurVent = 1.0 + (zoneOperation.getVent().getIntensite() / 100.0) * 0.5; // +50% max
            conso *= facteurVent;
        }
        return conso;
    }

    @Override
    public String getType() {
        return "VehiculeSurface";
    }
}
