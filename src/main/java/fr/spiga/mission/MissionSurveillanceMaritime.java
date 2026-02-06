package fr.spiga.mission;

import fr.spiga.core.Position3D;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Mission de surveillance maritime.
 * Surveillance continue d'une zone maritime spécifique.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class MissionSurveillanceMaritime extends Mission {

    private final Position3D destination;
    private final List<fr.spiga.core.ActifMobile> actifsArrives = new java.util.ArrayList<>();

    public MissionSurveillanceMaritime(LocalDateTime debutPrevu, LocalDateTime finPrevue, Position3D destination) {
        super("SURVEILLANCE", debutPrevu, finPrevue, "Surveillance au point " + destination);

        if (destination == null) {
            throw new IllegalArgumentException("La destination ne peut pas être nulle");
        }
        this.destination = destination;
    }

    public Position3D getDestination() {
        return destination;
    }

    @Override
    public String getNom() {
        return "Mission Surveillance (Point-à-Point)";
    }

    @Override
    public boolean estCompatible(fr.spiga.core.ActifMobile actif) {
        // Tous les types d'actifs peuvent participer
        return true;
    }

    @Override
    protected void mettreAJourSpecifique(double dt) {
        if (getActifsAssignes().isEmpty())
            return;

        boolean tousArrives = true;

        for (fr.spiga.core.ActifMobile actif : getActifsAssignes()) {
            if (actif.getEtatOperationnel() != fr.spiga.core.EtatOperationnel.EN_MISSION)
                continue;

            if (!actifsArrives.contains(actif)) {
                // Mouvement vers le point
                boolean arrive = actif.avancerVers(destination, dt);

                // On considère arrivé si proche (car collisions évitent le point exact si
                // plusieurs arrivent)
                if (arrive || actif.getPosition().distanceVers(destination) < 100.0) {
                    if (!actifsArrives.contains(actif)) {
                        actifsArrives.add(actif);
                        actif.eteindre();
                        actif.setEtatOperationnel(fr.spiga.core.EtatOperationnel.AU_SOL);
                        System.out.println("Actif " + actif.getId() + " est arrivé au point de surveillance.");
                    }
                } else {
                    tousArrives = false;
                }
            } else {
                // Déjà arrivé, peut patrouiller ou s'arrêter. Pour l'instant s'arrête.
                // Pour éviter le spam console on ne fait rien
            }
        }

        if (tousArrives && !getActifsAssignes().isEmpty()) {
            // Optionnel : attente ou fin immédiate. Le user demande "ir de um ponto a
            // outro".
            terminer("Surveillance établie. Tous les actifs sont sur position.");
        }
    }
}
