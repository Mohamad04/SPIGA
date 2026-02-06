package fr.spiga.physics;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import fr.spiga.core.*;
import fr.spiga.environment.*;
import fr.spiga.fleet.DroneReconnaissance;
import fr.spiga.fleet.VehiculeSurface;

public class PhysicsIntegrationTest {

    @Test
    public void testWindDrift() {
        // Drone à (0,0,1000) veut aller à (1000, 0, 1000)
        Position3D start = new Position3D(0, 0, 1000);
        Position3D target = new Position3D(1000, 0, 1000);
        DroneReconnaissance drone = new DroneReconnaissance(start);

        ZoneOperation zone = new ZoneOperation(new Position3D(-10000, -10000, 0), new Position3D(50000, 50000, 5000));
        // Vent fort vers le Nord (Y+)
        zone.setVent(new Vent(new Position3D(0, 1, 0), 100.0));
        drone.setZoneOperation(zone);
        drone.demarrer();

        // Simuler 1 seconde
        drone.avancerVers(target, 1.0);

        Position3D pos = drone.getPosition();
        // Le drone doit avoir bougé vers X+ (propulsion) et Y+ (dérive du vent)
        assertTrue(pos.getX() > 0, "Drone should move towards target X");
        assertTrue(pos.getY() > 0, "Drone should drift due to north wind (Y+)");
        System.out.println("Position after drift: " + pos);
    }

    @Test
    public void testCollisionWithObstacle() {
        Position3D start = new Position3D(0, 0, 0);
        Position3D target = new Position3D(100, 0, 0); // Directly into center of obstacle
        VehiculeSurface boat = new VehiculeSurface(start);

        ZoneOperation zone = new ZoneOperation(new Position3D(-1000, -1000, -100), new Position3D(5000, 5000, 5000));
        // Obstacle à (100, 0, 0)
        zone.ajouterObstacle(new Obstacle(new Position3D(100, 0, 0), 20.0, "Rocher"));
        boat.setZoneOperation(zone);
        boat.demarrer();

        // Simuler 5 secondes
        boat.avancerVers(target, 5.0);

        // Verification: The boat should have stopped BEFORE the obstacle (safe camping)
        // Obstacle is at (100,0,0) with rayon 20 + margin 5 = 25.
        // Edge is at 100 - 25 = 75.
        assertTrue(boat.getPosition().getX() <= 80.0, "Boat should stop before obstacle");
        assertEquals(EtatOperationnel.EN_MISSION, boat.getEtatOperationnel(), "Boat should be safe (stay in mission)");
    }
}
