package fr.spiga.fleet;

import fr.spiga.core.ActifMobile;
import fr.spiga.core.Position3D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Classe représentant un essaim (swarm) d'actifs mobiles hétérogènes.
 * Un essaim coordonne plusieurs actifs pour accomplir des missions ensemble.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class Essaim implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Identifiant unique de l'essaim */
    private final String id;

    /** Nom de l'essaim */
    private String nom;

    /** Liste des actifs membres de l'essaim */
    private final List<ActifMobile> actifs;

    /** Distance minimale entre actifs pour éviter les collisions (en mètres) */
    private static final double DISTANCE_SECURITE = 50.0;

    /**
     * Constructeur d'un essaim.
     * 
     * @param nom le nom de l'essaim
     */
    public Essaim(String nom) {
        this.id = UUID.randomUUID().toString();
        this.nom = nom != null ? nom : "Essaim-" + id.substring(0, 8);
        this.actifs = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        if (nom != null && !nom.isBlank()) {
            this.nom = nom;
        }
    }

    /**
     * Ajoute un actif à l'essaim.
     * 
     * @param actif l'actif à ajouter
     * @return true si ajout réussi, false sinon
     */
    public boolean ajouterActif(ActifMobile actif) {
        if (actif == null || actifs.contains(actif)) {
            return false;
        }
        return actifs.add(actif);
    }

    /**
     * Retire un actif de l'essaim.
     * 
     * @param actif l'actif à retirer
     * @return true si retrait réussi, false sinon
     */
    public boolean retirerActif(ActifMobile actif) {
        return actifs.remove(actif);
    }

    /**
     * Obtient la liste des actifs (copie défensive).
     * 
     * @return la liste des actifs
     */
    public List<ActifMobile> getActifs() {
        return new ArrayList<>(actifs);
    }

    /**
     * Obtient le nombre d'actifs dans l'essaim.
     * 
     * @return le nombre d'actifs
     */
    public int getNombreActifs() {
        return actifs.size();
    }

    /**
     * Vérifie les risques de collision entre les actifs de l'essaim.
     * 
     * @return la liste des paires d'actifs en risque de collision
     */
    public List<String> verifierCollisions() {
        List<String> alertes = new ArrayList<>();

        for (int i = 0; i < actifs.size(); i++) {
            for (int j = i + 1; j < actifs.size(); j++) {
                ActifMobile actif1 = actifs.get(i);
                ActifMobile actif2 = actifs.get(j);

                // On ne vérifie que les actifs qui bougent (ou au moins un des deux)
                if (actif1.getEtatOperationnel() != fr.spiga.core.EtatOperationnel.EN_MISSION &&
                        actif2.getEtatOperationnel() != fr.spiga.core.EtatOperationnel.EN_MISSION) {
                    continue;
                }

                double distance = actif1.getPosition().distanceVers(actif2.getPosition());

                if (distance < 10.0) { // Collision réelle
                    if (actif1.getEtatOperationnel() != fr.spiga.core.EtatOperationnel.EN_PANNE) {
                        actif1.setEtatOperationnel(fr.spiga.core.EtatOperationnel.EN_PANNE);
                        actif1.notifierEtatCritique("COLLISION_VEHICULE");
                    }
                    if (actif2.getEtatOperationnel() != fr.spiga.core.EtatOperationnel.EN_PANNE) {
                        actif2.setEtatOperationnel(fr.spiga.core.EtatOperationnel.EN_PANNE);
                        actif2.notifierEtatCritique("COLLISION_VEHICULE");
                    }
                    alertes.add(String.format("!!! COLLISION !!! %s e %s bateram!",
                            actif1.getId(), actif2.getId()));
                } else if (distance < DISTANCE_SECURITE) {
                    alertes.add(String.format("Risque de collision: %s e %s (%.1fm)",
                            actif1.getId(), actif2.getId(), distance));
                }
            }
        }

        return alertes;
    }

    /**
     * Calcule le centre de l'essaim (position moyenne).
     * 
     * @return le centre de l'essaim, ou null si essaim vide
     */
    public Position3D calculerCentre() {
        if (actifs.isEmpty()) {
            return null;
        }

        double sumX = 0, sumY = 0, sumZ = 0;
        for (ActifMobile actif : actifs) {
            Position3D pos = actif.getPosition();
            sumX += pos.getX();
            sumY += pos.getY();
            sumZ += pos.getZ();
        }

        int n = actifs.size();
        return new Position3D(sumX / n, sumY / n, sumZ / n);
    }

    /**
     * Calcule l'autonomie moyenne de l'essaim.
     * 
     * @return l'autonomie moyenne en pourcentage
     */
    public double calculerAutonomieMoyenne() {
        if (actifs.isEmpty()) {
            return 0.0;
        }

        double somme = 0;
        for (ActifMobile actif : actifs) {
            somme += actif.getAutonomieRestante();
        }

        return somme / actifs.size();
    }

    @Override
    public String toString() {
        return String.format("Essaim[nom=%s, actifs=%d, autonomieMoyenne=%.1f%%]",
                nom, actifs.size(), calculerAutonomieMoyenne());
    }
}
