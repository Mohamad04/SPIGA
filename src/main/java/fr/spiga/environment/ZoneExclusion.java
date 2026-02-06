package fr.spiga.environment;

import fr.spiga.core.Position3D;
import java.io.Serializable;

/**
 * Classe représentant une zone d'exclusion (zone interdite) dans la zone
 * d'opération.
 * Les actifs ne doivent pas entrer dans ces zones.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class ZoneExclusion implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Position centrale de la zone */
    private final Position3D centre;

    /** Rayon de la zone en mètres */
    private final double rayon;

    /** Nom ou description de la zone */
    private final String nom;

    /**
     * Constructeur d'une zone d'exclusion.
     * 
     * @param centre le centre de la zone
     * @param rayon  le rayon en mètres
     * @param nom    le nom de la zone
     */
    public ZoneExclusion(Position3D centre, double rayon, String nom) {
        if (centre == null) {
            throw new IllegalArgumentException("Le centre ne peut pas être nul");
        }
        if (rayon <= 0) {
            throw new IllegalArgumentException("Le rayon doit être positif");
        }
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        }

        this.centre = centre;
        this.rayon = rayon;
        this.nom = nom;
    }

    public Position3D getCentre() {
        return centre;
    }

    public double getRayon() {
        return rayon;
    }

    public String getNom() {
        return nom;
    }

    /**
     * Vérifie si une position est dans cette zone interdite.
     * 
     * @param position la position à vérifier
     * @return true si dans la zone, false sinon
     */
    public boolean contientPosition(Position3D position) {
        return centre.distanceVers(position) <= rayon;
    }

    @Override
    public String toString() {
        return String.format("ZoneExclusion[nom=%s, centre=%s, rayon=%.1fm]",
                nom, centre, rayon);
    }
}
