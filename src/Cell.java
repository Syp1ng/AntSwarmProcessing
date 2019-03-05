public class Cell {
    private Ant ant;

    public Ant getAnt() {
        return ant;
    }
    public boolean hasAnt(){
        if(ant!=null)return true;
        else return false;
    }

    public void setAnt(Ant ant) {
        this.ant = ant;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public double getSignal() {
        return signal;
    }

    public void setSignal(double signal) {
        this.signal = signal;
    }

    private int food;
    private double signal;
    }