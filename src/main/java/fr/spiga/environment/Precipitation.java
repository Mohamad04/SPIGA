package fr.spiga.environment;

import java.io.Serializable;

/**
 * Classe modélisant les précipitations dans la zone d'opération.
 * Les précipitations affectent principalement les actifs aériens.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class Precipitation implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Intensité des précipitations en pourcentage (0-100) */
    private double intensite;

    /** Type de précipitation */
    private TypePrecipitation type;

    /**
     * Énumération des types de précipitations.
     */
    public enum TypePrecipitation {
        AUCUNE,
        PLUIE_LEGERE,
        PLUIE_MODEREE,
        PLUIE_FORTE,
        NEIGE,
        GRELE
    }

    /**
     * Constructeur d'une précipitation.
     * 
     * @param type      le type de précipitation
     * @param intensite l'intensité (0-100)
     */
    public Precipitation(TypePrecipitation type, double intensite) {
        if (type == null) {
            throw new IllegalArgumentException("Le type ne peut pas être nul");
        }
        if (intensite < 0 || intensite > 100) {
            throw new IllegalArgumentException("L'intensité doit être entre 0 et 100");
        }

        this.type = type;
        this.intensite = intensite;
    }

    /**
     * Constructeur sans précipitation.
     */
    public Precipitation() {
        this(TypePrecipitation.AUCUNE, 0.0);
    }

    public TypePrecipitation getType() {
        return type;
    }

    public void setType(TypePrecipitation type) {
        if (type == null) {
            throw new IllegalArgumentException("Le type ne peut pas être nul");
        }
        this.type = type;
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
        return String.format("Precipitation[type=%s, intensité=%.1f%%]", type, intensite);
    }
}
