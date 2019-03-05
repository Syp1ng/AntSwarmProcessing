public class Physics extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    //Starts here..............................
    //copied text from http://www.natureincode.com/code/various/ants.html
    //just need to manage from js to java. vars-> int/doble etc functions
    Random rand = new Random();

    private int grid_length = 500;

    String _data[]; // data from previous update
    int grid_length = 150;
    Cell[] grid;
    Cell[] temp_grid[];
    var population = [];
    int max_ants_on_grid = 100;
    int ms_between_updates = 33;
    int ants_out_of_nest = 0;

    public void draw_grid(data) {
        int width = 600;
        int height = 600;
        int grid_length = data.length;
        double width_cell = width/grid_length;
        double height_cell = height/grid_length;

        var canvas = document.getElementById("grid")
        if (canvas == null) {
            canvas = document.createElement('canvas');
            canvas.id = "grid";
            canvas.width = width;
            canvas.height = height;
            document.getElementsByTagName('body')[0].appendChild(canvas);
        }

        var context = canvas.getContext("2d");

        public void draw_cells(){

            for (var i = 0; i < grid_length; i++) {
                for (var ii = 0; ii < grid_length; ii++) {
                    if (_data && _data[i][ii] === color_for_cell(data[i][ii])) {
                        continue;
                    }
                    context.clearRect(i*width_cell, ii*height_cell, width_cell, height_cell);
                    context.fillStyle = color_for_cell(data[i][ii]);
                    context.fillRect(i*width_cell, ii*height_cell, width_cell, height_cell);
                }
            }

        }
        draw_cells();
        if (!_data) {
            _data = [];
        }
        for (var i = 0; i < grid_length; i++) {
            _data[i] = [];
            for (var ii = 0; ii < grid_length; ii++){
                _data[i][ii] = color_for_cell(data[i][ii]);
            }
        }
    }

    public void update_grid(data) {
        draw_grid(data);
    }




    var color_for_cell = function (cell) {
        if (cell.has_ant()) {
            return cell.ant.has_food ? "rgb(159,248,101)" : "rgb(0,0,0)";
        }
        else if (cell.food > 0) {
            return "rgba(86,169,46,"+Math.pow(cell.food/10,0.5)+")";
        }
        else {
            if (cell.signal > 0) {
                var signal = cell.signal > 1 ? 1 : cell.signal;
                return "rgba(17,103,189,"+cell.signal+")";
            }
            else return "rgb(250,250,250)";
        }
    }

    var opacity_for_signal = function (cell) {
        return cell.has_ant() ? "1.0": cell.signal;
    }

    Math.to_radians = function(degrees) {
        return degrees * Math.PI / 180;
    };

    Math.to_degrees = function(radians) {
        return radians * 180 / Math.PI;
    };



    function init_grids() {
        for (var i = 0; i < grid_length; i = i + 1) {
            grid[i] = [];
            temp_grid[i] = [];
            for (var ii = 0; ii < grid_length; ii = ii + 1) {
                grid[i][ii] = new Cell(i,ii);
                temp_grid[i][ii] = new Cell(i,ii);
            }
        }
    }

    function initialize_simulation() {
        init_grids();
        place_food();
        draw_grid(grid.map(function(row) {return row.map(function(cell) {return cell;});}));
    }

    initialize_simulation();
    var interval_id = setInterval(simulate_and_visualize, ms_between_updates);


    function simulate_and_visualize() {
        run_time_step();
        update_grid(grid.map(function(row) {return row.map(function(cell) {return cell;});}));
    }

    public void place_food() {
        var center_i = Math.round(grid_length * 0.8);
        var center_ii = center_i;
        var max_distance = grid_length/10;
        for (var i = center_i-max_distance; i <= center_i+max_distance; i++) {
            for (var ii = center_ii-max_distance; ii < center_ii+max_distance; ii++) {
                bounded_i = get_bounded_index(i);
                bounded_ii = get_bounded_index(ii);
                var distance = calc_distance(center_i,center_ii,bounded_i,bounded_ii)
                var food_level = Math.round(10 - Math.pow(distance,1.2));
                grid[i][ii].food = food_level;
            }
        }
    }

    public void run_time_step() {
        move_ants();
        check_for_food();
        sense_signal();
    }

    public void sense_signal() {
        for (var i = 0; i < grid_length; i = i + 1) {
            for (var ii = 0; ii < grid_length; ii = ii + 1) {
                if (grid[i][ii].has_ant()) {
                    grid[i][ii].ant.last_signal = grid[i][ii].signal;
                }
            }
        }
    }

    public void move_ants() {
        for (var i = 0; i < grid_length; i = i + 1) {
            for (var ii = 0; ii < grid_length; ii = ii + 1) {
                if (grid[i][ii].has_ant()) {
                    move_ant(i,ii);
                }
            }
        }
        // signal
        for (var i = 0; i < grid_length; i = i + 1) {
            for (var ii = 0; ii < grid_length; ii = ii + 1) {
                // adjust reference
                grid[i][ii].ant = temp_grid[i][ii].ant;
                if (grid[i][ii].has_ant() && grid[i][ii].ant.has_food) {
                    bounded_i = get_bounded_index(i);
                    bounded_ii = get_bounded_index(ii);
                    var signal_strength = 1 - Math.pow(0.5,1/calc_distance(i,ii,bounded_i,bounded_ii));
                    grid[bounded_i][bounded_ii].signal += signal_strength;
                    // is the ant near the nest with food? drop food
                    if (i < 5 && ii < 5) {
                        grid[i][ii].ant.has_food = false;
                    }
                }
                else {
                    grid[i][ii].signal *= 0.95;
                }
                if (grid[i][ii].signal < 0.05) {
                    grid[i][ii].signal = 0;
                }
            }
        }
        move_ant_out_of_nest();
    }



    function move_ant_out_of_nest() {
        var i = 0;
        var ii = 0;
        var new_coords = get_random_coordinates(i,ii);
        var j = new_coords[0];
        var jj = new_coords[1];
        if (!grid[j][jj].has_ant() && ants_out_of_nest < max_ants_on_grid) {
            grid[j][jj].ant = new Ant();
            temp_grid[j][jj].ant = grid[j][jj].ant;
            ants_out_of_nest++;
        }
    }

    function get_coords_from_orientation(i,ii) {
        var coords = [];
        var orientation_radians = Math.to_radians(grid[i][ii].ant.orientation)
        coords.push(get_bounded_index(Math.round(i + Math.cos(orientation_radians))));
        coords.push(get_bounded_index(Math.round(ii + Math.sin(orientation_radians))));
        return coords;
    }

    function move_ant(i,ii) {
        var new_coords, j, jj;
        if (grid[i][ii].ant.has_food) {
            var current_distance = calc_distance_to_nest(i,ii);
            do {
                grid[i][ii].ant.orientation = Math.random() * 360;
                new_coords = get_coords_from_orientation(i,ii);
                j = new_coords[0];
                jj = new_coords[1];
            } while (calc_distance_to_nest(j,jj) >= current_distance);
        }
        else {
            // random movement in case there is no signal
            new_coords = get_coords_from_orientation(i,ii);
            int j = new_coords[0];
            int jj = new_coords[1];
            grid[i][ii].ant.orientation += Math.random() * 45 - 22.5;
            // let's check for some signal
            double last = grid[i][ii].ant.last_signal;
            double current;
            var min = 0;
            var max = 0;
            for (var n_i = i-1; n_i <= i+1; n_i++) {
                for (var n_ii = ii-1; n_ii <= ii+1; n_ii++) {
                    int bounded_n_i = get_bounded_index(n_i);
                    int bounded_n_ii = get_bounded_index(n_ii);
                    current = grid[bounded_n_i][bounded_n_ii].signal;
                    if (current.signal == 0) {
                        continue;
                    }
                    var diff = last-current;
                    if (last == 0) {
                        if (diff < min) {
                            j = bounded_n_i;
                            jj = bounded_n_ii;
                        }
                    }
                    else {
                        if (diff > max) {
                            j = bounded_n_i;
                            jj = bounded_n_ii;
                        }
                    }
                }
            }
        }
        // some randomness
        if (Math.random() < 0.05) {
            new_coords = get_random_coordinates(i,ii);
            j = new_coords[0];
            jj = new_coords[1];
        }
        // now that we have new coords:
        if (!temp_grid[j][jj].has_ant()) {
            // adjust reference
            temp_grid[j][jj].ant = temp_grid[i][ii].ant;
            temp_grid[i][ii].ant = null;
        }
    }

    public double calc_distance(int i,int ii, int j,int jj) {
        return Math.pow(Math.pow(Math.abs(i-j),2) + Math.pow(Math.abs(ii-jj),2) , 0.5);
    }

    public double calc_distance_to_nest(int i, int ii) {
        return calc_distance(i,ii,0,0);
    }

    public int[] get_random_coordinates(int i, int ii) {
        Random rand = new Random();
        //int randomNum = rand.nextInt((max - min) + 1) + min
        int j   = get_random_int(i-1, i+1);
        int jj  = get_random_int(ii-1, ii+1);
        j  = get_bounded_index(j);
        jj = get_bounded_index(jj);
        int[] reValue = new int[2];
        reValue[0] = j;
        reValue[1] =jj;
        return reValue;
    }

    public void check_for_food(int i,int ii) {
        for (var i = 0; i < grid_length; i = i + 1) {
            for (var ii = 0; ii < grid_length; ii = ii + 1) {
                if (grid[i][ii].has_ant() && !grid[i][ii].ant.has_food) {
                    if (grid[i][ii].food > 0) {
                        grid[i][ii].ant.has_food = true;
                        grid[i][ii].food--;
                    }
                }
            }
        }
    }

    public int get_random_int(int min, int max) {
        return rand.nextInt(max-min+1)+min;
    }

    public int get_bounded_index(int index) {
        var bounded_index = index;
        if (index < 0) {
            bounded_index = 0;
        }
        if (index >= grid_length) {
            bounded_index = grid_length-1;
        }
        return bounded_index;
    }
}
