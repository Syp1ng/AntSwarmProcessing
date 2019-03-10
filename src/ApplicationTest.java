import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationTest {
    private static Physics physics;
    int size = 100;

    @Test
    void placedFood() {
        physics = new Physics(size);
        int countFoodCells = 0;
        for (Cell[] gridRow : physics.getGrid()) {
            for (Cell c : gridRow) {
                if (c.getFood() > 0) {
                    countFoodCells++;
                }
            }
        }
        assertTrue(countFoodCells >= 1);
    }

    @Test
    void antMove() {
        int antsX = 0, antsY = 0;
        physics = new Physics(size);
        for (int i = 0; i <= 10; i++) {
            physics.runTimeStep();
        }
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (physics.getGrid()[x][y].hasAnt()) {
                    if (x > antsX) antsX = x;
                    if (y > antsY) antsY = y;
                }
            }
        }
        Assertions.assertTrue(antsX > 0 || antsY > 0);
    }

    @Test
    void checkBound() {
        physics = new Physics(size);
        Assertions.assertEquals(size - 1, physics.getBoundedIndex(120));
        Assertions.assertEquals(0, physics.getBoundedIndex(-5));
    }

    @Test
    void carryFood() {
        int executions = 500;//higher value for true
        physics = new Physics(size);
        int totalFood = 0, currentFood = 0;
        for (Cell[] gridRow : physics.getGrid()) {
            for (Cell c : gridRow) {
                if (c.getFood() > 0) totalFood += c.getFood();
            }
        }
        boolean oneAntCarryFood = false;
        for (int i = 0; i <= executions; i++) {
            physics.runTimeStep();
            for (Cell[] gridRow : physics.getGrid()) {
                for (Cell c : gridRow) {
                    if (c.hasAnt() && c.getAnt().getHasFood()) oneAntCarryFood = true;
                }
            }
        }
        for (Cell[] gridRow : physics.getGrid()) {
            for (Cell c : gridRow) {
                if (c.getFood() > 0) currentFood += c.getFood();
            }
        }
        Assertions.assertTrue(oneAntCarryFood);
        Assertions.assertTrue(currentFood < totalFood);
    }

    @Test
    void newAnts() {
        int currentAnts = 0;
        physics = new Physics(size);
        for (int i = 0; i <= 150; i++) {
            physics.runTimeStep();
        }
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (physics.getGrid()[x][y].hasAnt()) {
                    currentAnts++;
                }
            }
        }
        Assertions.assertTrue(physics.getAntsOutOfNest() > 10 && physics.getAntsOutOfNest() <= 100);
        Assertions.assertTrue(currentAnts > 10 && currentAnts <= 100);
    }

    @Test
    void placeSignal() {
        boolean signalPlaced = false;
        physics = new Physics(size);
        for (int i = 0; i <= 500; i++) {
            physics.runTimeStep();
        }
        //little bit random
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (physics.getGrid()[x][y].getSignal() > 0) {
                    signalPlaced = true;
                }
            }
        }
        Assertions.assertTrue(signalPlaced);
    }

    @Test
    void color() {
        physics = new Physics(size);
        physics.runTimeStep();
        int white = 0, ant = 0;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (physics.getGuiData()[x][y].equals("rgb(250,250,250)")) {
                    white++;
                } else if (physics.getGuiData()[x][y].equals("rgb(0,0,0)")) {
                    ant++;
                }
            }
        }
        //At Beginning nealy everything is white, (there are a few ants and food only)
        Assertions.assertTrue(white > 0.8 * size * size);
        Assertions.assertTrue(ant < 0.001 * size * size);
        Assertions.assertTrue(ant < 10);
    }

    @Test
    void guiColorSplit() {
        Gui g = new Gui();
        String[] expected1 = {"0", "0", "0"};
        String[] expected2 = {"250", "200", "222"};
        String[] expected3 = {"114", "241", "54", "0.5"};
        Assertions.assertArrayEquals(expected1, g.splitColorString("rgb(0,0,0)"));
        Assertions.assertArrayEquals(expected2, g.splitColorString("rgb(250,200,222)"));
        Assertions.assertArrayEquals(expected3, g.splitColorString(" rgba(114,241,54,0.5"));
    }
}