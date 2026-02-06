package fr.spiga.core;

import fr.spiga.interfaces.*;
import fr.spiga.environment.ZoneOperation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Classe abstraite représentant la base de tous les actifs mobiles.
 * Centralise les attributs et comportements fondamentaux communs à tous les
 * engins.
 * 
 * <p>
 * Cette classe implémente les principes SOLID :
 * <ul>
 * <li>SRP : Responsabilité unique de gestion d'actif mobile</li>
 * <li>OCP : Ouverte à l'extension via héritage, fermée à la modification</li>
 * <li>LSP : Les sous-classes peuvent remplacer ActifMobile</li>
 * <li>ISP : Les interfaces sont ségrégées par comportement</li>
 * </ul>
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public abstract class ActifMobile implements Deplacable, Rechargeable, Communicable,
        Pilotable, Alertable, Serializable {
    private static final long serialVersionUID = 1L;

    /** Seuil critique d'autonomie (en pourcentage) */
    protected static final double SEUIL_AUTONOMIE_CRITIQUE = 20.0;

    /** Identifiant unique de l'actif */
    private final String id;

    /** Position actuelle en 3D */
    private Position3D position;

    /** Vitesse maximale en m/s */
    private final double vitesseMax;

    /** Autonomie maximale en heures */
    private final double autonomieMax;

    /** Autonomie restante en pourcentage (0.0 à 100.0) */
    private double autonomieRestante;

    /** État opérationnel actuel */
    private EtatOperationnel etatOperationnel;

    /** Indique si l'actif est en marche */
    private boolean enMarche;

    /** Zone d'opération dans laquelle évolue l'actif */
    protected ZoneOperation zoneOperation;

    /** Vecteur de déplacement résiduel (dérive environnementale) */
    protected Position3D deriveAccumulee = new Position3D(0, 0, 0);

    /** Liste des alertes reçues */
    private final List<String> alertesRecues;

    /**
     * Constructeur protégé pour les sous-classes.
     * 
     * @param position     la position initiale
     * @param vitesseMax   la vitesse maximale en m/s
     * @param autonomieMax l'autonomie maximale en heures
     * @throws IllegalArgumentException si les paramètres sont invalides
     */
    protected ActifMobile(Position3D position, double vitesseMax, double autonomieMax) {
        if (position == null) {
            throw new IllegalArgumentException("La position ne peut pas être nulle");
        }
        if (vitesseMax <= 0) {
            throw new IllegalArgumentException("La vitesse maximale doit être positive");
        }
        if (autonomieMax <= 0) {
            throw new IllegalArgumentException("L'autonomie maximale doit être positive");
        }

        this.id = UUID.randomUUID().toString();
        this.position = position;
        this.vitesseMax = vitesseMax;
        this.autonomieMax = autonomieMax;
        this.autonomieRestante = 100.0;
        this.etatOperationnel = EtatOperationnel.AU_SOL;
        this.enMarche = false;
        this.alertesRecues = new ArrayList<>();
    }

    /**
     * Constructeur avec ID personnalisé pour les tests.
     * 
     * @param id           l'identifiant personnalisé
     * @param position     la position initiale
     * @param vitesseMax   la vitesse maximale en m/s
     * @param autonomieMax l'autonomie maximale en heures
     */
    protected ActifMobile(String id, Position3D position, double vitesseMax, double autonomieMax) {
        this(position, vitesseMax, autonomieMax);
    }

    // Getters et setters avec validation

    public String getId() {
        return id;
    }

    @Override
    public Position3D getPosition() {
        return position;
    }

    /**
     * Modifie la position de l'actif avec validation.
     * 
     * @param nouvellePosition la nouvelle position
     * @throws IllegalArgumentException si la position est nulle
     */
    protected void setPosition(Position3D nouvellePosition) {
        if (nouvellePosition == null) {
            throw new IllegalArgumentException("La position ne peut pas être nulle");
        }
        this.position = nouvellePosition;
    }

    public double getVitesseMax() {
        return vitesseMax;
    }

    public double getAutonomieMax() {
        return autonomieMax;
    }

    @Override
    public double getAutonomieRestante() {
        return autonomieRestante;
    }

    public EtatOperationnel getEtatOperationnel() {
        return etatOperationnel;
    }

    /**
     * Modifie l'état opérationnel de l'actif.
     * 
     * @param etat le nouvel état
     * @throws IllegalArgumentException si l'état est nul
     */
    public void setEtatOperationnel(EtatOperationnel etat) {
        if (etat == null) {
            throw new IllegalArgumentException("L'état opérationnel ne peut pas être nul");
        }
        this.etatOperationnel = etat;
    }

    @Override
    public boolean estEnMarche() {
        return enMarche;
    }

    public void setZoneOperation(ZoneOperation zone) {
        if (this.zoneOperation != null) {
            this.zoneOperation.retirerActif(this);
        }
        this.zoneOperation = zone;
        if (this.zoneOperation != null) {
            this.zoneOperation.enregistrerActif(this);
        }
    }

    public ZoneOperation getZoneOperation() {
        return zoneOperation;
    }

    // Implémentation de Rechargeable

    @Override
    public void recharger() {
        this.autonomieRestante = 100.0;
        if (this.etatOperationnel == EtatOperationnel.AU_SOL) {
            System.out.println("Actif " + id + " rechargé à 100%");
        }
    }

    @Override
    public void ravitailler() {
        recharger(); // Par défaut, similaire au rechargement
    }

    @Override
    public void consommerAutonomie(double montant) {
        this.autonomieRestante = Math.max(0, this.autonomieRestante - montant);
        if (this.autonomieRestante <= 0) {
            this.etatOperationnel = EtatOperationnel.EN_PANNE;
            notifierEtatCritique("BATTERIE_CRITIQUE");
        } else if (estEnEtatCritique()) {
            notifierEtatCritique("BATTERIE_CRITIQUE");
        }
    }

    // Implémentation de Communicable

    @Override
    public boolean transmettreAlerte(String message, ActifMobile actifCible) {
        if (actifCible == null || message == null) {
            return false;
        }
        actifCible.recevoirAlerte(message, this);
        return true;
    }

    @Override
    public void recevoirAlerte(String message, ActifMobile emetteur) {
        String alerteComplete = "Alerte de " + emetteur.getId() + ": " + message;
        alertesRecues.add(alerteComplete);
        System.out.println("Actif " + id + " a reçu: " + alerteComplete);
    }

    // Implémentation de Pilotable

    @Override
    public boolean demarrer() {
        if (etatOperationnel == EtatOperationnel.EN_PANNE) {
            System.out.println("Impossible de démarrer: actif en panne");
            return false;
        }
        if (etatOperationnel == EtatOperationnel.EN_MAINTENANCE) {
            System.out.println("Impossible de démarrer: actif en maintenance");
            return false;
        }

        enMarche = true;
        if (etatOperationnel == EtatOperationnel.AU_SOL) {
            etatOperationnel = EtatOperationnel.EN_MISSION;
        }
        return true;
    }

    @Override
    public boolean eteindre() {
        enMarche = false;
        if (etatOperationnel == EtatOperationnel.EN_MISSION) {
            etatOperationnel = EtatOperationnel.AU_SOL;
        }
        return true;
    }

    // Implémentation de Alertable

    @Override
    public void notifierEtatCritique(String message) {
        System.out.println("ALERTE CRITIQUE [" + message + "] pour actif " + id);
    }

    @Override
    public boolean estEnEtatCritique() {
        return autonomieRestante < SEUIL_AUTONOMIE_CRITIQUE ||
                etatOperationnel == EtatOperationnel.EN_PANNE;
    }

    // Implémentation de Deplacable (abstraite, à implémenter par les sous-classes)

    @Override
    public abstract boolean deplacer(Position3D cible);

    @Override
    public abstract List<Position3D> calculerTrajet(Position3D cible);

    /**
     * Déplace l'actif vers la cible en fonction du temps écoulé (simulation).
     * 
     * @param cible la position cible
     * @param dt    le temps écoulé en secondes
     * @return true si la cible est atteinte, false sinon
     */
    /**
     * Déplace l'actif vers la cible en fonction du temps écoulé (simulation).
     * 
     * @param cible la position cible
     * @param dt    le temps écoulé en secondes
     * @return true si la cible est atteinte, false sinon
     */
    public boolean avancerVers(Position3D cible, double dt) {
        if (cible == null || dt <= 0)
            return false;

        // Si en panne ou batterie vide, on ne bouge plus
        if (etatOperationnel == EtatOperationnel.EN_PANNE || autonomieRestante <= 0) {
            if (autonomieRestante <= 0)
                notifierEtatCritique("BATTERIE_CRITIQUE");
            return false;
        }

        // --- CLAMPING DESTINATION (Refinement) ---
        // If target is unreachable (inside obstacle), get closest valid point.
        Position3D destinationEffective = cible;
        if (zoneOperation != null) {
            if (this instanceof ActifAerien) {
                // Keep for potential future use or debugging if needed, but remove unused var
                // for now
                // double altitude = ((ActifAerien) this).getAltitude();
            }
            // Marine uses Z directly which is depth, handled by ZoneOperation logic usually
            // check against ZMax
            // For simplicity, passing current Z or explicit param if strictly needed.
            // ActifMobile does not know it's Aerien/Marin directly without casting, but
            // getPosition().getZ() is generic.

            // We use current Z for this check.
            destinationEffective = zoneOperation.getClampedTarget(this, position, cible, position.getZ());
        }

        // 1. Calculer le vecteur de propulsion vers la destination effective
        double distanceTotale = position.distanceVers(destinationEffective);

        // If we are effectively AT the clamped destination (close enough), we are done.
        // This handles the "go to wall and stop" requirement.
        if (distanceTotale < 1.0) { // 1 meter tolerance for exact arrival at clamped point
            return true;
        }

        // --- STEERING AVOIDANCE (Soft avoidance) ---
        double dxP = (destinationEffective.getX() - position.getX()) / distanceTotale;
        double dyP = (destinationEffective.getY() - position.getY()) / distanceTotale;
        double dzP = (destinationEffective.getZ() - position.getZ()) / distanceTotale;

        if (zoneOperation != null) {
            List<ActifMobile> voisins = zoneOperation.getVoisins(this, 80.0); // 80m detection range
            for (ActifMobile v : voisins) {
                Position3D posV = v.getPosition();
                double dist = position.distanceVers(posV);
                if (dist < 60.0) { // Proximity threshold
                    // Calculate lateral vector to push away from neighbor
                    double lx = position.getX() - posV.getX();
                    double ly = position.getY() - posV.getY();

                    // If almost perfectly aligned on Y, add a small bias to "choose a side"
                    if (Math.abs(ly) < 1.0) {
                        ly += 5.0; // Bias to steer "up" (Y+)
                    }

                    double lNorm = Math.sqrt(lx * lx + ly * ly);
                    if (lNorm > 0) {
                        // Apply a steering force proportional to inverse distance
                        double force = (60.0 - dist) / 60.0;
                        dxP += (lx / lNorm) * force;
                        dyP += (ly / lNorm) * force;
                        // Re-normalize propulsion vector
                        double newNorm = Math.sqrt(dxP * dxP + dyP * dyP + dzP * dzP);
                        dxP /= newNorm;
                        dyP /= newNorm;
                        dzP /= newNorm;
                    }
                }
            }
        }

        double vitessePropulsion = vitesseMax;

        // Appliquer les limitations de précipitation si applicable (via surcharge
        // possible ou calcul direct)
        vitessePropulsion = ajusterVitesseSelonEnvironnement(vitessePropulsion);

        double distanceParcourable = vitessePropulsion * dt;

        double dxEnv = 0;
        double dyEnv = 0;
        double dzEnv = 0;

        if (zoneOperation != null) {
            if (this instanceof ActifAerien) {
                fr.spiga.environment.Vent v = zoneOperation.getVent();
                if (v != null) {
                    dxEnv = v.getDirection().getX() * (v.getIntensite() / 100.0) * dt;
                    dyEnv = v.getDirection().getY() * (v.getIntensite() / 100.0) * dt;
                }
            } else if (this instanceof ActifMarin) {
                fr.spiga.environment.CourantMarin c = zoneOperation.getCourantMarin();
                if (c != null) {
                    dxEnv = c.getDirection().getX() * (c.getIntensite() / 100.0) * dt;
                    dyEnv = c.getDirection().getY() * (c.getIntensite() / 100.0) * dt;
                    dzEnv = c.getDirection().getZ() * (c.getIntensite() / 100.0) * dt;
                }
            }
        }

        // 3. Consommation (dépend de la distance de propulsion)
        double consommation = calculerConsommation(Math.min(distanceTotale, distanceParcourable));

        if (consommation > autonomieRestante) {
            // Pas assez d'énergie pour tout le trajet
            double ratio = autonomieRestante / consommation;
            distanceParcourable *= ratio;
            consommerAutonomie(autonomieRestante); // Vider le reste

            // L'actif s'arrête et tombe en panne
            this.etatOperationnel = EtatOperationnel.EN_PANNE;
            notifierEtatCritique("BATTERIE_CRITIQUE");
            System.out.println("Actif " + id + " en panne sèche !");

            // On fait le petit bond qui restait
            if (distanceParcourable > 0.1) {
                deplacerPartiellement(destinationEffective, distanceTotale, distanceParcourable);
            }
            return false;
        } else {
            consommerAutonomie(consommation);
        }

        // 4. Calculer la nouvelle position cible (Propulsion + Dérive)
        double dxPropTotal, dyPropTotal, dzPropTotal;

        double distAEffectuer = Math.min(distanceTotale, distanceParcourable);
        dxPropTotal = dxP * distAEffectuer;
        dyPropTotal = dyP * distAEffectuer;
        dzPropTotal = dzP * distAEffectuer;

        // 5. DÉTECTION ET MOUVEMENT PAR SOUS-ETAPES (SUB-STEPPING)
        double distStepTotal = Math
                .sqrt(dxPropTotal * dxPropTotal + dyPropTotal * dyPropTotal + dzPropTotal * dzPropTotal);
        int nbSubSteps = (int) Math.max(1, Math.ceil(distStepTotal / 10.0));

        // Vecteur par pas
        double dxStep = (dxPropTotal + dxEnv) / nbSubSteps;
        double dyStep = (dyPropTotal + dyEnv) / nbSubSteps;
        double dzStep = (dzPropTotal + dzEnv) / nbSubSteps;

        Position3D currentPos = position;

        for (int i = 0; i < nbSubSteps; i++) {
            Position3D nextPos = new Position3D(
                    currentPos.getX() + dxStep,
                    currentPos.getY() + dyStep,
                    currentPos.getZ() + dzStep);

            if (zoneOperation != null) {
                // Vérification des collision sur le point suivant
                boolean collision = false;

                // Obstacles
                for (fr.spiga.environment.Obstacle obs : zoneOperation.getObstacles()) {
                    if (obs.estEnCollision(nextPos)) {
                        // LOGIQUE D'ÉVITEMENT
                        // Even with clamping, we might hit other obstacles along the path.
                        // Keep avoidance logic.

                        // 1. Flyover check
                        if (currentPos.getZ() > obs.getZMax() + 10.0) {
                            continue; // Flyover ok
                        }

                        // 2. Contournement
                        double dxObs = nextPos.getX() - obs.getPosition().getX();
                        double dyObs = nextPos.getY() - obs.getPosition().getY();
                        double distObs = Math.sqrt(dxObs * dxObs + dyObs * dyObs);

                        if (distObs > 0.1) {
                            // Vecteur normalisé
                            double nx = dxObs / distObs;
                            double ny = dyObs / distObs;

                            // Vecteur tangent pour le glissement (Main droite)
                            double tx = -ny;
                            double ty = nx;

                            // On pousse le point à l'extérieur du rayon avec une marge
                            double penetration = obs.getRayon() - distObs + 5.0; // +5m marge

                            // Ajuster nextPos pour "glisser" sur l'obstacle
                            double slideFactor = 2.0;

                            nextPos = new Position3D(
                                    nextPos.getX() + nx * penetration + tx * slideFactor,
                                    nextPos.getY() + ny * penetration + ty * slideFactor,
                                    nextPos.getZ());
                        } else {
                            // Trop proche du centre ou bug
                            collision = true;
                        }
                    }
                }

                // Zones d'exclusion (Bloquante)
                if (zoneOperation.estDansZoneExclusion(nextPos)) {
                    collision = true;
                }

                if (collision) {
                    this.etatOperationnel = EtatOperationnel.EN_PANNE;
                    notifierEtatCritique("PANNE_SYSTEME");
                    System.err.println("CRASH/BLOCAGE ! L'actif " + id + " bloqué à " + nextPos);
                    return false; // Arrêt immédiat
                }
            }

            // Valider le pas
            currentPos = nextPos;
        }

        // 6. Mise à jour finale
        // On vérifie une dernière fois si on est sorti de la map
        if (zoneOperation != null && !zoneOperation.estDansZone(currentPos)) {
            return false;
        }

        setPosition(currentPos);
        return distanceTotale <= distanceParcourable;
    }

    /**
     * Ajuste a velocidade máxima de acordo com fatores ambientais (ex: chuva
     * forte).
     */
    protected double ajusterVitesseSelonEnvironnement(double vMax) {
        if (zoneOperation != null) {
            fr.spiga.environment.Precipitation p = zoneOperation.getPrecipitationAt(getPosition());
            double intensity = p.getIntensite();
            if (intensity > 50) {
                return vMax * (1.0 - (intensity - 50) / 100.0); // Réduction jusqu'à 50%
            }
        }
        return vMax;
    }

    private void deplacerPartiellement(Position3D cible, double distanceTotale, double distanceParcourable) {
        double ratio = distanceParcourable / distanceTotale;
        double dx = (cible.getX() - position.getX()) * ratio;
        double dy = (cible.getY() - position.getY()) * ratio;
        double dz = (cible.getZ() - position.getZ()) * ratio;

        setPosition(new Position3D(
                position.getX() + dx,
                position.getY() + dy,
                position.getZ() + dz));
    }

    /**
     * Méthode template pour calculer la consommation d'énergie.
     * Peut être surchargée par les sous-classes pour des calculs spécifiques.
     * 
     * @param distance la distance parcourue en mètres
     * @return la consommation en pourcentage d'autonomie
     */
    protected double calculerConsommation(double distance) {
        // Consommation réduite pour permettre des missions plus longues
        // Ex: 0.4% par km
        double consommationBase = (distance / 1000.0) * 0.4;
        return consommationBase;
    }

    /**
     * Obtient le nom du type d'actif.
     * 
     * @return le nom du type
     */
    public abstract String getType();

    @Override
    public String toString() {
        return String.format("%s[id=%s, position=%s, autonomie=%.1f%%, état=%s]",
                getType(), id.substring(0, 8), position, autonomieRestante, etatOperationnel);
    }
}
