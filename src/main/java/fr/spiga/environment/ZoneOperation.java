package fr.spiga.environment;

import fr.spiga.core.Position3D;
import fr.spiga.core.ActifMobile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe centrale gérant la zone d'opération et tous ses facteurs
 * environnementaux.
 * Cette classe intègre les facteurs dynamiques (vent, courants, précipitations)
 * ainsi que les contraintes géographiques (limites, obstacles, zones
 * interdites).
 * 
 * @author SPIGA Team
 * @version 1.0
 */
public class ZoneOperation implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Limites de la zone */
    private final Position3D limitesMin;
    private final Position3D limitesMax;
    private Position3D rainZoneMin;
    private Position3D rainZoneMax;

    /** Facteurs atmosphériques */
    private Vent vent;
    private Precipitation precipitation;

    /** Facteurs hydriques */
    private CourantMarin courantMarin;

    /** Obstacles fixes */
    private final List<Obstacle> obstacles;

    /** Zones d'exclusion */
    private final List<ZoneExclusion> zonesExclusion;

    /** Actifs présents dans la zone */
    private final List<ActifMobile> actifs;

    /**
     * Constructeur de la zone d'opération.
     * 
     * @param limitesMin les limites minimales (coin inférieur)
     * @param limitesMax les limites maximales (coin supérieur)
     */
    public ZoneOperation(Position3D limitesMin, Position3D limitesMax) {
        if (limitesMin == null || limitesMax == null) {
            throw new IllegalArgumentException("Les limites ne peuvent pas être nulles");
        }
        if (limitesMin.getX() >= limitesMax.getX() ||
                limitesMin.getY() >= limitesMax.getY()) {
            throw new IllegalArgumentException("Les limites min doivent être < limites max");
        }

        this.limitesMin = limitesMin;
        this.limitesMax = limitesMax;
        this.obstacles = new ArrayList<>();
        this.zonesExclusion = new ArrayList<>();
        this.actifs = new ArrayList<>();

        // Conditions par défaut (calmes)
        this.vent = new Vent(new Position3D(0, 0, 0), 0.0);
        this.precipitation = new Precipitation();
        this.courantMarin = new CourantMarin(new Position3D(0, 0, 0), 0.0);

        // Zone de pluie par défaut (une partie de la carte)
        this.rainZoneMin = new Position3D(20000, 20000, -2000);
        this.rainZoneMax = new Position3D(60000, 60000, 10000);
    }

    /**
     * Obtient la précipitation à une position donnée.
     */
    public Precipitation getPrecipitationAt(Position3D pos) {
        if (pos.getX() >= rainZoneMin.getX() && pos.getX() <= rainZoneMax.getX() &&
                pos.getY() >= rainZoneMin.getY() && pos.getY() <= rainZoneMax.getY()) {
            return precipitation;
        }
        return new Precipitation(); // Pas de pluie ailleurs
    }

    public Position3D getRainZoneMin() {
        return rainZoneMin;
    }

    public Position3D getRainZoneMax() {
        return rainZoneMax;
    }

    public Position3D getLimitesMin() {
        return limitesMin;
    }

    public Position3D getLimitesMax() {
        return limitesMax;
    }

    public Vent getVent() {
        return vent;
    }

    public void setVent(Vent vent) {
        if (vent == null) {
            throw new IllegalArgumentException("Le vent ne peut pas être nul");
        }
        this.vent = vent;
    }

    public Precipitation getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(Precipitation precipitation) {
        if (precipitation == null) {
            throw new IllegalArgumentException("La précipitation ne peut pas être nulle");
        }
        this.precipitation = precipitation;
    }

    public CourantMarin getCourantMarin() {
        return courantMarin;
    }

    public void setCourantMarin(CourantMarin courantMarin) {
        if (courantMarin == null) {
            throw new IllegalArgumentException("Le courant marin ne peut pas être nul");
        }
        this.courantMarin = courantMarin;
    }

    /**
     * Ajoute un obstacle à la zone.
     * 
     * @param obstacle l'obstacle à ajouter
     */
    public void ajouterObstacle(Obstacle obstacle) {
        if (obstacle != null) {
            obstacles.add(obstacle);
        }
    }

    /**
     * Ajoute une zone d'exclusion.
     * 
     * @param zone la zone à ajouter
     */
    public void ajouterZoneExclusion(ZoneExclusion zone) {
        if (zone != null) {
            zonesExclusion.add(zone);
        }
    }

    public List<Obstacle> getObstacles() {
        return new ArrayList<>(obstacles);
    }

    public List<ZoneExclusion> getZonesExclusion() {
        return new ArrayList<>(zonesExclusion);
    }

    /**
     * Enregistre um ativo na zona.
     */
    public void enregistrerActif(ActifMobile actif) {
        if (actif != null && !actifs.contains(actif)) {
            actifs.add(actif);
        }
    }

    /**
     * Remove um ativo da zona.
     */
    public void retirerActif(ActifMobile actif) {
        actifs.remove(actif);
    }

    public List<ActifMobile> getTousLesActifs() {
        return new ArrayList<>(actifs);
    }

    /**
     * Busca ativos vizinhos em um determinado raio.
     */
    public List<ActifMobile> getVoisins(ActifMobile demandeur, double rayon) {
        List<ActifMobile> voisins = new ArrayList<>();
        Position3D posD = demandeur.getPosition();
        for (ActifMobile a : actifs) {
            if (a != demandeur && a.getEtatOperationnel() != fr.spiga.core.EtatOperationnel.EN_PANNE) {
                if (posD.distanceVers(a.getPosition()) <= rayon) {
                    voisins.add(a);
                }
            }
        }
        return voisins;
    }

    /**
     * Vérifie si une position est dans les limites de la zone.
     * 
     * @param position la position à vérifier
     * @return true si dans la zone, false sinon
     */
    public boolean estDansZone(Position3D position) {
        return position.getX() >= limitesMin.getX() && position.getX() <= limitesMax.getX() &&
                position.getY() >= limitesMin.getY() && position.getY() <= limitesMax.getY() &&
                position.getZ() >= limitesMin.getZ() && position.getZ() <= limitesMax.getZ();
    }

    /**
     * Vérifie si une position est en collision avec un obstacle.
     * 
     * @param position la position à vérifier
     * @return true si collision, false sinon
     */
    public boolean estEnCollisionAvecObstacle(Position3D position) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle.estEnCollision(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si une position est dans une zone d'exclusion.
     * 
     * @param position la position à vérifier
     * @return true si dans une zone interdite, false sinon
     */
    public boolean estDansZoneExclusion(Position3D position) {
        for (ZoneExclusion zone : zonesExclusion) {
            if (zone.contientPosition(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calcule le facteur d'ajustement de consommation pour une position donnée.
     * Tient compte des conditions environnementales.
     * 
     * @param position la position
     * @return le facteur multiplicateur (1.0 = normal, >1.0 = plus de consommation)
     */
    public double calculerFacteurConsommation(Position3D position) {
        double facteur = 1.0;

        // Facteur vent
        if (vent != null && vent.getIntensite() > 0) {
            facteur += vent.getIntensite() / 200.0; // Max +50%
        }

        // Facteur précipitation
        if (precipitation != null && precipitation.getIntensite() > 0) {
            facteur += precipitation.getIntensite() / 300.0; // Max +33%
        }

        // Facteur courant marin
        if (courantMarin != null && courantMarin.getIntensite() > 0) {
            facteur += courantMarin.getIntensite() / 250.0; // Max +40%
        }

        return facteur;
    }

    /**
     * Calcule le point accessible le plus proche pour une position donnée et une
     * altitude/profondeur.
     * Si la position est dans un obstacle bloquant (compte tenu de zAlt), retourne
     * le point sur la bordure.
     * Sinon retourne la position elle-même.
     * 
     * @param position la position cible idéale
     * @param zAlt     l'altitude ou profondeur de l'actif
     * @return la position accessible la plus proche
     */
    /**
     * Calcule la destination ajustée (clamped) si la cible est dans un obstacle.
     * Cherche l'intersection entre le segment [depart, destination] et les
     * obstacles.
     * Retourne le point d'intersection (avec marge) le plus proche du départ.
     * 
     * @param demandeur   l'actif qui demande le trajet (pour s'exclure des
     *                    collisions)
     * @param depart      la position de départ de l'actif
     * @param destination la position cible idéale
     * @param zAlt        l'altitude ou profondeur de l'actif (pour le flyover)
     * @return la position accessible la plus proche sur le trajet
     */
    public Position3D getClampedTarget(ActifMobile demandeur, Position3D depart, Position3D destination, double zAlt) {
        Position3D meilleurPoint = destination;
        double minT = 1.0;
        boolean clamped = false;

        for (Obstacle obs : obstacles) {
            // 1. Rayon effectif (marge de sécurité incluse)
            double rayon = obs.getRayon() + 5.0; // 5m margin

            // 2. Check Vertical
            if (zAlt > obs.getZMax() + 10.0)
                continue; // Flyover

            // 3. Check si destination est dedans (2D)
            double dxDest = destination.getX() - obs.getPosition().getX();
            double dyDest = destination.getY() - obs.getPosition().getY();
            boolean destInside = (dxDest * dxDest + dyDest * dyDest) < (rayon * rayon);

            if (!destInside)
                continue; // Si la destination n'est pas dans cet obstacle, on ignore (on gère l'arrivée
                          // finale)

            // 4. Calcul Intersection Rayon-Cercle
            // Segment P = depart + t * D
            double dx = destination.getX() - depart.getX();
            double dy = destination.getY() - depart.getY();

            // Vecteur F = depart - centre
            double fx = depart.getX() - obs.getPosition().getX();
            double fy = depart.getY() - obs.getPosition().getY();

            // Equation quadratique at^2 + bt + c = 0
            double a = dx * dx + dy * dy;
            double b = 2 * (fx * dx + fy * dy);
            double c = (fx * fx + fy * fy) - (rayon * rayon);

            double delta = b * b - 4 * a * c;

            if (delta >= 0) {
                // On cherche la plus petite solution positive t
                double t1 = (-b - Math.sqrt(delta)) / (2 * a);
                double t2 = (-b + Math.sqrt(delta)) / (2 * a);

                double t = -1.0;
                if (t1 >= 0 && t1 <= 1.0)
                    t = t1;
                else if (t2 >= 0 && t2 <= 1.0)
                    t = t2;

                if (t >= 0 && t < minT) {
                    minT = t;
                    clamped = true;
                }
            }
        }

        // 5. Check against other vehicles
        for (ActifMobile out : actifs) {
            if (out == demandeur)
                continue;
            // Un veículo parado ou arrivando no ponto conta como obstáculo se estiver
            // "ocupando" o ponto.
            // Para simplificar, tratamos todos os ativos como obstáculos circulares de
            // raio 15m.
            double rayonVehicule = 15.0;

            // Check Vertical (mesma "fatia" Z)
            if (Math.abs(zAlt - out.getPosition().getZ()) > 10.0)
                continue;

            double dxDest = destination.getX() - out.getPosition().getX();
            double dyDest = destination.getY() - out.getPosition().getY();
            boolean destInside = (dxDest * dxDest + dyDest * dyDest) < (rayonVehicule * rayonVehicule);

            if (!destInside)
                continue;

            double dx = destination.getX() - depart.getX();
            double dy = destination.getY() - depart.getY();
            double fx = depart.getX() - out.getPosition().getX();
            double fy = depart.getY() - out.getPosition().getY();

            double a = dx * dx + dy * dy;
            double b = 2 * (fx * dx + fy * dy);
            double c = (fx * fx + fy * fy) - (rayonVehicule * rayonVehicule);

            double delta = b * b - 4 * a * c;
            if (delta >= 0) {
                double t1 = (-b - Math.sqrt(delta)) / (2 * a);
                double t2 = (-b + Math.sqrt(delta)) / (2 * a);
                double t = -1.0;
                if (t1 >= 0 && t1 <= 1.0)
                    t = t1;
                else if (t2 >= 0 && t2 <= 1.0)
                    t = t2;

                if (t >= 0 && t < minT) {
                    minT = t;
                    clamped = true;
                }
            }
        }

        if (clamped) {
            // Calculer le point exact
            double dx = destination.getX() - depart.getX();
            double dy = destination.getY() - depart.getY();
            double dz = destination.getZ() - depart.getZ();

            // On recule un tout petit peu (0.1%) pour être sûr d'être dehors
            double tSafe = Math.max(0, minT - 0.001);

            meilleurPoint = new Position3D(
                    depart.getX() + dx * tSafe,
                    depart.getY() + dy * tSafe,
                    depart.getZ() + dz * tSafe // On garde la pente Z
            );
        }

        return meilleurPoint;
    }

    @Override
    public String toString() {
        return String.format("ZoneOperation[limites=%s à %s, obstacles=%d, zones exclusion=%d]",
                limitesMin, limitesMax, obstacles.size(), zonesExclusion.size());
    }
}
