package fr.spiga.mission;

import fr.spiga.core.Position3D;
import fr.spiga.fleet.VehiculeSousMarin;
import java.time.LocalDateTime;

import java.util.List; // Added this line

/**
 * Mission d'inspection sous-marine.
 * Cette mission consiste à passer par une série de points de passage
 * (waypoints)
 * à des profondeurs variables pour inspecter des infrastructures immergées.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class MissionInspectionSousMarine extends Mission {

    private final Position3D destination;
    private final List<fr.spiga.core.ActifMobile> actifsArrives = new java.util.ArrayList<>();

    public MissionInspectionSousMarine(LocalDateTime debutPrevu, LocalDateTime finPrevue, Position3D destination) {
        super("RECONNAISSANCE", debutPrevu, finPrevue, "Inspection sous-marine au point " + destination);
        if (destination == null) {
            throw new IllegalArgumentException("La destination ne peut pas être nulle");
        }
        this.destination = destination;
    }

    @Override
    public String getNom() {
        return "Mission Inspection Sous-Marine";
    }

    @Override
    public boolean estCompatible(fr.spiga.core.ActifMobile actif) {
        // Seuls les véhicules sous-marins peuvent effectuer cette mission
        return actif instanceof VehiculeSousMarin;
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

                if (arrive || actif.getPosition().distanceVers(destination) < 50.0) {
                    if (!actifsArrives.contains(actif)) {
                        actifsArrives.add(actif);
                        actif.eteindre();
                        actif.setEtatOperationnel(fr.spiga.core.EtatOperationnel.AU_SOL);
                        System.out.println("Submersible " + actif.getId() + " est arrivé au point d'inspection.");
                    }
                } else {
                    tousArrives = false;
                }
            }
        }

        if (tousArrives && !getActifsAssignes().isEmpty()) {
            terminer("Inspection terminée. Tous les submersibles sont sur zone.");
        }
    }
}
