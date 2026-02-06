package fr.spiga.mission;

import java.util.List;

import fr.spiga.core.ActifMobile;
import fr.spiga.core.Position3D;
import fr.spiga.fleet.DroneReconnaissance;
import java.time.LocalDateTime;

/**
 * Mission de reconnaissance aérienne.
 * Patrouille une zone rectangulaire à haute altitude pour collecter des
 * données.
 * 
 * @author SPIGA Team
 * @version 1.1
 */
public class MissionReconnaissanceAerienne extends Mission {

    private final Position3D destination;
    private final List<fr.spiga.core.ActifMobile> actifsArrives = new java.util.ArrayList<>();

    public MissionReconnaissanceAerienne(LocalDateTime debutPrevu, LocalDateTime finPrevue, Position3D destination) {
        super("SURVEILLANCE", debutPrevu, finPrevue, "Reconnaissance aérienne vers " + destination);
        if (destination == null) {
            throw new IllegalArgumentException("La destination ne peut pas être nulle");
        }
        this.destination = destination;
    }

    @Override
    public boolean estCompatible(ActifMobile actif) {
        // Uniquement pour les drones (aériens)
        return actif instanceof fr.spiga.core.ActifAerien;
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

                if (arrive || actif.getPosition().distanceVers(destination) < 100.0) {
                    if (!actifsArrives.contains(actif)) {
                        actifsArrives.add(actif);
                        actif.eteindre();
                        actif.setEtatOperationnel(fr.spiga.core.EtatOperationnel.AU_SOL);
                        System.out.println("Drone " + actif.getId() + " est arrivé au point de reconnaissance.");
                    }
                } else {
                    tousArrives = false;
                }
            }
        }

        if (tousArrives && !getActifsAssignes().isEmpty()) {
            terminer("Reconnaissance terminée. Tous les drones sont sur zone.");
        }
    }

    @Override
    public String getNom() {
        return "Mission Reconnaissance Aérienne";
    }
}
