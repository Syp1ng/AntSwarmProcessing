import processing.core.PApplet;

public class Gui extends PApplet {
private String[][] colors;
private Physics p;
private int heightGrid=500;
private int widthGrid=500;
private int rectSize=5;
    public void settings() {
        size(widthGrid+rectSize, heightGrid+rectSize);

    }
    public void setup(){
        background(155,155,0);
        p= new Physics();
    }
    public void draw(){
        for(int i=0; i<widthGrid;){
            for(int j = 0;j<heightGrid; ){
                fill(155,155,155,1);
                rect(i,j,20,20);
                j+=20;
            }
            i+=20;
        }
    }
    public void update(){
        colors=p.get_data();
        for (var i = 0; i < widthGrid ; i++) {
            for (var ii = 0; ii < heightGrid; ii++) {
                String stringThisRect = colors[i][ii];
                int[] colorThisRect =colorStringToInt(stringThisRect);
                if(colorThisRect.length==3){
                    fill(colorThisRect[0],colorThisRect[1], colorThisRect[2]);
                }
                else{
                    fill(colorThisRect[0],colorThisRect[1], colorThisRect[2],colorThisRect[3]);
                }
                noStroke();
                rect(i,ii,rectSize,rectSize);
                ii+=rectSize;
            }
            i+=rectSize;
        }
    }

    private int[] colorStringToInt(String s){


        return null;
    }
    /*
        for (var i = 0; i < grid_length; i++) {
        _data[i] = [];
        for (var ii = 0; ii < grid_length; ii++){
            _data[i][ii] = color_for_cell(data[i][ii]);
        }
    }*/
}
