import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images are declared
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird Variables and Bird Class
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipe Variables and Pipe Class
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  // scaled by 1/6
    int pipeHeight = 512;
    
    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false; // tracks if bird has passed this pipe (for scoring)

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game logic and Game Physics
    Bird bird;
    int velocityX = -4; // speed by which pipes move left (simulates bird moving right)
    int velocityY = 0;  // speed by which bird moves up and down
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images into the declared Images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Bird & Pipes
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // Place pipes timer — spawns a new pipe pair every 1.5 seconds
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipeTimer.start();
        
        // Game timer — runs the game loop at ~60 frames per second
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
    }
    
    void placePipes() {
        // Randomize the top pipe's Y so the gap appears at different heights each time
        // Range: from -pipeHeight/4 down to -3/4 pipeHeight
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random() * (pipeHeight/2));
        int openingSpace = boardHeight/4; // vertical gap the bird must fly through

        // Place top pipe at the randomized Y position
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);
    
        // Place bottom pipe directly below the top pipe, offset by pipe height + gap
        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g); // delegate all drawing to the draw method
    }

    public void draw(Graphics g) {
        // Draw background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        // Draw bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // Draw all pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Draw score (or game over message) in white at top-left
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        // Apply gravity to bird and clamp to top of canvas
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        // Move each pipe left and check for scoring / collision
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            // Bird fully passed this pipe — award 0.5 points (x2 pipes = 1 point per pair)
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }

            // Collision with any pipe ends the game
            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        // Bird fell below the canvas — game over
        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    // AABB (axis-aligned bounding box) collision check between bird and a pipe
    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&      // a's left edge hasn't passed b's right edge
               a.x + a.width > b.x &&      // a's right edge has passed b's left edge
               a.y < b.y + b.height &&     // a's top edge hasn't passed b's bottom edge
               a.y + a.height > b.y;       // a's bottom edge has passed b's top edge
    }

    @Override
    public void actionPerformed(ActionEvent e) { // called every ~16ms by the game loop timer
        move();
        repaint();
        if (gameOver) {
            // Stop both timers when the game ends
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }  

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9; // make bird jump upward

            if (gameOver) {
                // Restart: reset bird position, clear pipes, and restart both timers
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
            }
        }
    }

    // Not needed — required by KeyListener interface
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}