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

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getIi() {
        return ii;
    }

    public void setIi(int ii) {
        this.ii = ii;
    }

    public double getSignal() {
        return signal;
    }

    public void setSignal(double signal) {
        this.signal = signal;
    }

    private int food;
    private  int i;
    private  int ii;
    private double signal;
    }