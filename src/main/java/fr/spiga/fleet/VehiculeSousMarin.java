package fr.spiga.fleet;

import fr.spiga.core.ActifMarin;
import fr.spiga.core.Position3D;

/**
 * Véhicule sous-marin autonome (AUV - Autonomous Underwater Vehicle).
 * Opère en 3D sous l'eau avec gestion critique de la profondeur.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class VehiculeSousMarin extends ActifMarin {

    private static final double VITESSE_MAX_DEFAUT = 20.0; // m/s (72 km/h)
    private static final double AUTONOMIE_MAX_DEFAUT = 10.0; // heures
    private static final double PROFONDEUR_MAX_DEFAUT = 1000.0; // mètres
    private static final double SENSIBILITE_COURANT_DEFAUT = 1.0;

    /** Résistance à la pression (facteur de sécurité) */
    private final double resistancePression;

    /**
     * Constructeur avec paramètres par défaut.
     * 
     * @param position la position initiale (Z négatif pour sous l'eau)
     */
    public VehiculeSousMarin(Position3D position) {
        this(position, VITESSE_MAX_DEFAUT, AUTONOMIE_MAX_DEFAUT);
    }

    /**
     * Constructeur avec paramètres personnalisés.
     * 
     * @param position     la position initiale (Z négatif pour sous l'eau)
     * @param vitesseMax   la vitesse maximale en m/s
     * @param autonomieMax l'autonomie maximale en heures
     */
    public VehiculeSousMarin(Position3D position, double vitesseMax, double autonomieMax) {
        super(position, vitesseMax, autonomieMax, PROFONDEUR_MAX_DEFAUT, SENSIBILITE_COURANT_DEFAUT);
        this.resistancePression = 1.2;
    }

    public double getResistancePression() {
        return resistancePression;
    }

    /**
     * Fait surface le véhicule.
     * 
     * @return true si la remontée a réussi, false sinon
     */
    public boolean faireSurface() {
        Position3D surface = new Position3D(getPosition().getX(), getPosition().getY(), 0.0);
        return deplacer(surface);
    }

    /**
     * Plonge à une profondeur donnée.
     * 
     * @param profondeur la profondeur cible en mètres (valeur positive)
     * @return true si la plongée a réussi, false sinon
     */
    public boolean plonger(double profondeur) {
        if (profondeur < 0 || profondeur > getProfondeurMax()) {
            return false;
        }
        Position3D cible = new Position3D(getPosition().getX(), getPosition().getY(), -profondeur);
        return deplacer(cible);
    }

    @Override
    public String getType() {
        return "VehiculeSousMarin";
    }
}
