package fr.spiga.fleet;

import fr.spiga.core.ActifMobile;
import fr.spiga.core.EtatOperationnel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestionnaire central de la flotte d'actifs mobiles et des essaims.
 * Responsable de la création, gestion et optimisation des essaims.
 * 
 * <p>
 * Implémente le principe SRP (Single Responsibility) en se concentrant
 * uniquement sur la gestion de flotte.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class GestionnaireEssaim implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Liste de tous les actifs gérés */
    private final List<ActifMobile> tousLesActifs;

    /** Liste des essaims créés */
    private final List<Essaim> essaims;

    /**
     * Constructeur du gestionnaire d'essaim.
     */
    public GestionnaireEssaim() {
        this.tousLesActifs = new ArrayList<>();
        this.essaims = new ArrayList<>();
    }

    /**
     * Enregistre un nouvel actif dans la flotte.
     * 
     * @param actif l'actif à enregistrer
     * @return true si enregistrement réussi, false sinon
     */
    public boolean enregistrerActif(ActifMobile actif) {
        if (actif == null || tousLesActifs.contains(actif)) {
            return false;
        }
        return tousLesActifs.add(actif);
    }

    /**
     * Crée un nouvel essaim.
     * 
     * @param nom le nom de l'essaim
     * @return l'essaim créé
     */
    public Essaim creerEssaim(String nom) {
        Essaim essaim = new Essaim(nom);
        essaims.add(essaim);
        return essaim;
    }

    /**
     * Obtient tous les actifs disponibles (non en mission, non en panne).
     * 
     * @return la liste des actifs disponibles
     */
    public List<ActifMobile> getActifsDisponibles() {
        return tousLesActifs.stream()
                .filter(actif -> actif.getEtatOperationnel() == EtatOperationnel.AU_SOL)
                .collect(Collectors.toList());
    }

    /**
     * Obtient tous les actifs en mission.
     * 
     * @return la liste des actifs en mission
     */
    public List<ActifMobile> getActifsEnMission() {
        return tousLesActifs.stream()
                .filter(actif -> actif.getEtatOperationnel() == EtatOperationnel.EN_MISSION)
                .collect(Collectors.toList());
    }

    /**
     * Obtient tous les actifs en panne ou maintenance.
     * 
     * @return la liste des actifs nécessitant attention
     */
    public List<ActifMobile> getActifsNecessitantMaintenance() {
        return tousLesActifs.stream()
                .filter(actif -> actif.getEtatOperationnel() == EtatOperationnel.EN_PANNE ||
                        actif.getEtatOperationnel() == EtatOperationnel.EN_MAINTENANCE)
                .collect(Collectors.toList());
    }

    /**
     * Sélectionne l'actif optimal selon l'autonomie restante.
     * 
     * @return l'actif avec la meilleure autonomie, ou null si aucun disponible
     */
    public ActifMobile selectionnerActifOptimalParAutonomie() {
        return getActifsDisponibles().stream()
                .max(Comparator.comparingDouble(ActifMobile::getAutonomieRestante))
                .orElse(null);
    }

    /**
     * Sélectionne l'actif optimal d'un type donné selon l'autonomie.
     * 
     * @param typeClass la classe du type d'actif recherché
     * @return l'actif optimal du type spécifié, ou null si aucun disponible
     */
    public ActifMobile selectionnerActifOptimalParType(Class<? extends ActifMobile> typeClass) {
        return getActifsDisponibles().stream()
                .filter(typeClass::isInstance)
                .max(Comparator.comparingDouble(ActifMobile::getAutonomieRestante))
                .orElse(null);
    }

    /**
     * Vérifie les collisions dans tous les essaims.
     * 
     * @return la liste de toutes les alertes de collision
     */
    public List<String> verifierToutesLesCollisions() {
        List<String> toutesLesAlertes = new ArrayList<>();

        for (Essaim essaim : essaims) {
            List<String> alertes = essaim.verifierCollisions();
            if (!alertes.isEmpty()) {
                toutesLesAlertes.add("Essaim: " + essaim.getNom());
                toutesLesAlertes.addAll(alertes);
            }
        }

        return toutesLesAlertes;
    }

    /**
     * Recharge tous les actifs au sol.
     */
    public void rechargerTousLesActifsAuSol() {
        tousLesActifs.stream()
                .filter(actif -> actif.getEtatOperationnel() == EtatOperationnel.AU_SOL)
                .forEach(ActifMobile::recharger);
    }

    /**
     * Obtient des statistiques sur la flotte.
     * 
     * @return un rapport textuel des statistiques
     */
    public String genererRapportFlotte() {
        long disponibles = tousLesActifs.stream()
                .filter(a -> a.getEtatOperationnel() == EtatOperationnel.AU_SOL)
                .count();
        long enMission = tousLesActifs.stream()
                .filter(a -> a.getEtatOperationnel() == EtatOperationnel.EN_MISSION)
                .count();
        long enPanne = tousLesActifs.stream()
                .filter(a -> a.getEtatOperationnel() == EtatOperationnel.EN_PANNE)
                .count();
        long enMaintenance = tousLesActifs.stream()
                .filter(a -> a.getEtatOperationnel() == EtatOperationnel.EN_MAINTENANCE)
                .count();

        return String.format(
                "=== Rapport de Flotte ===%n" +
                        "Total actifs: %d%n" +
                        "Disponibles: %d%n" +
                        "En mission: %d%n" +
                        "En panne: %d%n" +
                        "En maintenance: %d%n" +
                        "Essaims actifs: %d",
                tousLesActifs.size(), disponibles, enMission, enPanne, enMaintenance, essaims.size());
    }

    public List<ActifMobile> getTousLesActifs() {
        return new ArrayList<>(tousLesActifs);
    }

    public List<Essaim> getEssaims() {
        return new ArrayList<>(essaims);
    }

    /**
     * Suggère ou crée un essaim optimal pour un "type" de mission simplifié.
     * 
     * @param type le type de mission (ex: "LOGISTIQUE", "SURVEILLANCE")
     * @return un essaim optimisé pour la mission, ou null si ressources
     *         insuffisantes
     */
    public Essaim suggererEssaim(String type) {
        List<ActifMobile> disponibles = getActifsDisponibles();
        if (disponibles.isEmpty())
            return null;

        Essaim essaimSuggere = new Essaim("Essaim-" + type);

        if ("LOGISTIQUE".equalsIgnoreCase(type)) {
            disponibles.stream()
                    .filter(a -> a instanceof fr.spiga.fleet.DroneLogistique)
                    .sorted(Comparator.comparingDouble(ActifMobile::getAutonomieRestante).reversed())
                    .limit(1)
                    .forEach(essaimSuggere::ajouterActif);
        } else if ("SURVEILLANCE".equalsIgnoreCase(type)) {
            disponibles.stream()
                    .filter(a -> a instanceof fr.spiga.fleet.DroneReconnaissance
                            || a instanceof fr.spiga.core.ActifMarin)
                    .sorted(Comparator.comparingDouble(ActifMobile::getAutonomieRestante).reversed())
                    .limit(2)
                    .forEach(essaimSuggere::ajouterActif);
        } else {
            // Par défaut
            ActifMobile best = selectionnerActifOptimalParAutonomie();
            if (best != null) {
                essaimSuggere.ajouterActif(best);
            }
        }

        return essaimSuggere.getNombreActifs() > 0 ? essaimSuggere : null;
    }
}
