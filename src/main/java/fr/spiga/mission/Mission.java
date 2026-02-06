package fr.spiga.mission;

import fr.spiga.core.ActifMobile;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Classe abstraite représentant une mission.
 * Définit la structure commune pour toutes les missions.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public abstract class Mission implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Identifiant unique de la mission */
    private final String id;

    /** Type de mission (ex: "LOGISTIQUE", "SURVEILLANCE") */
    private final String type;

    /** Statut de la mission ("PLANIFIEE", "EN_COURS", "TERMINEE", "ANNULEE") */
    protected String statut;
    protected String description;
    protected double progres; // 0.0 to 1.0
    protected boolean objectifAtteint;

    /** Date/heure de début prévue */
    private LocalDateTime debutPrevu;

    /** Date/heure de fin prévue */
    private LocalDateTime finPrevue;

    /** Date/heure de début réel */
    private LocalDateTime debutReel;

    /** Date/heure de fin réelle */
    private LocalDateTime finReelle;

    /** Actifs assignés à la mission */
    private final List<ActifMobile> actifsAssignes;

    /** Résultats attendus */
    private String resultatsAttendus;

    /** Résultats obtenus */
    private String resultatsObtenus;

    /**
     * Constructeur protégé pour les sous-classes.
     * 
     * @param type              le type de mission
     * @param debutPrevu        la date/heure de début prévue
     * @param finPrevue         la date/heure de fin prévue
     * @param resultatsAttendus les résultats attendus
     */
    protected Mission(String type, LocalDateTime debutPrevu, LocalDateTime finPrevue,
            String resultatsAttendus) {
        if (type == null) {
            throw new IllegalArgumentException("Le type de mission ne peut pas être nul");
        }
        if (debutPrevu == null || finPrevue == null) {
            throw new IllegalArgumentException("Les dates ne peuvent pas être nulles");
        }
        if (debutPrevu.isAfter(finPrevue)) {
            throw new IllegalArgumentException("La date de début doit être avant la date de fin");
        }

        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.statut = "PLANIFIEE";
        this.debutPrevu = debutPrevu;
        this.finPrevue = finPrevue;
        this.resultatsAttendus = resultatsAttendus;
        this.actifsAssignes = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getStatut() {
        return statut;
    }

    public LocalDateTime getDebutPrevu() {
        return debutPrevu;
    }

    public LocalDateTime getFinPrevue() {
        return finPrevue;
    }

    public LocalDateTime getDebutReel() {
        return debutReel;
    }

    public LocalDateTime getFinReelle() {
        return finReelle;
    }

    public List<ActifMobile> getActifsAssignes() {
        return new ArrayList<>(actifsAssignes);
    }

    public String getResultatsAttendus() {
        return resultatsAttendus;
    }

    public String getResultatsObtenus() {
        return resultatsObtenus;
    }

    /** Zone d'opération associée à la mission */
    protected fr.spiga.environment.ZoneOperation zoneOperation;

    /**
     * Vérifie si un actif est compatible avec cette mission.
     * 
     * @param actif l'actif à vérifier
     * @return true si compatible, false sinon
     */
    public abstract boolean estCompatible(ActifMobile actif);

    /**
     * Assigne un actif à la mission.
     * 
     * @param actif l'actif à assigner
     * @return true si assignation réussie, false sinon
     */
    public boolean assignerActif(ActifMobile actif) {
        if (actif == null || actifsAssignes.contains(actif)) {
            return false;
        }
        if (!"PLANIFIEE".equals(statut)) {
            return false; // Impossible d'assigner si mission déjà lancée
        }
        if (actif.getEtatOperationnel() != fr.spiga.core.EtatOperationnel.AU_SOL) {
            System.out.println("Actif " + actif.getId() + " indisponible (état: " + actif.getEtatOperationnel() + ")");
            return false;
        }
        if (!estCompatible(actif)) {
            System.out.println("Actif " + actif.getId() + " incompatible avec la mission " + getNom());
            return false;
        }
        return actifsAssignes.add(actif);
    }

    /**
     * Assigne tous les membres d'un essaim à la mission.
     * 
     * @param essaim l'essaim à assigner
     * @return true si au moins un actif a été assigné
     */
    public boolean assignerEssaim(fr.spiga.fleet.Essaim essaim) {
        if (essaim == null || !"PLANIFIEE".equals(statut)) {
            return false;
        }
        boolean assigned = false;
        for (ActifMobile actif : essaim.getActifs()) {
            if (assignerActif(actif)) {
                assigned = true;
            }
        }
        return assigned;
    }

    /**
     * Démarre la mission.
     * 
     * @return true si démarrage réussi, false sinon
     */
    public boolean demarrer() {
        if (!"PLANIFIEE".equals(statut)) {
            return false;
        }
        if (actifsAssignes.isEmpty()) {
            System.out.println("Impossible de démarrer: aucun actif assigné");
            return false;
        }

        this.statut = "EN_COURS";
        this.debutReel = LocalDateTime.now();

        // Démarrer tous les actifs assignés
        for (ActifMobile actif : actifsAssignes) {
            actif.demarrer();
        }

        return true;
    }

    /**
     * Termine la mission avec succès.
     * 
     * @param resultats les résultats obtenus
     * @return true si terminaison réussie, false sinon
     */
    public boolean terminer(String resultats) {
        if (!"EN_COURS".equals(statut)) {
            return false;
        }

        this.statut = "TERMINEE";
        this.finReelle = LocalDateTime.now();
        this.resultatsObtenus = resultats;

        // Réinitialiser l'état des actifs pour qu'ils soient de nouveau disponibles
        for (ActifMobile actif : actifsAssignes) {
            actif.eteindre();
            actif.setEtatOperationnel(fr.spiga.core.EtatOperationnel.AU_SOL);
        }

        return true;
    }

    public boolean annuler(String raison) {
        if ("TERMINEE".equals(statut) || "ANNULEE".equals(statut)) {
            return false;
        }

        this.statut = "ANNULEE";
        this.resultatsObtenus = "Mission annulée: " + raison;

        if (debutReel != null) {
            this.finReelle = LocalDateTime.now();
        }

        // Les actifs ne sont plus éteints automatiquement ici.
        // La logique de mission doit s'assurer qu'ils sont rentrés à la base.
        // Si besoin, on pourra ajouter une vérification ou un warning si on termine
        // alors qu'ils sont en vol.

        return true;
    }

    public fr.spiga.environment.ZoneOperation getZoneOperation() {
        return zoneOperation;
    }

    public void setZoneOperation(fr.spiga.environment.ZoneOperation zoneOperation) {
        this.zoneOperation = zoneOperation;
    }

    public void mettreAJour(double dt) {
        if (!"EN_COURS".equals(statut)) {
            return;
        }

        // Vérifier si des actifs assignés sont revenus à un état disponible (AU_SOL)
        // après une panne ou rechargement, et les remettre en mission.
        for (ActifMobile actif : actifsAssignes) {
            if (actif.getEtatOperationnel() == fr.spiga.core.EtatOperationnel.AU_SOL) {
                System.out.println("Actif " + actif.getId() + " de nouveau opérationnel. Reprise de mission.");
                actif.demarrer(); // Remet en état EN_MISSION
            }
        }

        mettreAJourSpecifique(dt);
    }

    /**
     * Met à jour la logique spécifique de la mission (implémenté par les
     * sous-classes).
     * 
     * @param dt le temps écoulé
     */
    protected abstract void mettreAJourSpecifique(double dt);

    /**
     * Obtient le nom descriptif de la mission.
     * 
     * @return le nom de la mission
     */
    public abstract String getNom();

    /**
     * Obtient l'objectif clair de la mission.
     * 
     * @return la description de l'objectif
     */
    public String getObjectif() {
        return resultatsAttendus;
    }

    @Override
    public String toString() {
        return String.format("%s [ID: %s] - %s (Statut: %s)",
                getNom(), id.substring(0, 8), getObjectif(), statut);
    }
}
