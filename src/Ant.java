public class Ant {
    private boolean hasFood = false;
    private double lastSignal = 0;
    private double orientation; //Math.random() * 90;

    public boolean getHasFood() {
        return hasFood;
    }

    public void setHasFood(boolean has_food) {
        this.hasFood = has_food;
    }

    public double getLastSignal() {
        return lastSignal;
    }

    public void setLastSignal(double last_signal) {
        this.lastSignal = last_signal;
    }

    public double getOrientation() {
        return orientation;
    }

    public void setOrientation(double orientation) {
        this.orientation = orientation;
    }
}
