package fr.spiga.fleet;

import fr.spiga.core.EtatOperationnel;
import fr.spiga.core.Position3D;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour DroneReconnaissance.
 */
class DroneReconnaissanceTest {

    @Test
    void testCreation() {
        Position3D pos = new Position3D(1000, 2000, 500);
        DroneReconnaissance drone = new DroneReconnaissance(pos);

        assertNotNull(drone);
        assertEquals(pos, drone.getPosition());
        assertEquals(EtatOperationnel.AU_SOL, drone.getEtatOperationnel());
        assertEquals(100.0, drone.getAutonomieRestante());
    }

    @Test
    void testDemarrerEteindre() {
        DroneReconnaissance drone = new DroneReconnaissance(new Position3D(0, 0, 100));

        assertTrue(drone.demarrer());
        assertTrue(drone.estEnMarche());
        assertEquals(EtatOperationnel.EN_MISSION, drone.getEtatOperationnel());

        assertTrue(drone.eteindre());
        assertFalse(drone.estEnMarche());
    }

    @Test
    void testRecharger() {
        DroneReconnaissance drone = new DroneReconnaissance(new Position3D(0, 0, 100));
        drone.consommerAutonomie(50.0);
        assertEquals(50.0, drone.getAutonomieRestante());

        drone.recharger();
        assertEquals(100.0, drone.getAutonomieRestante());
    }

    @Test
    void testType() {
        DroneReconnaissance drone = new DroneReconnaissance(new Position3D(0, 0, 100));
        assertEquals("DroneReconnaissance", drone.getType());
    }
}
