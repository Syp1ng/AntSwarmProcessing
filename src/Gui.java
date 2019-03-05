import processing.core.PApplet;

public class Gui extends PApplet {
public String[] colors;

    public void settings() {
        size(500, 500);
    }
    public void setup(){
        background(155,155,0);
    }
    public void draw(){
        for(int i=0; i<width;){
            for(int j = 0;j<height; ){
                fill(155,155,155,1);
                rect(i,j,20,20);
                j+=20;
            }
            i+=20;
        }
    }
    public void update(){

    }
}
