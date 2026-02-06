package fr.spiga.mission;

import fr.spiga.core.Position3D;
import fr.spiga.fleet.DroneReconnaissance;
import fr.spiga.fleet.VehiculeSousMarin;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MissionInspectionSousMarineTest {

    @Test
    void testCompatibility() {
        MissionInspectionSousMarine mission = new MissionInspectionSousMarine(
                LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                new Position3D(0, 0, -100));

        VehiculeSousMarin sub = new VehiculeSousMarin(new Position3D(0, 0, -10));
        DroneReconnaissance drone = new DroneReconnaissance(new Position3D(0, 0, 100));

        assertTrue(mission.estCompatible(sub), "Submarine should be compatible");
        assertFalse(mission.estCompatible(drone), "Drone should NOT be compatible");
    }

    @Test
    void testAssignmentEnforcement() {
        MissionInspectionSousMarine mission = new MissionInspectionSousMarine(
                LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                new Position3D(0, 0, -100));

        DroneReconnaissance drone = new DroneReconnaissance(new Position3D(0, 0, 100));
        assertFalse(mission.assignerActif(drone), "Should fail to assign incompatible asset");
        assertTrue(mission.getActifsAssignes().isEmpty());
    }
}
