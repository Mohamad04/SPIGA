package fr.spiga.interfaces;

import fr.spiga.core.Position3D;
import java.util.List;

/**
 * Interface définissant le contrat pour les entités déplaçables.
 * Tout actif mobile capable de se déplacer doit implémenter cette interface.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public interface Deplacable {

    /**
     * Déplace l'actif vers une position cible.
     * 
     * @param cible la position de destination
     * @return true si le déplacement a réussi, false sinon
     */
    boolean deplacer(Position3D cible);

    /**
     * Calcule le trajet optimal vers une position cible.
     * 
     * @param cible la position de destination
     * @return une liste de positions représentant le trajet
     */
    List<Position3D> calculerTrajet(Position3D cible);

    /**
     * Obtient la position actuelle de l'actif.
     * 
     * @return la position actuelle
     */
    Position3D getPosition();
}
