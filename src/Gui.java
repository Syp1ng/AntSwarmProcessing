import processing.core.PApplet;

public class Gui extends PApplet {
    private String[][] colors; //colors to display
    private Physics p; //where the physic happens
    private int gridLength = 130; //Square gridLength * gridLength
    private int rectSize = 5;  //Sets the size of the rectangles
    private int restartButtonHeight = 30;

    public void settings() {
        size(gridLength * rectSize, gridLength * rectSize + restartButtonHeight);
    }

    public void setup() {
        surface.setTitle("Ant Swarm Simulation");
        p = new Physics(gridLength);
        noStroke();
    }

    public void draw() {
        p.runTimeStep(); //do physics
        colors = p.getGuiData();//get current data
        background(0xFFFFFF);
        for (int x = 0; x < gridLength; x++) {
            for (int y = 0; y < gridLength; y++) {
                String stringThisRect = colors[x][y];
                String[] colorThisRect = splitColorString(stringThisRect);
                //set color
                if (colorThisRect.length == 3) {
                    fill(Integer.valueOf(colorThisRect[0]), Integer.valueOf(colorThisRect[1]), Integer.valueOf(colorThisRect[2]));
                } else {
                    fill(Integer.valueOf(colorThisRect[0]), Integer.valueOf(colorThisRect[1]),
                            Integer.valueOf(colorThisRect[2]), Math.round(Double.valueOf(colorThisRect[3]) * 255));
                }
                rect(x * rectSize, y * rectSize, rectSize, rectSize);//Rectangle set
            }
        }
        //RestartButton
        fill(102, 153, 255);
        rect(0, rectSize * gridLength, gridLength * rectSize, restartButtonHeight);
        textSize(22);
        textAlign(CENTER, CENTER);
        fill(204, 0, 0);
        text("Restart", 0, rectSize * gridLength, rectSize * gridLength, restartButtonHeight);
        if (mousePressed) {
            if (mouseY > rectSize * gridLength) {
                p = new Physics(gridLength);
            }
        }
    }

    //takes an string and removes the unused chars, that Processing can handle the Strings
    String[] splitColorString(String s) {
        s = s.replace("rgb(", "");
        s = s.replace("rgba", "");
        s = s.replace(")", "");
        s = s.replace("(", "");
        s = s.replace(" ", "");
        String[] splits = s.split(",");
        return splits;
    }


}
