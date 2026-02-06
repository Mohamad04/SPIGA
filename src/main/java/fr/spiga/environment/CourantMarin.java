package fr.spiga.environment;

import fr.spiga.core.Position3D;
import java.io.Serializable;

/**
 * Classe modélisant les courants marins dans la zone d'opération.
 * Les courants affectent les actifs marins (surface et sous-marins).
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class CourantMarin implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Direction et force du courant (utilisant Position3D comme vecteur) */
    private Position3D direction;

    /** Intensité du courant en pourcentage (0-100) */
    private double intensite;

    /**
     * Constructeur d'un courant marin.
     * 
     * @param direction la direction du courant (Position3D)
     * @param intensite l'intensité (0-100)
     */
    public CourantMarin(Position3D direction, double intensite) {
        if (direction == null) {
            throw new IllegalArgumentException("La direction ne peut pas être nulle");
        }
        if (intensite < 0 || intensite > 100) {
            throw new IllegalArgumentException("L'intensité doit être entre 0 et 100");
        }

        this.direction = direction;
        this.intensite = intensite;
    }

    public Position3D getDirection() {
        return direction;
    }

    public void setDirection(Position3D direction) {
        if (direction == null) {
            throw new IllegalArgumentException("La direction ne peut pas être nulle");
        }
        this.direction = direction;
    }

    public double getIntensite() {
        return intensite;
    }

    public void setIntensite(double intensite) {
        if (intensite < 0 || intensite > 100) {
            throw new IllegalArgumentException("L'intensité doit être entre 0 et 100");
        }
        this.intensite = intensite;
    }

    @Override
    public String toString() {
        return String.format("CourantMarin[direction=%s, intensité=%.1f%%]", direction, intensite);
    }
}
