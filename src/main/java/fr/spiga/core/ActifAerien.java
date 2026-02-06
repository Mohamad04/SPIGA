package fr.spiga.core;

import fr.spiga.environment.Vent;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe abstraite représentant un actif aérien.
 * Les drones et autres véhicules aériens héritent de cette classe.
 * 
 * <p>
 * Les actifs aériens évoluent principalement en 2D (X, Y) avec une composante
 * Z (altitude). Ils sont sensibles aux facteurs atmosphériques comme le vent.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public abstract class ActifAerien extends ActifMobile {

    /** Altitude maximale opérationnelle en mètres */
    private final double altitudeMax;

    /** Sensibilité au vent (facteur multiplicateur de consommation) */
    private final double sensibiliteVent;

    /**
     * Constructeur pour les actifs aériens.
     * 
     * @param position        la position initiale
     * @param vitesseMax      la vitesse maximale en m/s
     * @param autonomieMax    l'autonomie maximale en heures
     * @param altitudeMax     l'altitude maximale en mètres
     * @param sensibiliteVent le facteur de sensibilité au vent (1.0 = normal)
     */
    protected ActifAerien(Position3D position, double vitesseMax, double autonomieMax,
            double altitudeMax, double sensibiliteVent) {
        super(position, vitesseMax, autonomieMax);

        if (altitudeMax <= 0) {
            throw new IllegalArgumentException("L'altitude maximale doit être positive");
        }
        if (sensibiliteVent < 0) {
            throw new IllegalArgumentException("La sensibilité au vent ne peut pas être négative");
        }

        this.altitudeMax = altitudeMax;
        this.sensibiliteVent = sensibiliteVent;
    }

    public double getAltitudeMax() {
        return altitudeMax;
    }

    public double getSensibiliteVent() {
        return sensibiliteVent;
    }

    /**
     * Obtient l'altitude actuelle de l'actif.
     * 
     * @return l'altitude actuelle en mètres
     */
    public double getAltitude() {
        return getPosition().getZ();
    }

    @Override
    protected void setPosition(Position3D nouvellePosition) {
        if (nouvellePosition == null) {
            throw new IllegalArgumentException("La position ne peut pas être nulle");
        }
        if (!estAltitudeValide(nouvellePosition.getZ())) {
            throw new IllegalArgumentException("Altitude invalide pour cet actif aérien: " + nouvellePosition.getZ());
        }
        super.setPosition(nouvellePosition);
    }

    /**
     * Vérifie si l'altitude est valide pour cet actif.
     * 
     * @param altitude l'altitude à vérifier
     * @return true si valide, false sinon
     */
    protected boolean estAltitudeValide(double altitude) {
        return altitude >= 0 && altitude <= altitudeMax;
    }

    @Override
    public boolean deplacer(Position3D cible) {
        if (cible == null) {
            return false;
        }

        // Vérifier l'altitude
        if (!estAltitudeValide(cible.getZ())) {
            System.out.println("Altitude cible invalide: " + cible.getZ() +
                    " (max: " + altitudeMax + ")");
            notifierEtatCritique("ALTITUDE_INVALIDE");
            return false;
        }

        // Vérifier l'autonomie
        double distance = getPosition().distanceVers(cible);
        double consommation = calculerConsommation(distance);

        if (consommation > getAutonomieRestante()) {
            System.out.println("Autonomie insuffisante pour atteindre la cible");
            notifierEtatCritique("BATTERIE_CRITIQUE");
            return false;
        }

        // Vérifier la zone d'opération
        if (zoneOperation != null && !zoneOperation.estDansZone(cible)) {
            System.out.println("Cible hors de la zone d'opération");
            notifierEtatCritique("ZONE_INTERDITE");
            return false;
        }

        // Effectuer le déplacement
        setPosition(cible);
        consommerAutonomie(consommation);

        return true;
    }

    @Override
    public List<Position3D> calculerTrajet(Position3D cible) {
        List<Position3D> trajet = new ArrayList<>();
        trajet.add(getPosition());

        // Trajet simple en ligne droite
        // Dans une implémentation avancée, on pourrait éviter les obstacles
        trajet.add(cible);

        return trajet;
    }

    @Override
    protected double calculerConsommation(double distance) {
        double consommationBase = super.calculerConsommation(distance);

        // Ajuster selon le vent (sensibilité)
        if (zoneOperation != null) {
            Vent vent = zoneOperation.getVent();
            if (vent != null) {
                // Si on va CONTRE le vent, on consomme plus.
                // Pour simplifier ici, on garde le facteur d'intensité
                double facteurVent = 1.0 + (vent.getIntensite() / 100.0) * sensibiliteVent;
                consommationBase *= facteurVent;
            }

            // Précipitations : la pluie alourdit et augmente la traînée
            fr.spiga.environment.Precipitation precip = zoneOperation.getPrecipitation();
            if (precip != null && precip.getIntensite() > 0) {
                consommationBase *= (1.0 + precip.getIntensite() / 200.0);
            }
        }

        // Ajuster selon l'altitude (plus haut = plus de consommation)
        double altitude = getPosition().getZ();
        double facteurAltitude = 1.0 + (altitude / altitudeMax) * 0.2;
        consommationBase *= facteurAltitude;

        return consommationBase;
    }

    @Override
    public String toString() {
        return String.format("%s[altitude=%.1fm, altitudeMax=%.1fm]",
                super.toString(), getAltitude(), altitudeMax);
    }
}
