import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
public class FlappyBird extends JPanel {
    int boardWidth = 360;
    int boardHeight = 640;

    //Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    FlappyBird(){
        setPreferredSize(new Dimension(boardWidth,boardHeight));
        setBackground(Color.blue);
    }
}
