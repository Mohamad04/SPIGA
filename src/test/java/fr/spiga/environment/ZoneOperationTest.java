package fr.spiga.environment;

import fr.spiga.core.Position3D;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ZoneOperation.
 */
class ZoneOperationTest {

    @Test
    void testCreation() {
        Position3D min = new Position3D(0, 0, -1000);
        Position3D max = new Position3D(10000, 10000, 1000);
        ZoneOperation zone = new ZoneOperation(min, max);

        assertNotNull(zone);
        assertEquals(min, zone.getLimitesMin());
        assertEquals(max, zone.getLimitesMax());
    }

    @Test
    void testEstDansZone() {
        ZoneOperation zone = new ZoneOperation(
                new Position3D(0, 0, 0),
                new Position3D(1000, 1000, 1000));

        assertTrue(zone.estDansZone(new Position3D(500, 500, 500)));
        assertFalse(zone.estDansZone(new Position3D(2000, 500, 500)));
    }

    @Test
    void testAjouterObstacle() {
        ZoneOperation zone = new ZoneOperation(
                new Position3D(0, 0, 0),
                new Position3D(1000, 1000, 1000));

        Obstacle obstacle = new Obstacle(new Position3D(500, 500, 0), 100, "Montagne");
        zone.ajouterObstacle(obstacle);

        assertEquals(1, zone.getObstacles().size());
    }

    @Test
    void testEstEnCollisionAvecObstacle() {
        ZoneOperation zone = new ZoneOperation(
                new Position3D(0, 0, 0),
                new Position3D(1000, 1000, 1000));

        Obstacle obstacle = new Obstacle(new Position3D(500, 500, 0), 100, "Test");
        zone.ajouterObstacle(obstacle);

        assertTrue(zone.estEnCollisionAvecObstacle(new Position3D(500, 500, 0)));
        assertFalse(zone.estEnCollisionAvecObstacle(new Position3D(800, 800, 0)));
    }
}
