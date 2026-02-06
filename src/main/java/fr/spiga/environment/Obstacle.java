package fr.spiga.environment;

import fr.spiga.core.Position3D;
import java.io.Serializable;

/**
 * Classe représentant un obstacle fixe dans la zone d'opération.
 * Les obstacles peuvent être des montagnes, structures, récifs, etc.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class Obstacle implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Position centrale de l'obstacle */
    private final Position3D position;

    /** Rayon de l'obstacle en mètres */
    private final double rayon;

    /** Altitude/Profondeur minimale */
    private final double zMin;

    /** Altitude/Profondeur maximale */
    private final double zMax;

    /** Type d'obstacle */
    private final String type;

    /**
     * Constructeur d'un obstacle.
     * 
     * @param position la position centrale
     * @param rayon    le rayon en mètres
     * @param type     le type d'obstacle
     */
    public Obstacle(Position3D position, double rayon, String type) {
        this(position, rayon, -Double.MAX_VALUE, Double.MAX_VALUE, type);
    }

    /**
     * Constructeur complet d'un obstacle avec limites de hauteur.
     * 
     * @param position la position centrale
     * @param rayon    le rayon en mètres
     * @param zMin     altitude minimale
     * @param zMax     altitude maximale
     * @param type     le type d'obstacle
     */
    public Obstacle(Position3D position, double rayon, double zMin, double zMax, String type) {
        if (position == null) {
            throw new IllegalArgumentException("La position ne peut pas être nulle");
        }
        if (rayon <= 0) {
            throw new IllegalArgumentException("Le rayon doit être positif");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Le type ne peut pas être vide");
        }

        this.position = position;
        this.rayon = rayon;
        this.zMin = zMin;
        this.zMax = zMax;
        this.type = type;
    }

    public Position3D getPosition() {
        return position;
    }

    public double getRayon() {
        return rayon;
    }

    public double getZMin() {
        return zMin;
    }

    public double getZMax() {
        return zMax;
    }

    public String getType() {
        return type;
    }

    /**
     * Vérifie si une position est en collision avec cet obstacle.
     * Prend en compte le rayon (cylindre) e a faixa de Z.
     * 
     * @param pos la position à vérifier
     * @return true si collision, false sinon
     */
    public boolean estEnCollision(Position3D pos) {
        // Vérifier d'abord la hauteur
        if (pos.getZ() < zMin || pos.getZ() > zMax) {
            return false;
        }

        // Vérifier le rayon (distance en 2D pour un cylindre vertical)
        double dx = pos.getX() - position.getX();
        double dy = pos.getY() - position.getY();
        double distance2D = Math.sqrt(dx * dx + dy * dy);

        return distance2D < rayon;
    }

    @Override
    public String toString() {
        return String.format("Obstacle[type=%s, position=%s, rayon=%.1fm]",
                type, position, rayon);
    }
}
