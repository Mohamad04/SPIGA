package fr.spiga.core;

import java.io.Serializable;
import java.util.Objects;

/**
 * Classe représentant une position en trois dimensions (X, Y, Z).
 * Cette classe est immuable et sérialisable.
 * 
 * <p>
 * Les coordonnées sont en mètres :
 * <ul>
 * <li>X : coordonnée est-ouest</li>
 * <li>Y : coordonnée nord-sud</li>
 * <li>Z : altitude (positive vers le haut) ou profondeur (négative sous
 * l'eau)</li>
 * </ul>
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public final class Position3D implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double x;
    private final double y;
    private final double z;

    /**
     * Constructeur d'une position 3D.
     * 
     * @param x la coordonnée X en mètres
     * @param y la coordonnée Y en mètres
     * @param z la coordonnée Z en mètres (altitude/profondeur)
     */
    public Position3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Obtient la coordonnée X.
     * 
     * @return la coordonnée X
     */
    public double getX() {
        return x;
    }

    /**
     * Obtient la coordonnée Y.
     * 
     * @return la coordonnée Y
     */
    public double getY() {
        return y;
    }

    /**
     * Obtient la coordonnée Z.
     * 
     * @return la coordonnée Z
     */
    public double getZ() {
        return z;
    }

    /**
     * Calcule la distance euclidienne 3D vers une autre position.
     * 
     * @param autre l'autre position
     * @return la distance en mètres
     */
    public double distanceVers(Position3D autre) {
        double dx = autre.x - this.x;
        double dy = autre.y - this.y;
        double dz = autre.z - this.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calcule la distance 2D (sans tenir compte de Z) vers une autre position.
     * Utile pour les calculs de distance au sol.
     * 
     * @param autre l'autre position
     * @return la distance 2D en mètres
     */
    public double distance2DVers(Position3D autre) {
        double dx = autre.x - this.x;
        double dy = autre.y - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calcule la norme (magnitude) de la position comme s'il s'agissait d'un
     * vecteur.
     * 
     * @return la norme
     */
    public double norme() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Ajoute une autre position à celle-ci.
     * 
     * @param autre l'autre position
     * @return une nouvelle position résultant de l'addition
     */
    public Position3D plus(Position3D autre) {
        return new Position3D(this.x + autre.x, this.y + autre.y, this.z + autre.z);
    }

    /**
     * Soustrait une autre position de celle-ci.
     * 
     * @param autre l'autre position
     * @return une nouvelle position résultant de la soustraction
     */
    public Position3D moins(Position3D autre) {
        return new Position3D(this.x - autre.x, this.y - autre.y, this.z - autre.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Position3D that = (Position3D) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("Position3D(x=%.2f, y=%.2f, z=%.2f)", x, y, z);
    }
}
