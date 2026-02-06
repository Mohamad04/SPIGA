package fr.spiga.fleet;

import fr.spiga.core.Position3D;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour Essaim.
 */
class EssaimTest {

    @Test
    void testCreation() {
        Essaim essaim = new Essaim("Test");
        assertEquals("Test", essaim.getNom());
        assertEquals(0, essaim.getNombreActifs());
    }

    @Test
    void testAjouterActif() {
        Essaim essaim = new Essaim("Test");
        DroneReconnaissance drone = new DroneReconnaissance(new Position3D(0, 0, 100));

        assertTrue(essaim.ajouterActif(drone));
        assertEquals(1, essaim.getNombreActifs());

        // Ne pas ajouter deux fois
        assertFalse(essaim.ajouterActif(drone));
    }

    @Test
    void testRetirerActif() {
        Essaim essaim = new Essaim("Test");
        DroneReconnaissance drone = new DroneReconnaissance(new Position3D(0, 0, 100));

        essaim.ajouterActif(drone);
        assertTrue(essaim.retirerActif(drone));
        assertEquals(0, essaim.getNombreActifs());
    }

    @Test
    void testCalculerCentre() {
        Essaim essaim = new Essaim("Test");
        essaim.ajouterActif(new DroneReconnaissance(new Position3D(0, 0, 0)));
        essaim.ajouterActif(new DroneReconnaissance(new Position3D(100, 100, 0)));

        Position3D centre = essaim.calculerCentre();
        assertNotNull(centre);
        assertEquals(50.0, centre.getX(), 0.001);
        assertEquals(50.0, centre.getY(), 0.001);
    }
}
