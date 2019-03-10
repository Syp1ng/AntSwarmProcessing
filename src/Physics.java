public class Physics extends Application {

    private int gridLength;
    private String[][] guiData; // data from previous update
    private Cell[][] grid; //current Data
    private Cell[][] tempGrid; //temp Data
    private int sizeX, sizeY; //sizes are equal in this example
    private int maxAntsOnGrid = 100;
    private int antsOutOfNest = 0;

    public Physics(int size) {
        sizeX = size;
        sizeY = size;
        gridLength = sizeX;
        guiData = new String[sizeX][sizeY];
        grid = new Cell[sizeX][sizeY];
        tempGrid = new Cell[sizeX][sizeY];
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                grid[x][y] = new Cell();
                tempGrid[x][y] = new Cell();
            }
        }
        placeFood();
    }

    public Cell[][] getGrid() { // for testing purpose
        return grid;
    }

    public int getAntsOutOfNest() { //for testing purpose
        return antsOutOfNest;
    }

    public String[][] getGuiData() {
        return guiData;
    }

    private String colorForCell(Cell cell) { //take it from the requirements,(Code)
        if (cell.hasAnt()) {
            return cell.getAnt().getHasFood() ? "rgb(159,248,101)" : "rgb(0,0,0)";
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
        int centerX = Math.toIntExact(Math.round(gridLength * 0.8));
        int centerY = centerX;
        int maxDistance = gridLength / 10;
        for (int x = centerX - maxDistance; x <= centerX + maxDistance; x++) {
            for (int y = centerY - maxDistance; y < centerY + maxDistance; y++) {
                int boundedX = getBoundedIndex(x);
                int boundedY = getBoundedIndex(y);
                double distance = calcDistance(centerX, centerY, boundedX, boundedY);
                int foodLevel = Math.toIntExact(Math.round(10 - Math.pow(distance, 1.2)));
                grid[x][y].setFood(foodLevel);
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
        for (int x = 0; x < gridLength; x = x + 1) {
            for (int y = 0; y < gridLength; y = y + 1) {
                if (grid[x][y].hasAnt()) {
                    grid[x][y].getAnt().setLastSignal(grid[x][y].getSignal());
                }
            }
        }
    }

    private void moveAnts() {
        for (int x = 0; x < gridLength; x = x + 1) {
            for (int y = 0; y < gridLength; y = y + 1) {
                if (grid[x][y].hasAnt()) {
                    moveAnt(x, y);
                }
            }
        }
        // signal
        for (int x = 0; x < gridLength; x = x + 1) {
            for (int y = 0; y < gridLength; y = y + 1) {
                // adjust reference
                grid[x][y].setAnt(tempGrid[x][y].getAnt());
                if (grid[x][y].hasAnt() && grid[x][y].getAnt().getHasFood()) {
                    int boundedX = getBoundedIndex(x);
                    int boundedY = getBoundedIndex(y);
                    double signalStrength = 1 - Math.pow(0.5, 1 / calcDistance(x, y, boundedX, boundedY));
                    grid[boundedX][boundedY].setSignal(grid[boundedX][boundedY].getSignal() + signalStrength);
                    // is the ant near the nest with food? drop food
                    if (x < 5 && y < 5) {
                        grid[x][y].getAnt().setHasFood(false);
                    }
                } else {
                    grid[x][y].setSignal(grid[x][y].getSignal() * 0.95);
                }
                if (grid[x][y].getSignal() < 0.05) {
                    grid[x][y].setSignal(0);
                }
            }
        }
        moveAntOutOfNest();
    }


    private void moveAntOutOfNest() {
        int x = 0, y = 0;
        int[] newCoords = getRandomCoordinates(x, y);
        x = newCoords[0];
        y = newCoords[1];
        if (!grid[x][y].hasAnt() && antsOutOfNest < maxAntsOnGrid) {
            grid[x][y].setAnt(new Ant());
            tempGrid[x][y].setAnt(grid[x][y].getAnt());
            antsOutOfNest++;
        }
    }

    private int[] getCoordsFromOrientation(int x, int y) {
        int[] coords = new int[2];
        double orientationRadians = toRadians(grid[x][y].getAnt().getOrientation());
        coords[0] = getBoundedIndex(Math.toIntExact(Math.round(x + Math.cos(orientationRadians))));
        coords[1] = getBoundedIndex(Math.toIntExact(Math.round(y + Math.sin(orientationRadians))));
        return coords;
    }

    private void moveAnt(int x, int y) {
        int newX, newY;
        int[] newCoordsArray;
        if (grid[x][y].getAnt().getHasFood()) {
            double currentDistance = calcDistanceToNest(x, y);
            do {
                grid[x][y].getAnt().setOrientation(Math.random() * 360);
                newCoordsArray = getCoordsFromOrientation(x, y);
                newX = newCoordsArray[0];
                newY = newCoordsArray[1];
            } while (calcDistanceToNest(newX, newY) >= currentDistance);
        } else {
            // random movement in case there is no signal
            newCoordsArray = getCoordsFromOrientation(x, y);
            newX = newCoordsArray[0];
            newY = newCoordsArray[1];
            grid[x][y].getAnt().setOrientation(grid[x][y].getAnt().getOrientation() + Math.random() * 45 - 22.5);
            // let's check for some signal
            double last = grid[x][y].getAnt().getLastSignal();
            double current;
            int min = 0, max = 0;
            for (int nX = x - 1; nX <= x + 1; nX++) {
                for (int nY = y - 1; nY <= y + 1; nY++) {
                    int boundedNX = getBoundedIndex(nX);
                    int boundedNY = getBoundedIndex(nY);
                    current = grid[boundedNX][boundedNY].getSignal();
                    if (current == 0) {
                        continue;
                    }
                    double diff = last - current;
                    if (last == 0) {
                        if (diff < min) {
                            newX = boundedNX;
                            newY = boundedNY;
                        }
                    } else {
                        if (diff > max) {
                            newX = boundedNX;
                            newY = boundedNY;
                        }
                    }
                }
            }
        }
        // some randomness
        if (Math.random() < 0.05) {
            newCoordsArray = getRandomCoordinates(x, y);
            newX = newCoordsArray[0];
            newY = newCoordsArray[1];
        }
        // now that we have new coords:
        if (!tempGrid[newX][newY].hasAnt()) {
            // adjust reference
            tempGrid[newX][newY].setAnt(tempGrid[x][y].getAnt());
            tempGrid[x][y].setAnt(null);
        }
    }

    private double calcDistance(int x1, int y1, int x2, int y2) {
        return Math.pow(Math.pow(Math.abs(x1 - x2), 2) + Math.pow(Math.abs(y1 - y2), 2), 0.5);
    }

    private double calcDistanceToNest(int x, int y) {
        return calcDistance(x, y, 0, 0);
    }

    private int[] getRandomCoordinates(int x, int y) {
        int newX = getRandomInt(x - 1, x + 1);
        int newy = getRandomInt(y - 1, y + 1);
        newX = getBoundedIndex(newX);
        newy = getBoundedIndex(newy);
        int[] reValue = new int[2];
        reValue[0] = newX;
        reValue[1] = newy;
        return reValue;
    }

    private void checkForFood() {
        for (int x = 0; x < gridLength; x = x + 1) {
            for (int y = 0; y < gridLength; y = y + 1) {
                if (grid[x][y].hasAnt() && !grid[x][y].getAnt().getHasFood()) {
                    if (grid[x][y].getFood() > 0) {
                        grid[x][y].getAnt().setHasFood(true);
                        grid[x][y].setFood(grid[x][y].getFood() - 1);
                    }
                }
            }
        }
    }

    private int getRandomInt(int min, int max) {
        return (int) Math.floor(Math.random() * (max - min + 1)) + min;
    }

    int getBoundedIndex(int index) {
        int boundedIndex = index;
        if (index < 0) {
            boundedIndex = 0;
        }
        if (index >= gridLength) {
            boundedIndex = gridLength - 1;
        }
        return boundedIndex;
    }
}
