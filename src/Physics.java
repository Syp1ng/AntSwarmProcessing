import java.util.Random;

public class Physics extends Application {

    private Random rand = new Random();
    private int grid_length;
    private String guiData[][]; // data from previous update
    private Cell[][] grid;
    private Cell[][] temp_grid;
    private int sizeX, sizeY;
    private int max_ants_on_grid = 100;
    private int ants_out_of_nest = 0;

    public Physics(int sizeX, int sizeY) {
        grid_length = sizeX;
        guiData = new String[sizeX][sizeY];
        grid = new Cell[sizeX][sizeY];
        temp_grid = new Cell[sizeX][sizeY];
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                grid[x][y] = new Cell();
                temp_grid[x][y] = new Cell();
            }
        }
        placeFood();
        runTimeStep();
    }

    private String colorForCell(Cell cell) {
        if (cell.hasAnt()) {
            return cell.getAnt().has_food ? "rgb(159,248,101)" : "rgb(0,0,0)";
        } else if (cell.getFood() > 0) {
            return "rgba(86,169,46," + Math.pow(cell.getFood() / 10.0, 0.5) + ")";
        } else {
            if (cell.getSignal() > 0.0) {
                double signal = cell.getSignal() > 1 ? 1 : cell.getSignal();
                return "rgba(17,103,189," + signal + ")";
            } else return "rgb(250,250,250)";
        }
    }

    private double toRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    private void placeFood() {
        int centerX = Math.toIntExact(Math.round(grid_length * 0.8));
        int centerY = centerX;
        var max_distance = grid_length / 10;
        for (int x = centerX - max_distance; x <= centerX + max_distance; x++) {
            for (int y = centerY - max_distance; y < centerY + max_distance; y++) {
                int bounded_i = getBoundedIndex(x);
                int bounded_ii = getBoundedIndex(y);
                double distance = calcDistance(centerX, centerY, bounded_i, bounded_ii);
                int food_level = Math.toIntExact(Math.round(10 - Math.pow(distance, 1.2)));
                grid[x][y].setFood(food_level);
            }
        }
    }

    public void runTimeStep() {
        moveAnts();
        checkForFood();
        senseSignal();
        generateGuiData();
    }

    private void generateGuiData() {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                guiData[x][y] = colorForCell(grid[x][y]);
            }
        }
    }

    private void senseSignal() {
        for (int x = 0; x < grid_length; x = x + 1) {
            for (int y = 0; y < grid_length; y = y + 1) {
                if (grid[x][y].hasAnt()) {
                    grid[x][y].getAnt().last_signal = grid[x][y].getSignal();
                }
            }
        }
    }

    private void moveAnts() {
        for (int x = 0; x < grid_length; x = x + 1) {
            for (int y = 0; y < grid_length; y = y + 1) {
                if (grid[x][y].hasAnt()) {
                    moveAnt(x, y);
                }
            }
        }
        // signal
        for (var i = 0; i < grid_length; i = i + 1) {
            for (var ii = 0; ii < grid_length; ii = ii + 1) {
                // adjust reference
                grid[i][ii].setAnt(temp_grid[i][ii].getAnt());
                if (grid[i][ii].hasAnt() && grid[i][ii].getAnt().has_food) {
                    int bounded_i = getBoundedIndex(i);
                    int bounded_ii = getBoundedIndex(ii);
                    double signal_strength = 1 - Math.pow(0.5, 1 / calcDistance(i, ii, bounded_i, bounded_ii));
                    grid[bounded_i][bounded_ii].setSignal(grid[bounded_i][bounded_ii].getSignal() + signal_strength);
                    // is the ant near the nest with food? drop food
                    if (i < 5 && ii < 5) {
                        grid[i][ii].getAnt().has_food = false;
                    }
                } else {
                    grid[i][ii].setSignal(grid[i][ii].getSignal() * 0.95);
                }
                if (grid[i][ii].getSignal() < 0.05) {
                    grid[i][ii].setSignal(0);
                }
            }
        }
        moveAntOutOfNest();
    }


    private void moveAntOutOfNest() {
        int x = 0;
        int y = 0;
        int[] newCoords = getRandomCoordinates(x, y);
        x = newCoords[0];
        y = newCoords[1];
        if (!grid[x][y].hasAnt() && ants_out_of_nest < max_ants_on_grid) {
            grid[x][y].setAnt(new Ant());
            temp_grid[x][y].setAnt(grid[x][y].getAnt());
            ants_out_of_nest++;
        }
    }

    private int[] getCoordsFromOrientation(int i, int ii) {
        int coords[] = new int[2];
        double orientation_radians = toRadians(grid[i][ii].getAnt().orientation);
        coords[0] = getBoundedIndex(Math.toIntExact(Math.round(i + Math.cos(orientation_radians))));
        coords[1] = getBoundedIndex(Math.toIntExact(Math.round(ii + Math.sin(orientation_radians))));
        return coords;
    }

    private void moveAnt(int x, int y) {
        int newX, newY;
        int[] newCordsArray;
        if (grid[x][y].getAnt().has_food) {
            var current_distance = calcDistanceToNest(x, y);
            do {
                grid[x][y].getAnt().orientation = Math.random() * 360;
                newCordsArray = getCoordsFromOrientation(x, y);
                newX = newCordsArray[0];
                newY = newCordsArray[1];
            } while (calcDistanceToNest(newX, newY) >= current_distance);
        } else {
            // random movement in case there is no signal
            newCordsArray = getCoordsFromOrientation(x, y);
            newX = newCordsArray[0];
            newY = newCordsArray[1];
            grid[x][y].getAnt().orientation += Math.random() * 45 - 22.5;
            // let's check for some signal
            double last = grid[x][y].getAnt().last_signal;
            double current;
            var min = 0;
            var max = 0;
            for (var n_i = x - 1; n_i <= x + 1; n_i++) {
                for (var n_ii = y - 1; n_ii <= y + 1; n_ii++) {
                    int bounded_n_i = getBoundedIndex(n_i);
                    int bounded_n_ii = getBoundedIndex(n_ii);
                    current = grid[bounded_n_i][bounded_n_ii].getSignal();
                    if (current == 0) {
                        continue;
                    }
                    var diff = last - current;
                    if (last == 0) {
                        if (diff < min) {
                            newX = bounded_n_i;
                            newY = bounded_n_ii;
                        }
                    } else {
                        if (diff > max) {
                            newX = bounded_n_i;
                            newY = bounded_n_ii;
                        }
                    }
                }
            }
        }
        // some randomness
        if (Math.random() < 0.05) {
            newCordsArray = getRandomCoordinates(x, y);
            newX = newCordsArray[0];
            newY = newCordsArray[1];
        }
        // now that we have new coords:
        if (!temp_grid[newX][newY].hasAnt()) {
            // adjust reference
            temp_grid[newX][newY].setAnt(temp_grid[x][y].getAnt());
            temp_grid[x][y].setAnt(null);
        }
    }

    private double calcDistance(int x1, int y1, int x2, int y2) {
        return Math.pow(Math.pow(Math.abs(x1 - x2), 2) + Math.pow(Math.abs(y1 - y2), 2), 0.5);
    }

    private double calcDistanceToNest(int x, int y) {
        return calcDistance(x, y, 0, 0);
    }

    private int[] getRandomCoordinates(int x, int y) {
        int j = getRandomInt(x - 1, x + 1);
        int jj = getRandomInt(y - 1, y + 1);
        j = getBoundedIndex(j);
        jj = getBoundedIndex(jj);
        int[] reValue = new int[2];
        reValue[0] = j;
        reValue[1] = jj;
        return reValue;
    }

    private void checkForFood() {
        for (int x = 0; x < grid_length; x = x + 1) {
            for (int y = 0; y < grid_length; y = y + 1) {
                if (grid[x][y].hasAnt() && !grid[x][y].getAnt().has_food) {
                    if (grid[x][y].getFood() > 0) {
                        grid[x][y].getAnt().has_food = true;

                        grid[x][y].setFood(grid[x][y].getFood() - 1);
                    }
                }
            }
        }
    }

    private int getRandomInt(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }

    private int getBoundedIndex(int index) {
        int bounded_index = index;
        if (index < 0) {
            bounded_index = 0;
        }
        if (index >= grid_length) {
            bounded_index = grid_length - 1;
        }
        return bounded_index;
    }

    public String[][] getGuiData() {
        return guiData;
    }
}
