package fr.spiga.cli;

import fr.spiga.core.*;
import fr.spiga.environment.*;
import fr.spiga.fleet.*;
import fr.spiga.mission.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interface en ligne de commande (CLI) pour le système SPIGA.
 * Permet de gérer la flotte, les missions et les simulations via un menu
 * console.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class SPIGACli {

    private final Scanner scanner;
    private final GestionnaireEssaim gestionnaire;
    private final List<Mission> missions;
    private ZoneOperation zoneOperation;

    /**
     * Constructeur du CLI.
     */
    public SPIGACli() {
        this.scanner = new Scanner(System.in);
        this.gestionnaire = new GestionnaireEssaim();
        this.missions = new ArrayList<>();
        initialiserZoneOperation();
    }

    /**
     * Initialise la zone d'opération par défaut.
     */
    private void initialiserZoneOperation() {
        Position3D min = new Position3D(0, 0, -2000);
        Position3D max = new Position3D(100000, 100000, 10000);
        this.zoneOperation = new ZoneOperation(min, max);

        // Ajouter quelques conditions environnementales
        zoneOperation.setVent(new Vent(Math.PI / 4, 20.0)); // Vent modéré
        zoneOperation.setCourantMarin(new CourantMarin(new Position3D(1, 0.5, 0), 15.0));
    }

    /**
     * Démarre l'application CLI.
     */
    public void demarrer() {
        System.out.println("===============================================");
        System.out.println("   SPIGA - Simulateur de Planification");
        System.out.println("   et de Gestion d'Actifs Mobiles");
        System.out.println("===============================================\n");

        boolean continuer = true;
        while (continuer) {
            afficherMenuPrincipal();
            int choix = lireEntier("Votre choix: ");

            switch (choix) {
                case 1 -> menuCreationActifs();
                case 2 -> menuGestionMissions();
                case 3 -> menuSimulation();
                case 4 -> afficherEtatFlotte();
                case 5 -> menuConfiguration();
                case 0 -> {
                    System.out.println("\nAu revoir !");
                    continuer = false;
                }
                default -> System.out.println("Choix invalide !");
            }
        }

        scanner.close();
    }

    /**
     * Affiche le menu principal.
     */
    private void afficherMenuPrincipal() {
        System.out.println("\n=== MENU PRINCIPAL ===");
        System.out.println("1.  Créer des actifs");
        System.out.println("2. Gestion des missions");
        System.out.println("3. Simulation");
        System.out.println("4. Afficher l'état de la flotte");
        System.out.println("5. Configuration");
        System.out.println("0. Quitter");
        System.out.println();
    }

    /**
     * Menu de création d'actifs.
     */
    private void menuCreationActifs() {
        System.out.println("\n=== CRÉATION D'ACTIFS ===");
        System.out.println("1. Drone de Reconnaissance");
        System.out.println("2. Drone Logistique");
        System.out.println("4. Véhicule de Surface");
        System.out.println("5. Véhicule Sous-Marin");
        System.out.println("0. Retour");

        int choix = lireEntier("Type d'actif: ");

        if (choix == 0)
            return;

        double x = lireDouble("Position X (m): ");
        double y = lireDouble("Position Y (m): ");
        double z = lireDouble("Position Z (m, altitude ou profondeur): ");
        Position3D position = new Position3D(x, y, z);

        ActifMobile actif = null;

        switch (choix) {
            case 1 -> actif = new DroneReconnaissance(position);
            case 2 -> actif = new DroneLogistique(position);
            case 4 -> actif = new VehiculeSurface(position);
            case 5 -> actif = new VehiculeSousMarin(position);
            default -> {
                System.out.println("Type invalide !");
                return;
            }
        }

        actif.setZoneOperation(zoneOperation);
        gestionnaire.enregistrerActif(actif);
        System.out.println("✓ Actif créé: " + actif);
    }

    /**
     * Menu de gestion des missions.
     */
    private void menuGestionMissions() {
        System.out.println("\n=== GESTION DES MISSIONS ===");
        System.out.println("1. Créer une mission");
        System.out.println("2. Assigner des actifs à une mission");
        System.out.println("3. Démarrer une mission");
        System.out.println("4. Afficher l'historique");
        System.out.println("0. Retour");

        int choix = lireEntier("Votre choix: ");

        switch (choix) {
            case 1 -> creerMission();
            case 2 -> assignerActifsAMission();
            case 3 -> demarrerMission();
            case 4 -> {
                System.out.println("\n--- HISTORIQUE DES MISSIONS ---");
                for (Mission m : missions) {
                    System.out.println(m);
                }
            }
        }
    }

    /**
     * Crée une nouvelle mission.
     */
    private void creerMission() {
        System.out.println("\nType de mission:");
        System.out.println("1. Surveillance Maritime");
        System.out.println("2. Recherche et Sauvetage");

        int type = lireEntier("Type: ");

        LocalDateTime debut = LocalDateTime.now().plusHours(1);
        LocalDateTime fin = debut.plusHours(4);

        Mission mission = null;

        if (type == 1) {
            mission = new fr.spiga.mission.MissionSurveillanceMaritime(
                    debut, fin,
                    new fr.spiga.core.Position3D(50000, 50000, 0));
        } else if (type == 2) {
            System.out.println("Véhicules en détresse :");
            gestionnaire.getTousLesActifs().stream()
                    .filter(a -> a.getEtatOperationnel() == EtatOperationnel.EN_PANNE || a.getAutonomieRestante() < 25)
                    .forEach(a -> System.out.println(" - " + a.getId() + " (" + a.getType() + ")"));

            String idCible = lireTexte("ID du véhicule à secourir: ");
            ActifMobile cibleRescape = gestionnaire.getTousLesActifs().stream()
                    .filter(a -> a.getId().equalsIgnoreCase(idCible))
                    .findFirst().orElse(null);

            if (cibleRescape == null) {
                System.out.println("Véhicule introuvable !");
                return;
            }
            mission = new MissionRechercheEtSauvetage(debut, fin, cibleRescape);
        }

        if (mission != null) {
            missions.add(mission);
            System.out.println("✓ Mission créée: " + mission);
        }
    }

    /**
     * Assigne des actifs à une mission.
     */
    private void assignerActifsAMission() {
        if (missions.isEmpty()) {
            System.out.println("Aucune mission disponible !");
            return;
        }

        System.out.println("\nMissions disponibles:");
        for (int i = 0; i < missions.size(); i++) {
            System.out.println((i + 1) + ". " + missions.get(i));
        }

        int choix = lireEntier("Mission: ") - 1;
        if (choix < 0 || choix >= missions.size()) {
            System.out.println("Mission invalide !");
            return;
        }

        Mission mission = missions.get(choix);

        // Sélectionner un actif optimal
        ActifMobile actif = gestionnaire.selectionnerActifOptimalParAutonomie();

        if (actif == null) {
            System.out.println("Aucun actif disponible !");
            return;
        }

        mission.assignerActif(actif);
        System.out.println("✓ Actif " + actif.getId() + " assigné à " + mission.getNom());
    }

    /**
     * Démarre une mission.
     */
    private void demarrerMission() {
        List<Mission> planned = missions.stream()
                .filter(m -> "PLANIFIEE".equals(m.getStatut()))
                .toList();

        if (missions.isEmpty()) {
            System.out.println("Aucune mission planifiée !");
            return;
        }

        System.out.println("\nMissions planifiées:");
        for (int i = 0; i < planned.size(); i++) {
            System.out.println((i + 1) + ". " + planned.get(i));
        }

        int choix = lireEntier("Mission à démarrer: ") - 1;
        if (choix >= 0 && choix < planned.size()) {
            Mission mission = planned.get(choix);
            if (mission.demarrer()) {
                System.out.println("✓ Mission démarrée: " + mission.getNom());
            } else {
                System.out.println("✗ Échec du démarrage !");
            }
        }
    }

    /**
     * Menu de simulation.
     */
    private void menuSimulation() {
        System.out.println("\n=== SIMULATION ===");
        System.out.println("1. Déplacer un actif");
        System.out.println("2. Vérifier les collisions");
        System.out.println("3. Recharger tous les actifs au sol");
        System.out.println("0. Retour");

        int choix = lireEntier("Votre choix: ");

        switch (choix) {
            case 1 -> deplacerActif();
            case 2 -> verifierCollisions();
            case 3 -> {
                gestionnaire.rechargerTousLesActifsAuSol();
                System.out.println("✓ Tous les actifs au sol ont été rechargés");
            }
        }
    }

    /**
     * Déplace un actif.
     */
    private void deplacerActif() {
        List<ActifMobile> actifs = gestionnaire.getTousLesActifs();
        if (actifs.isEmpty()) {
            System.out.println("Aucun actif disponible !");
            return;
        }

        System.out.println("\nActifs:");
        for (int i = 0; i < actifs.size(); i++) {
            System.out.println((i + 1) + ". " + actifs.get(i));
        }

        int choix = lireEntier("Actif: ") - 1;
        if (choix < 0 || choix >= actifs.size())
            return;

        ActifMobile actif = actifs.get(choix);

        double x = lireDouble("Cible X: ");
        double y = lireDouble("Cible Y: ");
        double z = lireDouble("Cible Z: ");

        Position3D cible = new Position3D(x, y, z);

        if (actif.deplacer(cible)) {
            System.out.println("✓ Déplacement réussi");
            System.out.println("Nouvelle position: " + actif.getPosition());
            System.out.println("Autonomie restante: " + String.format("%.1f%%", actif.getAutonomieRestante()));
        } else {
            System.out.println("✗ Déplacement échoué !");
        }
    }

    /**
     * Vérifie les collisions.
     */
    private void verifierCollisions() {
        List<String> alertes = gestionnaire.verifierToutesLesCollisions();

        if (alertes.isEmpty()) {
            System.out.println("✓ Aucune collision détectée");
        } else {
            System.out.println("⚠ Alertes de collision:");
            alertes.forEach(System.out::println);
        }
    }

    /**
     * Affiche l'état de la flotte.
     */
    private void afficherEtatFlotte() {
        System.out.println("\n" + gestionnaire.genererRapportFlotte());

        System.out.println("\nActifs détaillés:");
        for (ActifMobile actif : gestionnaire.getTousLesActifs()) {
            System.out.println("  - " + actif);
        }
    }

    /**
     * Menu de configuration.
     */
    private void menuConfiguration() {
        System.out.println("\n=== CONFIGURATION ===");
        System.out.println("1. Modifier intensité du vent");
        System.out.println("0. Retour");

        int choix = lireEntier("Votre choix: ");

        switch (choix) {
            case 1 -> {
                double intensite = lireDouble("Nouvelle intensité vent (0-100): ");
                zoneOperation.setVent(new Vent(Math.PI / 4, intensite));
                System.out.println("✓ Vent modifié");
            }
        }
    }

    /**
     * Lit un entier depuis la console.
     */
    private int lireEntier(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            scanner.next();
            System.out.print(prompt);
        }
        int result = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return result;
    }

    /**
     * Lit un double depuis la console.
     */
    private double lireDouble(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            scanner.next();
            System.out.print(prompt);
        }
        double result = scanner.nextDouble();
        scanner.nextLine(); // consume newline
        return result;
    }

    /**
     * Lit du texte depuis la console.
     */
    private String lireTexte(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Point d'entrée principal pour le mode CLI.
     */
    public static void main(String[] args) {
        SPIGACli cli = new SPIGACli();
        cli.demarrer();
    }
}
