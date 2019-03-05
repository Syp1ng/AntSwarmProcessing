public class Ant {
    boolean has_food = false;
    double last_signal = 0;

    public boolean isHas_food() {
        return has_food;
    }

    public void setHas_food(boolean has_food) {
        this.has_food = has_food;
    }

    public double getLast_signal() {
        return last_signal;
    }

    public void setLast_signal(double last_signal) {
        this.last_signal = last_signal;
    }

    public double getOrientation() {
        return orientation;
    }

    public void setOrientation(double orientation) {
        this.orientation = orientation;
    }

    double orientation; //Math.random() * 90;
}
