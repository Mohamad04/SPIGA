package fr.spiga.gui;

import fr.spiga.core.*;
import fr.spiga.environment.ZoneOperation;
import fr.spiga.fleet.*;
import fr.spiga.mission.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Application JavaFX principale pour SPIGA.
 * Fournit une interface graphique pour la visualisation et la gestion de la
 * flotte.
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class SPIGAApplication extends Application {

    private GestionnaireEssaim gestionnaire;
    private List<Mission> missions;
    private ZoneOperation zoneOperation;
    private Canvas canvas;
    private TextArea logArea;
    private TextArea statsArea;
    private Label envStatusLabel;
    private ListView<String> assetListView;

    private static final double CANVAS_WIDTH = 800;
    private static final double CANVAS_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SPIGA - Simulateur de Planification et de Gestion d'Actifs Mobiles");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Centre: Canvas de visualisation
        VBox centreBox = new VBox(10);
        Label canvasLabel = new Label("Visualisation 2D de la Zone d'Opération");
        canvasLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
        centreBox.getChildren().addAll(canvasLabel, canvas);
        root.setCenter(centreBox);

        // Bas: Log (créer EN PREMIER pour permettre le logging pendant
        // l'initialisation)
        VBox bottomPanel = creerPanneauLog();
        root.setBottom(bottomPanel);

        // Initialiser le système (maintenant logArea existe)
        initialiser();

        // Gauche: Contrôles (maintenant gestionnaire existe)
        VBox leftPanel = creerPanneauControles();
        root.setLeft(leftPanel);

        // Droite: Liste des actifs et Conditions
        VBox rightPanel = new VBox(10);
        rightPanel.getChildren().addAll(creerPanneauEnvironnement(), creerPanneauActifs());
        root.setRight(rightPanel);

        dessinerVisualization();

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        // Démarrer la boucle de simulation (Game Loop)
        new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                // Temps écoulé en secondes (convertir nanosecondes en secondes)
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                // Mettre à jour toutes les missions actives
                // Accélérer massivement le temps simulé (x200) pour une réactivité optimale
                double simulationDt = dt * 200.0;

                // Mettre à jour l'affichage de l'environnement
                mettreAJourAffichageEnvironnement();

                for (Mission mission : missions) {
                    if ("EN_COURS".equals(mission.getStatut())) {
                        mission.mettreAJour(simulationDt);
                    }
                }

                // Vérifier les collisions entre actifs
                List<String> collisions = gestionnaire.verifierToutesLesCollisions();
                for (String c : collisions) {
                    if (c.contains("!!!")) {
                        ajouterLog(c);
                    }
                }

                // Rafraîchir l'affichage à chaque frame
                mettreAJourAffichage();
            }
        }.start();

        primaryStage.show();
    }

    /**
     * Initialise le système.
     */
    private void initialiser() {
        gestionnaire = new GestionnaireEssaim();
        missions = new ArrayList<>();

        // Créer la zone d'opération
        Position3D min = new Position3D(0, 0, -2000);
        Position3D max = new Position3D(100000, 100000, 10000);
        zoneOperation = new ZoneOperation(min, max);

        // Ajouter quelques conditions environnementales par défaut
        zoneOperation.setVent(new fr.spiga.environment.Vent(Math.PI / 4, 30.0));
        zoneOperation.setCourantMarin(
                new fr.spiga.environment.CourantMarin(new Position3D(1, -0.5, 0), 20.0));
        zoneOperation.setPrecipitation(new fr.spiga.environment.Precipitation(
                fr.spiga.environment.Precipitation.TypePrecipitation.PLUIE_MODEREE, 60.0));

        // --- AJOUT D'OBSTACLES POUR LES COLLISIONS ---
        creerObstaclesDemo();

        // Créer quelques actifs de démonstration
        creerActifsDemo();
    }

    /**
     * Crée des obstacles de démonstration.
     */
    private void creerObstaclesDemo() {
        // Île centrale (Maintenant plus grande et franchissable par les airs)
        // Rayon 5000m, Hauteur max 200m (Les drones volent à 300m+)
        zoneOperation.ajouterObstacle(new fr.spiga.environment.Obstacle(
                new Position3D(50000, 50000, 0), 5000, -2000, 200, "Ile Centrale"));

        // Récif dangereux (Uniquement sous-marin/surface)
        zoneOperation.ajouterObstacle(new fr.spiga.environment.Obstacle(
                new Position3D(20000, 80000, -50), 2000, -200, 0, "Récif Nord"));

        // Montagne (Obstacle total, très haut)
        zoneOperation.ajouterObstacle(new fr.spiga.environment.Obstacle(
                new Position3D(80000, 20000, 0), 3000, -2000, 2000, "Montagne Est"));

        // Une zone d'exclusion (Port)
        zoneOperation.ajouterZoneExclusion(new fr.spiga.environment.ZoneExclusion(
                new Position3D(10000, 10000, 0), 2500, "Port de Base"));
    }

    /**
     * Crée des actifs de démonstration.
     */

    private void mettreAJourAffichageEnvironnement() {
        if (envStatusLabel == null || zoneOperation == null)
            return;

        fr.spiga.environment.Vent v = zoneOperation.getVent();
        fr.spiga.environment.Precipitation p = zoneOperation.getPrecipitation();
        fr.spiga.environment.CourantMarin c = zoneOperation.getCourantMarin();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("💨 VENT: %.1f%% (Dir: %.0f°)\n",
                v.getIntensite(), Math.toDegrees(Math.atan2(v.getDirection().getY(), v.getDirection().getX()))));

        String pIcon = p.getType() == fr.spiga.environment.Precipitation.TypePrecipitation.AUCUNE ? "Soleil" : "Pluie";
        sb.append(String.format("%s PREC: %s (%.1f%%)\n",
                pIcon, p.getType().name(), p.getIntensite()));

        sb.append(String.format("🌊 COUR: %.1f%% (X:%.1f Y:%.1f)",
                c.getIntensite(), c.getDirection().getX(), c.getDirection().getY()));

        envStatusLabel.setText(sb.toString());
    }

    /**
     * Cria o painel de condições ambientais.
     */
    private VBox creerPanneauEnvironnement() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: lightblue; -fx-border-width: 2px; -fx-background-color: #f0f8ff;");
        // panel.setPrefWidth(300); // Déjà géré par le parent

        Label title = new Label("Conditions Environnementales");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        envStatusLabel = new Label("Initialisation...");
        envStatusLabel.setWrapText(true);
        envStatusLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        panel.getChildren().addAll(title, envStatusLabel);
        return panel;
    }

    private void creerActifsDemo() {
        DroneReconnaissance drone1 = new DroneReconnaissance(trouverPositionValide(500));
        DroneLogistique drone2 = new DroneLogistique(trouverPositionValide(300));
        VehiculeSurface usv = new VehiculeSurface(trouverPositionValide(0));
        VehiculeSousMarin auv = new VehiculeSousMarin(trouverPositionValide(-100));

        drone1.setZoneOperation(zoneOperation);
        drone2.setZoneOperation(zoneOperation);
        usv.setZoneOperation(zoneOperation);
        auv.setZoneOperation(zoneOperation);

        gestionnaire.enregistrerActif(drone1);
        gestionnaire.enregistrerActif(drone2);
        gestionnaire.enregistrerActif(usv);
        gestionnaire.enregistrerActif(auv);

        ajouterLog("4 actifs de démonstration créés (positions validées)");
    }

    /**
     * Crée le panneau de contrôles.
     */
    private VBox creerPanneauControles() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: gray; -fx-border-width: 1px;");
        panel.setPrefWidth(250);

        Label title = new Label("Contrôles");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button btnCreerDroneReco = new Button("Créer Drone Reco");
        btnCreerDroneReco.setMaxWidth(Double.MAX_VALUE);
        btnCreerDroneReco.setOnAction(e -> creerActif("DroneReconnaissance"));

        Button btnCreerDroneLog = new Button("Créer Drone Logistique");
        btnCreerDroneLog.setMaxWidth(Double.MAX_VALUE);
        btnCreerDroneLog.setOnAction(e -> creerActif("DroneLogistique"));

        Button btnCreerUSV = new Button("Créer Véhicule Surface");
        btnCreerUSV.setMaxWidth(Double.MAX_VALUE);
        btnCreerUSV.setOnAction(e -> creerActif("VehiculeSurface"));

        Button btnCreerAUV = new Button("Créer Sous-Marin");
        btnCreerAUV.setMaxWidth(Double.MAX_VALUE);
        btnCreerAUV.setOnAction(e -> creerActif("VehiculeSousMarin"));

        Button btnCreerMission = new Button("Nouvelle Mission (Config)");
        btnCreerMission.setMaxWidth(Double.MAX_VALUE);
        btnCreerMission.setStyle("-fx-font-weight: bold;");
        btnCreerMission.setOnAction(e -> gererCreationMissionAvancee());

        Button btnHistorique = new Button("Voir Historique");
        btnHistorique.setMaxWidth(Double.MAX_VALUE);
        btnHistorique.setOnAction(e -> voirHistorique());

        Button btnRecharger = new Button("Recharger Actifs");
        btnRecharger.setMaxWidth(Double.MAX_VALUE);
        btnRecharger.setOnAction(e -> {
            gestionnaire.rechargerTousLesActifsAuSol();
            ajouterLog("Actifs rechargés");
            mettreAJourAffichage();
        });

        Button btnResetMissions = new Button("Arrêter Toutes Missions");
        btnResetMissions.setMaxWidth(Double.MAX_VALUE);
        btnResetMissions.setStyle("-fx-text-fill: white; -fx-background-color: darkred;");
        btnResetMissions.setOnAction(e -> {
            for (Mission m : missions) {
                if ("EN_COURS".equals(m.getStatut()))
                    m.annuler("Réinitialisation forcée");
            }
            ajouterLog("Toutes les missions ont été arrêtées.");
            mettreAJourAffichage();
        });

        Separator sep = new Separator();

        Label statsLabel = new Label("Statistiques");
        statsLabel.setStyle("-fx-font-weight: bold;");

        statsArea = new TextArea();
        statsArea.setEditable(false);
        statsArea.setPrefHeight(150);
        statsArea.setText(gestionnaire.genererRapportFlotte());

        panel.getChildren().addAll(
                title,
                btnCreerDroneReco,
                btnCreerDroneLog,
                btnCreerUSV,
                btnCreerAUV,
                btnCreerMission,
                btnHistorique,
                btnRecharger,
                btnResetMissions,
                sep,
                statsLabel,
                statsArea);

        return panel;
    }

    /**
     * Crée le panneau des actifs.
     */
    private VBox creerPanneauActifs() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: gray; -fx-border-width: 1px;");
        panel.setPrefWidth(300);

        Label title = new Label("Liste des Actifs");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        assetListView = new ListView<>();
        assetListView.setPrefHeight(500);
        mettreAJourListeActifs();

        panel.getChildren().addAll(title, assetListView);

        return panel;
    }

    /**
     * Crée le panneau de log.
     */
    private VBox creerPanneauLog() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));

        Label title = new Label("Journal d'Activité");
        title.setStyle("-fx-font-weight: bold;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(100);

        panel.getChildren().addAll(title, logArea);

        return panel;
    }

    /**
     * Dessine la visualisation 2D.
     */
    private void dessinerVisualization() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Fond
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Grille
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        for (int i = 0; i < CANVAS_WIDTH; i += 50) {
            gc.strokeLine(i, 0, i, CANVAS_HEIGHT);
        }
        for (int i = 0; i < CANVAS_HEIGHT; i += 50) {
            gc.strokeLine(0, i, CANVAS_WIDTH, i);
        }

        // --- EFFETS ENVIRONNEMENTAUX ---
        if (zoneOperation != null) {
            dessinerVent(gc);
            dessinerCourants(gc);
            dessinerPluie(gc);
            dessinerObstacles(gc);
        }

        // Dessiner les actifs
        for (var actif : gestionnaire.getTousLesActifs()) {
            dessinerActif(gc, actif);
        }

        // Légende
        dessinerLegende(gc);
    }

    private void dessinerPluie(GraphicsContext gc) {
        fr.spiga.environment.Precipitation p = zoneOperation.getPrecipitation();
        if (p == null || p.getType() == fr.spiga.environment.Precipitation.TypePrecipitation.AUCUNE)
            return;

        Position3D min = zoneOperation.getRainZoneMin();
        Position3D max = zoneOperation.getRainZoneMax();

        double x1 = (min.getX() / 100000.0) * CANVAS_WIDTH;
        double y1 = (min.getY() / 100000.0) * CANVAS_HEIGHT;
        double x2 = (max.getX() / 100000.0) * CANVAS_WIDTH;
        double y2 = (max.getY() / 100000.0) * CANVAS_HEIGHT;

        gc.setStroke(Color.rgb(100, 100, 255, 0.4));
        gc.setLineWidth(1.0);

        // Dessiner quelques gouttes aléatoires dans la zone
        for (int i = 0; i < p.getIntensite() * 2; i++) {
            double rx = x1 + Math.random() * (x2 - x1);
            double ry = y1 + Math.random() * (y2 - y1);
            gc.strokeLine(rx, ry, rx - 2, ry + 5);
        }

        // Contour de la zone de pluie
        gc.setStroke(Color.rgb(150, 150, 150, 0.2));
        gc.strokeRect(x1, y1, x2 - x1, y2 - y1);
    }

    private void dessinerVent(GraphicsContext gc) {
        fr.spiga.environment.Vent v = zoneOperation.getVent();
        if (v == null || v.getIntensite() < 5)
            return;

        gc.setStroke(Color.rgb(200, 200, 200, 0.3));
        gc.setLineWidth(0.8);

        double angle = Math.atan2(v.getDirection().getY(), v.getDirection().getX());
        double dx = Math.cos(angle) * 20;
        double dy = Math.sin(angle) * 20;

        // Dessiner quelques flèches de vent éparpillées
        for (int i = 100; i < CANVAS_WIDTH; i += 200) {
            for (int j = 100; j < CANVAS_HEIGHT; j += 200) {
                gc.strokeLine(i, j, i + dx, j + dy);
            }
        }
    }

    private void dessinerCourants(GraphicsContext gc) {
        fr.spiga.environment.CourantMarin c = zoneOperation.getCourantMarin();
        if (c == null || c.getIntensite() < 5)
            return;

        gc.setStroke(Color.rgb(0, 100, 255, 0.2));
        gc.setLineWidth(1.5);

        double dx = c.getDirection().getX() * 15;
        double dy = c.getDirection().getY() * 15;

        // Ondulations avec direction
        for (int i = 50; i < CANVAS_WIDTH; i += 150) {
            for (int j = 50; j < CANVAS_HEIGHT; j += 150) {
                gc.strokeArc(i, j, 30, 10, 0, 180, javafx.scene.shape.ArcType.OPEN);
                gc.strokeLine(i + 15, j + 5, i + 15 + dx, j + 5 + dy);
            }
        }
    }

    private void dessinerObstacles(GraphicsContext gc) {
        // Dessiner les obstacles fixes
        for (fr.spiga.environment.Obstacle o : zoneOperation.getObstacles()) {
            double cx = (o.getPosition().getX() / 100000.0) * CANVAS_WIDTH;
            double cy = (o.getPosition().getY() / 100000.0) * CANVAS_HEIGHT;
            double cr = (o.getRayon() / 100000.0) * CANVAS_WIDTH;

            gc.setFill(Color.BROWN);
            gc.fillOval(cx - cr, cy - cr, cr * 2, cr * 2);
            gc.setStroke(Color.DARKRED);
            gc.strokeOval(cx - cr, cy - cr, cr * 2, cr * 2);
            gc.setFill(Color.BLACK);
            gc.fillText(o.getType(), cx - 20, cy);
        }

        // Dessiner les zones d'exclusion
        for (fr.spiga.environment.ZoneExclusion z : zoneOperation.getZonesExclusion()) {
            double cx = (z.getCentre().getX() / 100000.0) * CANVAS_WIDTH;
            double cy = (z.getCentre().getY() / 100000.0) * CANVAS_HEIGHT;
            double cr = (z.getRayon() / 100000.0) * CANVAS_WIDTH;

            gc.setStroke(Color.RED);
            gc.setLineWidth(2.0);
            gc.setLineDashes(10.0);
            gc.strokeOval(cx - cr, cy - cr, cr * 2, cr * 2);
            gc.setLineDashes(0);
            gc.setFill(Color.rgb(255, 0, 0, 0.1));
            gc.fillOval(cx - cr, cy - cr, cr * 2, cr * 2);
            gc.setFill(Color.RED);
            gc.fillText("INTERDIT: " + z.getNom(), cx - 30, cy);
        }
        gc.setLineWidth(1.0);
    }

    /**
     * Dessine un actif sur le canvas.
     */
    private void dessinerActif(GraphicsContext gc, fr.spiga.core.ActifMobile actif) {
        Position3D pos = actif.getPosition();

        // Convertir les coordonnées (échelle)
        double x = (pos.getX() / 100000.0) * CANVAS_WIDTH;
        double y = (pos.getY() / 100000.0) * CANVAS_HEIGHT;

        // Couleur selon le type et l'état
        Color couleur = obtenirCouleurActif(actif);
        gc.setFill(couleur);

        // Calculer l'échelle selon l'altitude/profondeur
        double scale = 1.0;
        if (actif instanceof fr.spiga.core.ActifAerien) {
            double altitude = ((fr.spiga.core.ActifAerien) actif).getAltitude();
            double altMax = ((fr.spiga.core.ActifAerien) actif).getAltitudeMax();
            // Plus haut = plus grand (jusqu'à +50%)
            if (altMax > 0)
                scale = 1.0 + (altitude / altMax) * 0.5;
        } else if (actif instanceof fr.spiga.core.ActifMarin) {
            double profondeur = ((fr.spiga.core.ActifMarin) actif).getProfondeur();
            double profMax = ((fr.spiga.core.ActifMarin) actif).getProfondeurMax();
            // Plus profond = plus petit (jusqu'à -50% pour meilleure visibilité)
            if (profMax > 0)
                scale = 1.0 - (profondeur / profMax) * 0.5;
        }

        double size = 16 * scale;
        double half = size / 2.0;

        // Forme selon le type
        if (actif instanceof fr.spiga.fleet.DroneReconnaissance) {
            // Triangle
            double[] xPoints = { x, x - half, x + half };
            double[] yPoints = { y - ((size * 0.6) + 2), y + ((size * 0.3) + 2), y + ((size * 0.3) + 2) };
            gc.fillPolygon(xPoints, yPoints, 3);

            // Icone R
            gc.setFill(Color.WHITE);
            gc.fillText("R", x - 3, y + 3);
        } else if (actif instanceof fr.spiga.fleet.DroneLogistique) {
            // Carré
            gc.fillRect(x - half, y - half, size, size);

            // Icone L
            gc.setFill(Color.WHITE);
            gc.fillText("L", x - 3, y + 3);
        } else if (actif instanceof fr.spiga.fleet.VehiculeSurface) {
            // Cercle
            gc.fillOval(x - half, y - half, size, size);
        } else if (actif instanceof fr.spiga.fleet.VehiculeSousMarin) {
            // Losange
            double[] xPoints = { x, x - half, x, x + half };
            double[] yPoints = { y - half, y, y + half, y };
            gc.fillPolygon(xPoints, yPoints, 4);
        } else {
            // Défaut
            gc.fillOval(x - (half / 2), y - (half / 2), half, half);
        }

        // Indicateur d'autonomie
        double autonomie = actif.getAutonomieRestante();
        double yBar = y + half + 2;

        gc.setFill(autonomie > 50 ? Color.GREEN : autonomie > 20 ? Color.ORANGE : Color.RED);
        gc.fillRect(x - half, yBar, size * (autonomie / 100.0), 3);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x - half, yBar, size, 3);
    }

    /**
     * Obtient la couleur d'un actif selon son type et état.
     */
    private Color obtenirCouleurActif(fr.spiga.core.ActifMobile actif) {
        switch (actif.getEtatOperationnel()) {
            case EN_MISSION -> {
                return Color.BLUE;
            }
            case EN_PANNE -> {
                return Color.RED;
            }
            case EN_MAINTENANCE -> {
                return Color.ORANGE;
            }
            default -> {
                return Color.GREEN;
            }
        }
    }

    /**
     * Dessine la légende.
     */
    private void dessinerLegende(GraphicsContext gc) {
        // Dessiner la légende en bas à droite
        double rectX = CANVAS_WIDTH - 200;
        double rectY = CANVAS_HEIGHT - 120;
        gc.setFill(Color.rgb(255, 255, 255, 0.8)); // Fond semi-transparent
        gc.fillRect(rectX, rectY, 190, 110);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(rectX, rectY, 190, 110);

        gc.setFill(Color.BLACK);
        gc.fillText("Légende:", rectX + 10, rectY + 20);

        gc.setFill(Color.GREEN);
        gc.fillRect(rectX + 10, rectY + 30, 10, 10);
        gc.setFill(Color.BLACK);
        gc.fillText("AU_SOL", rectX + 25, rectY + 40);

        gc.setFill(Color.BLUE);
        gc.fillRect(rectX + 10, rectY + 45, 10, 10);
        gc.setFill(Color.BLACK);
        gc.fillText("EN_MISSION", rectX + 25, rectY + 55);

        gc.setFill(Color.RED);
        gc.fillRect(rectX + 10, rectY + 60, 10, 10);
        gc.setFill(Color.BLACK);
        gc.fillText("EN_PANNE", rectX + 25, rectY + 70);
        gc.fillText("(R)eco  (S)urf  (I)nsp  (+)Sauv", rectX + 10, rectY + 95);
    }

    /**
     * Crée un nouvel actif.
     */
    private void creerActif(String type) {
        double z = type.contains("Drone") ? 500 : (type.contains("SousMarin") ? -50 : 0);
        Position3D pos = trouverPositionValide(z);

        fr.spiga.core.ActifMobile actif = switch (type) {
            case "DroneReconnaissance" -> new DroneReconnaissance(pos);
            case "DroneLogistique" -> new fr.spiga.fleet.DroneLogistique(pos);
            case "VehiculeSurface" -> new VehiculeSurface(pos);
            case "VehiculeSousMarin" -> new fr.spiga.fleet.VehiculeSousMarin(pos);
            default -> null;
        };

        if (actif != null) {
            actif.setZoneOperation(zoneOperation);
            gestionnaire.enregistrerActif(actif);
            ajouterLog("Actif créé: " + actif.getType() + " em " + pos);
            mettreAJourAffichage();
        }
    }

    private Position3D trouverPositionValide(double z) {
        int tentatives = 0;
        while (tentatives < 100) {
            double x = 5000 + Math.random() * 90000;
            double y = 5000 + Math.random() * 90000;
            Position3D pos = new Position3D(x, y, z);

            if (zoneOperation == null)
                return pos;

            if (!zoneOperation.estEnCollisionAvecObstacle(pos) &&
                    !zoneOperation.estDansZoneExclusion(pos)) {
                return pos;
            }
            tentatives++;
        }
        return new Position3D(5000, 5000, z); // Par défaut si coincé
    }

    // Méthode simulerPanneAleatoire supprimée

    /**
     * Gère la création avancée de mission avec sélection manuelle.
     */
    private void gererCreationMissionAvancee() {
        // 1. Choisir le type
        List<String> types = List.of("Recherche et Sauvetage", "Surveillance Maritime",
                "Inspection Sous-Marine",
                "Reconnaissance Aérienne");
        ChoiceDialog<String> dialogType = new ChoiceDialog<>(types.get(0),
                types);
        dialogType.setTitle("Nouvelle Mission");
        dialogType.setHeaderText("Étape 1/3 : Type de Mission");
        dialogType.setContentText("Sélectionnez le type :");

        Optional<String> typeOpt = dialogType.showAndWait();
        if (typeOpt.isEmpty())
            return;
        String typeChoisi = typeOpt.get();

        // 2. Configurer les paramètres spécifiques
        fr.spiga.mission.Mission mission = null;
        LocalDateTime now = LocalDateTime.now();

        // Demander la durée
        List<Integer> durees = List.of(1, 2, 4, 8);
        ChoiceDialog<Integer> dialogDuree = new ChoiceDialog<>(2, durees);
        dialogDuree.setTitle("Durée");
        dialogDuree.setHeaderText("Combien d'heures doit durer la mission ?");
        Optional<Integer> dureeOpt = dialogDuree.showAndWait();
        if (dureeOpt.isEmpty())
            return;

        LocalDateTime fin = now.plusHours(dureeOpt.get());

        if (typeChoisi.equals("Recherche et Sauvetage")) {
            // Logique Sauvetage (Inchangée pour la sélection cible)
            List<fr.spiga.core.ActifMobile> ciblesPotentielles = gestionnaire.getTousLesActifs().stream()
                    .filter(a -> a.getEtatOperationnel() == fr.spiga.core.EtatOperationnel.EN_PANNE
                            || a.getAutonomieRestante() < 90.0)
                    .collect(java.util.stream.Collectors.toList());

            if (ciblesPotentielles.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Impossible");
                alert.setHeaderText("Aucune cible valide");
                alert.setContentText("Aucun véhicule n'est en panne ou en besoin de ravitaillement (<90%).");
                alert.showAndWait();
                return;
            }

            ChoiceDialog<fr.spiga.core.ActifMobile> dialogCible = new ChoiceDialog<>(ciblesPotentielles.get(0),
                    ciblesPotentielles);
            dialogCible.setTitle("Configuration Sauvetage");
            dialogCible.setHeaderText("Étape 2/3 : Cible à secourir");
            dialogCible.setContentText("Choisissez le véhicule en détresse :");

            Optional<fr.spiga.core.ActifMobile> cibleOpt = dialogCible.showAndWait();
            if (cibleOpt.isEmpty())
                return;

            mission = new fr.spiga.mission.MissionRechercheEtSauvetage(now, fin, cibleOpt.get());
        } else {
            // Pour les autres missions : Point-to-Point avec Coordonnées

            // Dialog custom pour X, Y (et Z si inspection)
            Dialog<Position3D> dialogCoords = new Dialog<>();
            dialogCoords.setTitle("Coordonnées de destination");
            dialogCoords.setHeaderText(
                    "Entrez le point objectif (X, Y" + (typeChoisi.equals("Inspection Sous-Marine") ? ", Z)" : ")"));
            dialogCoords.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            TextField xField = new TextField("50000");
            TextField yField = new TextField("50000");
            TextField zField = new TextField(typeChoisi.equals("Reconnaissance Aérienne") ? "2000"
                    : (typeChoisi.equals("Inspection Sous-Marine") ? "-100" : "0"));

            grid.add(new Label("X:"), 0, 0);
            grid.add(xField, 1, 0);
            grid.add(new Label("Y:"), 0, 1);
            grid.add(yField, 1, 1);

            // Only show Z for missions that care, or default 0/safe
            if (typeChoisi.equals("Inspection Sous-Marine")) {
                grid.add(new Label("Z (Profondeur < 0):"), 0, 2);
                grid.add(zField, 1, 2);
            }

            dialogCoords.getDialogPane().setContent(grid);
            dialogCoords.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    try {
                        double x = Double.parseDouble(xField.getText());
                        double y = Double.parseDouble(yField.getText());
                        double z = Double.parseDouble(zField.getText());
                        return new Position3D(x, y, z);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                return null;
            });

            Optional<Position3D> posOpt = dialogCoords.showAndWait();
            if (posOpt.isEmpty())
                return;
            Position3D dest = posOpt.get();

            if (typeChoisi.equals("Surveillance Maritime")) {
                mission = new fr.spiga.mission.MissionSurveillanceMaritime(now, fin, dest);
            } else if (typeChoisi.equals("Reconnaissance Aérienne")) {
                mission = new fr.spiga.mission.MissionReconnaissanceAerienne(now, fin, dest);
            } else { // Inspection Sous-Marine
                mission = new fr.spiga.mission.MissionInspectionSousMarine(now, fin, dest);
            }
        }

        // 3. Sélectionner les véhicules exécutants (1 à 5)
        final fr.spiga.mission.Mission missionFinale = mission;
        List<fr.spiga.core.ActifMobile> candidats = gestionnaire.getActifsDisponibles().stream()
                .filter(missionFinale::estCompatible)
                .collect(java.util.stream.Collectors.toList());

        if (candidats.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Aucun véhicule compatible disponible");
            alert.setContentText("Il n'y a aucun véhicule disponible capable de réaliser cette mission.");
            alert.showAndWait();
            return;
        }

        // Custom Dialog for Multi-Selection
        Dialog<List<fr.spiga.core.ActifMobile>> dialogSelect = new Dialog<>();
        dialogSelect.setTitle("Affectation");
        dialogSelect.setHeaderText("Étape 3/3 : Sélectionner 1 à 5 véhicules");
        dialogSelect.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ListView<fr.spiga.core.ActifMobile> listView = new ListView<>();
        listView.getItems().addAll(candidats);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dialogSelect.getDialogPane().setContent(listView);

        dialogSelect.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new ArrayList<>(listView.getSelectionModel().getSelectedItems());
            }
            return null;
        });

        Optional<List<fr.spiga.core.ActifMobile>> selectionOpt = dialogSelect.showAndWait();
        if (selectionOpt.isPresent() && !selectionOpt.get().isEmpty()) {
            List<fr.spiga.core.ActifMobile> selected = selectionOpt.get();
            if (selected.size() > 5) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Attention");
                alert.setContentText("Maximum 5 véhicules. Seuls les 5 premiers seront utilisés.");
                alert.showAndWait();
                selected = selected.subList(0, 5);
            }

            for (fr.spiga.core.ActifMobile executant : selected) {
                mission.assignerActif(executant);
            }

            mission.demarrer();
            missions.add(mission);
            ajouterLog("Mission lancée : " + mission.getNom() + " avec " + selected.size() + " actifs.");
            mettreAJourAffichage();
        }
    }

    /**
     * Affiche l'historique des missions.
     */
    private void voirHistorique() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Historique des Missions");
        alert.setHeaderText("Rapport complet");

        StringBuilder sb = new StringBuilder("--- HISTORIQUE DES MISSIONS ---\n\n");
        for (Mission m : missions) {
            sb.append(m.toString()).append("\n");
        }

        TextArea area = new TextArea(sb.toString());
        area.setEditable(false);
        area.setWrapText(true);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    /**
     * Crée une mission (Obsolète, utiliser gererCreationMission).
     */
    // Méthode supprimée car inutilisée et redondante

    /**
     * Met à jour l'affichage.
     */
    private void mettreAJourAffichage() {
        dessinerVisualization();
        mettreAJourListeActifs();
        if (statsArea != null) {
            statsArea.setText(gestionnaire.genererRapportFlotte());
        }
    }

    /**
     * Met à jour la liste des actifs.
     */
    private void mettreAJourListeActifs() {
        assetListView.getItems().clear();
        for (var actif : gestionnaire.getTousLesActifs()) {
            assetListView.getItems().add(actif.toString());
        }
    }

    /**
     * Ajoute un message au log.
     */
    private void ajouterLog(String message) {
        logArea.appendText("[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + message + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
