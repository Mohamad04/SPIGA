package fr.spiga.core;

import fr.spiga.environment.CourantMarin;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe abstraite représentant un actif marin.
 * Les véhicules de surface et sous-marins héritent de cette classe.
 * 
 * <p>
 * Les actifs marins sont sensibles aux courants marins et à la profondeur.
 * La profondeur est représentée par une valeur Z négative.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public abstract class ActifMarin extends ActifMobile {

    /** Profondeur maximale opérationnelle en mètres (valeur positive) */
    private final double profondeurMax;

    /** Sensibilité aux courants marins (facteur multiplicateur de consommation) */
    private final double sensibiliteCourant;

    /**
     * Constructeur pour les actifs marins.
     * 
     * @param position           la position initiale
     * @param vitesseMax         la vitesse maximale en m/s
     * @param autonomieMax       l'autonomie maximale en heures
     * @param profondeurMax      la profondeur maximale en mètres (valeur positive)
     * @param sensibiliteCourant le facteur de sensibilité au courant (1.0 = normal)
     */
    protected ActifMarin(Position3D position, double vitesseMax, double autonomieMax,
            double profondeurMax, double sensibiliteCourant) {
        super(position, vitesseMax, autonomieMax);

        if (profondeurMax < 0) {
            throw new IllegalArgumentException("La profondeur maximale doit être positive");
        }
        if (sensibiliteCourant < 0) {
            throw new IllegalArgumentException("La sensibilité au courant ne peut pas être négative");
        }

        this.profondeurMax = profondeurMax;
        this.sensibiliteCourant = sensibiliteCourant;
    }

    public double getProfondeurMax() {
        return profondeurMax;
    }

    public double getSensibiliteCourant() {
        return sensibiliteCourant;
    }

    /**
     * Obtient la profondeur actuelle de l'actif.
     * Retourne une valeur positive (l'opposé de Z).
     * 
     * @return la profondeur actuelle en mètres
     */
    public double getProfondeur() {
        return -getPosition().getZ();
    }

    @Override
    protected void setPosition(Position3D nouvellePosition) {
        if (nouvellePosition == null) {
            throw new IllegalArgumentException("La position ne peut pas être nulle");
        }
        // Pour un actif marin, la profondeur est -Z
        if (!estProfondeurValide(-nouvellePosition.getZ())) {
            throw new IllegalArgumentException("Profondeur invalide pour cet actif marin: " + -nouvellePosition.getZ());
        }
        super.setPosition(nouvellePosition);
    }

    /**
     * Vérifie si la profondeur est valide pour cet actif.
     * 
     * @param profondeur la profondeur à vérifier (valeur positive)
     * @return true si valide, false sinon
     */
    protected boolean estProfondeurValide(double profondeur) {
        // La profondeur doit être positive (Z négatif) et <= max
        // Note: La validation Z < 0 est implicite ici si on veut forcer
        // l'immersion/surface
        return profondeur >= -0.1 && profondeur <= profondeurMax; // Tolérance de 0.1m
    }

    @Override
    public boolean deplacer(Position3D cible) {
        if (cible == null) {
            return false;
        }

        // Vérifier la profondeur (Z négatif ou nul pour les actifs marins)
        double profondeurCible = -cible.getZ();
        if (!estProfondeurValide(profondeurCible)) {
            System.out.println("Profondeur cible invalide: " + profondeurCible +
                    " (max: " + profondeurMax + ")");
            notifierEtatCritique("PROFONDEUR_INVALIDE");
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

        // Ajuster selon le courant marin si dans une zone d'opération
        if (zoneOperation != null) {
            CourantMarin courant = zoneOperation.getCourantMarin();
            if (courant != null) {
                double facteurCourant = 1.0 + (courant.getIntensite() / 100.0) * sensibiliteCourant;
                consommationBase *= facteurCourant;
            }
        }

        // Ajuster selon la profondeur (plus profond = plus de consommation)
        double profondeur = getProfondeur();
        if (profondeurMax > 0) {
            double facteurProfondeur = 1.0 + (profondeur / profondeurMax) * 0.3;
            consommationBase *= facteurProfondeur;
        }

        return consommationBase;
    }

    @Override
    public String toString() {
        return String.format("%s[profondeur=%.1fm, profondeurMax=%.1fm]",
                super.toString(), getProfondeur(), profondeurMax);
    }
}
