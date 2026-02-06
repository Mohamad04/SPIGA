package fr.spiga.mission;

import fr.spiga.core.ActifMobile;
import fr.spiga.core.EtatOperationnel;
import fr.spiga.core.Position3D;
import java.util.List;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class MissionResumptionTest {

        @Test
        void testMinimalStateReset() {
                // Minimal anonymous ActifMobile for testing
                ActifMobile testActif = new ActifMobile(new Position3D(0, 0, 0), 10.0, 10.0) {
                        @Override
                        public boolean deplacer(Position3D cible) {
                                return true;
                        }

                        @Override
                        public List<Position3D> calculerTrajet(Position3D cible) {
                                return new java.util.ArrayList<>();
                        }

                        @Override
                        public void notifierEtatCritique(String message) {
                        }

                        @Override
                        public String getType() {
                                return "TestActif";
                        }
                };

                // Minimal Mission for testing
                Mission testMission = new Mission("SURVEILLANCE", LocalDateTime.now(),
                                LocalDateTime.now().plusHours(1), "Test") {
                        @Override
                        public boolean estCompatible(ActifMobile actif) {
                                return true;
                        }

                        @Override
                        protected void mettreAJourSpecifique(double dt) {
                        }

                        @Override
                        public String getNom() {
                                return "TestMission";
                        }
                };

                testMission.assignerActif(testActif);
                testMission.demarrer();
                assertEquals(EtatOperationnel.EN_MISSION, testActif.getEtatOperationnel());

                // Call terminer and check if state is reset
                testMission.terminer("Done");
                assertEquals(EtatOperationnel.AU_SOL, testActif.getEtatOperationnel());
                assertFalse(testActif.estEnMarche());
        }
}
