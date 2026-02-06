package fr.spiga.mission;

import fr.spiga.core.Position3D;
import fr.spiga.core.Position3D;
import fr.spiga.fleet.DroneReconnaissance;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour MissionSurveillanceMaritime.
 */
class MissionSurveillanceMaritimeTest {

    @Test
    void testCreation() {
        LocalDateTime debut = LocalDateTime.now().plusHours(1);
        LocalDateTime fin = debut.plusHours(4);
        Position3D zone = new Position3D(5000, 5000, 0);

        MissionSurveillanceMaritime mission = new MissionSurveillanceMaritime(
                debut, fin, zone);

        assertNotNull(mission);
        assertEquals("PLANIFIEE", mission.getStatut());
    }

    @Test
    void testAssignerActif() {
        LocalDateTime debut = LocalDateTime.now().plusHours(1);
        LocalDateTime fin = debut.plusHours(4);

        MissionSurveillanceMaritime mission = new MissionSurveillanceMaritime(
                debut, fin, new Position3D(0, 0, 0));

        DroneReconnaissance drone = new DroneReconnaissance(new Position3D(0, 0, 100));

        assertTrue(mission.assignerActif(drone));
        assertEquals(1, mission.getActifsAssignes().size());
    }

    @Test
    void testDemarrerMission() {
        LocalDateTime debut = LocalDateTime.now().plusHours(1);
        LocalDateTime fin = debut.plusHours(4);

        MissionSurveillanceMaritime mission = new MissionSurveillanceMaritime(
                debut, fin, new Position3D(0, 0, 0));

        DroneReconnaissance drone = new DroneReconnaissance(new Position3D(0, 0, 100));
        mission.assignerActif(drone);

        assertTrue(mission.demarrer());
        assertEquals("EN_COURS", mission.getStatut());
    }

    @Test
    void testTerminerMission() {
        LocalDateTime debut = LocalDateTime.now().plusHours(1);
        LocalDateTime fin = debut.plusHours(4);

        MissionSurveillanceMaritime mission = new MissionSurveillanceMaritime(
                debut, fin, new Position3D(0, 0, 0));

        DroneReconnaissance drone = new DroneReconnaissance(new Position3D(0, 0, 100));
        mission.assignerActif(drone);
        mission.demarrer();

        assertTrue(mission.terminer("Mission réussie"));
        assertEquals("TERMINEE", mission.getStatut());
        assertNotNull(mission.getResultatsObtenus());
    }
}
