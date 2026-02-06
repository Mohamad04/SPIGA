package fr.spiga.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe Position3D.
 */
class Position3DTest {

    @Test
    void testConstructeur() {
        Position3D pos = new Position3D(100.0, 200.0, 300.0);
        assertEquals(100.0, pos.getX());
        assertEquals(200.0, pos.getY());
        assertEquals(300.0, pos.getZ());
    }

    @Test
    void testDistanceVers() {
        Position3D p1 = new Position3D(0, 0, 0);
        Position3D p2 = new Position3D(3, 4, 0);
        assertEquals(5.0, p1.distanceVers(p2), 0.001);
    }

    @Test
    void testDistance2DVers() {
        Position3D p1 = new Position3D(0, 0, 100);
        Position3D p2 = new Position3D(3, 4, 500);
        assertEquals(5.0, p1.distance2DVers(p2), 0.001);
    }

    @Test
    void testEquals() {
        Position3D p1 = new Position3D(10, 20, 30);
        Position3D p2 = new Position3D(10, 20, 30);
        Position3D p3 = new Position3D(10, 20, 31);

        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
    }
}
