package fr.spiga.core;

import fr.spiga.environment.ZoneOperation;
import fr.spiga.fleet.VehiculeSurface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CollisionEvitementTest {
    private ZoneOperation zone;
    private Position3D min = new Position3D(0, 0, -100);
    private Position3D max = new Position3D(1000, 1000, 100);

    @BeforeEach
    public void setup() {
        zone = new ZoneOperation(min, max);
    }

    @Test
    public void testDestinationOccupancy() {
        // Vehicle A is at target (50, 50, 0)
        VehiculeSurface vA = new VehiculeSurface(new Position3D(50, 50, 0));
        vA.setZoneOperation(zone);
        vA.setEtatOperationnel(EtatOperationnel.AU_SOL); // Stationary

        // Vehicle B tries to go to (50, 50, 0)
        VehiculeSurface vB = new VehiculeSurface(new Position3D(0, 50, 0));
        vB.setZoneOperation(zone);
        vB.setEtatOperationnel(EtatOperationnel.EN_MISSION);

        // Calculate clamped target for B
        Position3D clamped = zone.getClampedTarget(vB, vB.getPosition(), new Position3D(50, 50, 0), 0);

        // Distance to target should be at least the safety radius (15m defined in
        // ZoneOperation)
        double distanceToA = clamped.distanceVers(vA.getPosition());
        assertTrue(distanceToA >= 14.5, "Should stop at safe distance, found " + distanceToA);
    }

    @Test
    public void testSteeringAvoidance() {
        // Vehicle A is at (50, 50, 0) - halfway on the path of B
        VehiculeSurface vA = new VehiculeSurface(new Position3D(50, 50, 0));
        vA.setZoneOperation(zone);
        vA.setEtatOperationnel(EtatOperationnel.AU_SOL);

        // Vehicle B is at (0, 50, 0) going to (100, 50, 0)
        // Its direct path goes through (50, 50, 0)
        VehiculeSurface vB = new VehiculeSurface(new Position3D(0, 50, 0));
        vB.setZoneOperation(zone);
        vB.setEtatOperationnel(EtatOperationnel.EN_MISSION);
        vB.demarrer();

        // One step of movement
        vB.avancerVers(new Position3D(100, 50, 0), 0.5); // move 0.5s at speed (e.g. 20m/s)

        // After move, it should have some lateral deviation (dy != 50)
        Position3D pos = vB.getPosition();
        assertTrue(Math.abs(pos.getY() - 50.0) > 0.001, "Should have steered away from the path, but staying at Y=50");
    }
}
