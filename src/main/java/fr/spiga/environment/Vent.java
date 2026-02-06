package fr.spiga.environment;

import fr.spiga.core.Position3D;
import java.io.Serializable;

/**
 * Classe modélisant le vent dans la zone d'opération.
 * Le vent affecte les actifs aériens et de surface.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class Vent implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Direction et force du vent (Position3D comme vecteur 2D, Z=0) */
    private Position3D direction;

    /** Intensité du vent en pourcentage (0-100) */
    private double intensite;

    /**
     * Constructeur d'un vent.
     * 
     * @param direction la direction du vent (Position3D)
     * @param intensite l'intensité (0-100)
     */
    public Vent(Position3D direction, double intensite) {
        if (direction == null) {
            throw new IllegalArgumentException("La direction ne peut pas être nulle");
        }
        if (intensite < 0 || intensite > 100) {
            throw new IllegalArgumentException("L'intensité doit être entre 0 et 100");
        }

        this.direction = direction;
        this.intensite = intensite;
    }

    /**
     * Constructeur avec paramètres simplifiés.
     * 
     * @param angleRadians l'angle en radians
     * @param intensite    l'intensité (0-100)
     */
    public Vent(double angleRadians, double intensite) {
        this(new Position3D(Math.cos(angleRadians), Math.sin(angleRadians), 0.0), intensite);
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
        return String.format("Vent[direction=%s, intensité=%.1f%%]", direction, intensite);
    }
}
