import java.util.Random;

public class Physics extends Application {

    Random rand = new Random();
    public Physics(int sizeX, int sizeY) {
        grid_length = sizeX;
        _data = new String[sizeX][sizeY];
        grid = new Cell[sizeX][sizeY];
        temp_grid = new Cell[sizeX][sizeY];
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        for(int x=0;x< sizeX;x++) {
            for (int y = 0; y < sizeY; y++) {
                grid[x][y] = new Cell();
                temp_grid[x][y] = new Cell();
            }
        }
        place_food();
        run_time_step();
    }

    private int grid_length = 500;

    String _data[][]; // data from previous update
    //int grid_length = 150;
    Cell[][] grid;
    Cell[][] temp_grid;
    int sizeX, sizeY;
    int max_ants_on_grid = 100;
    int ants_out_of_nest = 0;

        private String  color_for_cell(Cell cell){
            if (cell.hasAnt()) {
                return cell.getAnt().has_food ? "rgb(159,248,101)" : "rgb(0,0,0)";
            } else if (cell.getFood() > 0) {
                return "rgba(86,169,46," + Math.pow(cell.getFood() / 10, 0.5) + ")";
            } else {
                if (cell.getSignal() > 0.0) {
                    double signal = cell.getSignal() > 1 ? 1 : cell.getSignal();
                    return "rgba(17,103,189," + signal + ")";
                } else return "rgb(250,250,250)";
            }
        }

        private String opacity_for_signal(Cell cell) {
            return cell.hasAnt() ? "1.0" : String.valueOf(cell.getSignal());
        }

        double to_radians(double degrees){

            return degrees * Math.PI / 180;
        }

        public void place_food () {
            int centerX =  Math.toIntExact(Math.round(grid_length * 0.8));
            int centerY = centerX;
            var max_distance = grid_length / 10;
            for (int x = centerX - max_distance; x <= centerX + max_distance; x++) {
                for (int y = centerY - max_distance; y < centerY + max_distance; y++) {
                    int bounded_i = get_bounded_index(x);
                    int bounded_ii = get_bounded_index(y);
                    double distance = calc_distance(centerX, centerY, bounded_i, bounded_ii);
                    int food_level = Math.toIntExact(Math.round(10 - Math.pow(distance, 1.2)));
                    grid[x][y].setFood(food_level);
                }
            }
        }

        public void run_time_step () {
            move_ants();
            check_for_food();
            sense_signal();
            generateGuiData();
        }

    private void generateGuiData() {
            for(int x =0; x<sizeX;x++){
                for(int y = 0; y<sizeY;y++){
                    _data[x][y] = color_for_cell(grid[x][y]);
                }
            }
    }

    public void sense_signal () {
            for (int x = 0; x < grid_length; x = x + 1) {
                for (int y = 0; y < grid_length; y = y + 1) {
                    if (grid[x][y].hasAnt()) {
                        grid[x][y].getAnt().last_signal = grid[x][y].getSignal();
                    }
                }
            }
        }

        public void move_ants () {
            for (int x = 0; x < grid_length; x = x + 1) {
                for (int y = 0; y < grid_length; y = y + 1) {
                    if (grid[x][y].hasAnt()) {
                        move_ant(x, y);
                    }
                }
            }
            // signal
            for (var i = 0; i < grid_length; i = i + 1) {
                for (var ii = 0; ii < grid_length; ii = ii + 1) {
                    // adjust reference
                    grid[i][ii].setAnt(temp_grid[i][ii].getAnt());
                    if (grid[i][ii].hasAnt() && grid[i][ii].getAnt().has_food) {
                        int bounded_i = get_bounded_index(i);
                        int bounded_ii = get_bounded_index(ii);
                        double signal_strength = 1 - Math.pow(0.5, 1 / calc_distance(i, ii, bounded_i, bounded_ii));
                        grid[bounded_i][bounded_ii].setSignal(grid[bounded_i][bounded_ii].getSignal()+ signal_strength);
                        // is the ant near the nest with food? drop food
                        if (i < 5 && ii < 5) {
                            grid[i][ii].getAnt().has_food = false;
                        }
                    } else {
                        grid[i][ii].setSignal(grid[i][ii].getSignal()*0.95);
                    }
                    if (grid[i][ii].getSignal() < 0.05) {
                        grid[i][ii].setSignal(0);
                    }
                }
            }
            move_ant_out_of_nest();
        }


        private void move_ant_out_of_nest () {
            int x = 0;
            int y = 0;
            var new_coords = get_random_coordinates(x, y);
            x = new_coords[0];
            y = new_coords[1];
            if (!grid[x][y].hasAnt() && ants_out_of_nest < max_ants_on_grid) {
                grid[x][y].setAnt(new Ant());
                temp_grid[x][y].setAnt(grid[x][y].getAnt());
                ants_out_of_nest++;
            }
        }

        public int[] get_coords_from_orientation (int i, int ii){
            int coords[] = new int[2];
            double orientation_radians = to_radians(grid[i][ii].getAnt().orientation);
            coords[0] = get_bounded_index(Math.toIntExact(Math.round(i + Math.cos(orientation_radians))));
            coords[1] =get_bounded_index(Math.toIntExact(Math.round(ii + Math.sin(orientation_radians))));
            return coords;
        }

        private void move_ant (int x, int y){
            int newX, newY;
            int[] newCordsArray;
            if (grid[x][y].getAnt().has_food) {
                var current_distance = calc_distance_to_nest(x, y);
                do {
                    grid[x][y].getAnt().orientation = Math.random() * 360;
                    newCordsArray = get_coords_from_orientation(x, y);
                    newX = newCordsArray[0];
                    newY = newCordsArray[1];
                } while (calc_distance_to_nest(newX, newY) >= current_distance);
            } else {
                // random movement in case there is no signal
                newCordsArray = get_coords_from_orientation(x, y);
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
                        int bounded_n_i = get_bounded_index(n_i);
                        int bounded_n_ii = get_bounded_index(n_ii);
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
                newCordsArray = get_random_coordinates(x, y);
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

        public double calc_distance ( int x1, int y1, int x2, int y2){
            return Math.pow(Math.pow(Math.abs(x1 - x2), 2) + Math.pow(Math.abs(y1 - y2), 2), 0.5);
        }

        public double calc_distance_to_nest ( int x, int y){
            return calc_distance(x, y, 0, 0);
        }

        public int[] get_random_coordinates ( int x, int y){
            int j = get_random_int(x - 1, x + 1);
            int jj = get_random_int(y - 1, y + 1);
            j = get_bounded_index(j);
            jj = get_bounded_index(jj);
            int[] reValue = new int[2];
            reValue[0] = j;
            reValue[1] = jj;
            return reValue;
        }

        public void check_for_food (){
            for (int x = 0; x < grid_length; x = x + 1) {
                for (int y = 0; y < grid_length; y = y + 1) {
                    if (grid[x][y].hasAnt() && !grid[x][y].getAnt().has_food) {
                        if (grid[x][y].getFood() > 0) {
                            grid[x][y].getAnt().has_food = true;

                            grid[x][y].setFood(grid[x][y].getFood()-1);
                        }
                    }
                }
            }
        }

        public int get_random_int ( int min, int max){
            return rand.nextInt(max - min + 1) + min;
        }

        public int get_bounded_index ( int index){
            int bounded_index = index;
            if (index < 0) {
                bounded_index = 0;
            }
            if (index >= grid_length) {
                bounded_index = grid_length - 1;
            }
            return bounded_index;
        }

    public String[][] get_data() {
        return _data;
    }

    public void restart() {
            temp_grid = null;
            grid= null;
    }
}
