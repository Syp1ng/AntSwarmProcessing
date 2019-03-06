import processing.core.PApplet;


public class Gui extends PApplet {
private String[][] colors;
private Physics p;
private int gridLength =140; //Square gridLength * gridLength
private int rectSize=5;
private int restartButtonHight =30;

    public void settings() {
        size(gridLength *rectSize, gridLength *rectSize+restartButtonHight);
    }
    public void setup(){
        surface.setTitle("Ant Swarm Simulation");
        p= new Physics(gridLength, gridLength);
        noStroke();
    }
    public void draw(){
        colors=p.getGuiData();
        background(0xFFFFFF);
        for (int x = 0; x < gridLength; x++) {
            for (int y = 0; y < gridLength; y++) {
                String stringThisRect = colors[x][y];
                String[] colorThisRect =colorStringToInt(stringThisRect);
                if(colorThisRect.length==3){
                    fill(Integer.valueOf(colorThisRect[0]),Integer.valueOf(colorThisRect[1]), Integer.valueOf(colorThisRect[2]));
                }
                else{
                    fill(Integer.valueOf(colorThisRect[0]),Integer.valueOf(colorThisRect[1]),
                            Integer.valueOf(colorThisRect[2]), Math.round(Double.valueOf(colorThisRect[3])*255));
                }
                rect(x*rectSize,y*rectSize,rectSize,rectSize);
            }
        }
        p.runTimeStep();

        //RestartButton
        fill(102, 153, 255);
        rect(0, rectSize* gridLength, gridLength *rectSize,restartButtonHight);
        textSize(22);
        textAlign(CENTER,CENTER);
        fill(204, 0, 0);
        text("Restart", 0, rectSize* gridLength,rectSize* gridLength, restartButtonHight);
        if(mousePressed){
            if(mouseY>rectSize* gridLength){
                p = new Physics(gridLength, gridLength);
            }
        }
    }
    //takes an string and removes the unused chars,
    private String[] colorStringToInt(String s){
        s = s.replace( "rgb(", "" );
        s = s.replace("rgba","");
        s = s.replace( ")", "" );
        s = s.replace( "(", "" );
        s = s.replace( " ", "" );
        String[] splits =s.split(",");
        return splits;
    }


}
