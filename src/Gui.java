import processing.core.PApplet;

public class Gui extends PApplet {
private String[][] colors;
private Physics p;
private int gridsInX =100;
private int gridsInY =100;
private int rectSize=4;
private int restartButtonHight =20;
    public void settings() {
        size(gridsInY *rectSize, gridsInX *rectSize+restartButtonHight);

    }
    public void setup(){
        background(0xff0097);
        p= new Physics(gridsInX, gridsInY);
    }
    public void draw(){
        colors=p.get_data();
        for (int x = 0; x < gridsInY; x++) {
            for (int y = 0; y < gridsInX; y++) {
                String stringThisRect = colors[x][y];
                String[] colorThisRect =colorStringToInt(stringThisRect);
                if(colorThisRect.length==3){
                    fill(Integer.valueOf(colorThisRect[0]),Integer.valueOf(colorThisRect[1]), Integer.valueOf(colorThisRect[2]));
                }
                else{
                    fill(Integer.valueOf(colorThisRect[0]),Integer.valueOf(colorThisRect[1])
                            , Integer.valueOf(colorThisRect[2]), Math.round(Double.valueOf(colorThisRect[3])*255));
                }
                noStroke();
                rect(x*rectSize,y*rectSize,rectSize,rectSize);
            }
        }
        p.run_time_step();


        rect(rectSize*gridsInX, rectSize*gridsInY,restartButtonHight, gridsInY*rectSize);
        textSize(32);
        text("Restart",rectSize*gridsInX+restartButtonHight, rectSize*gridsInY);
        //RestartButon
        if(mousePressed){
            if(mouseY>rectSize*gridsInY){
                p = new Physics(gridsInX, gridsInY);
            }
        }
    }
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
