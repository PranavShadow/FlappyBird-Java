import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
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
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game logic and Game Physics
    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    boolean gameStarted = false; // tracks if the game has begun
    double score = 0;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images into the declared Images
        try {
            backgroundImg = new ImageIcon(getClass().getResourceAsStream("/flappybirdbg.png").readAllBytes()).getImage();
            birdImg = new ImageIcon(getClass().getResourceAsStream("/flappybird.png").readAllBytes()).getImage();
            topPipeImg = new ImageIcon(getClass().getResourceAsStream("/toppipe.png").readAllBytes()).getImage();
            bottomPipeImg = new ImageIcon(getClass().getResourceAsStream("/bottompipe.png").readAllBytes()).getImage();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        // Game timer — runs the game loop at ~60 frames per second
        gameLoop = new Timer(1000/60, this);
        gameLoop.start(); // start loop but gameStarted=false keeps game frozen on start screen
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random() * (pipeHeight/2));
        int openingSpace = boardHeight/4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // Draw bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // Draw pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Draw start modal
        if (!gameStarted && !gameOver) {
            drawStartModal(g);
            return;
        }

        // Draw game over modal
        if (gameOver) {
            drawGameOverModal(g);
            return;
        }

        // Draw live score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.drawString(String.valueOf((int) score), 10, 35);
    }

    void drawStartModal(Graphics g) {
        // Semi-transparent dark overlay
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(50, boardHeight/2 - 80, boardWidth - 100, 160, 20, 20);

        // Title text
        g.setColor(Color.yellow);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        String title = "Flappy Bird";
        g.drawString(title, (boardWidth - fm.stringWidth(title)) / 2, boardHeight/2 - 30);

        // Instruction text
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        fm = g.getFontMetrics();
        String msg = "Press SPACE to Start";
        g.drawString(msg, (boardWidth - fm.stringWidth(msg)) / 2, boardHeight/2 + 10);

        // Hint text
        g.setColor(new Color(200, 200, 200));
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        fm = g.getFontMetrics();
        String hint = "Press SPACE to flap";
        g.drawString(hint, (boardWidth - fm.stringWidth(hint)) / 2, boardHeight/2 + 40);
    }

    void drawGameOverModal(Graphics g) {
        // Semi-transparent dark overlay
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(50, boardHeight/2 - 100, boardWidth - 100, 200, 20, 20);

        // Game Over title
        g.setColor(Color.red);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        FontMetrics fm = g.getFontMetrics();
        String title = "Game Over!";
        g.drawString(title, (boardWidth - fm.stringWidth(title)) / 2, boardHeight/2 - 40);

        // Score text
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        fm = g.getFontMetrics();
        String scoreText = "Score: " + (int) score;
        g.drawString(scoreText, (boardWidth - fm.stringWidth(scoreText)) / 2, boardHeight/2);

        // Divider line
        g.setColor(new Color(255, 255, 255, 80));
        g.drawLine(70, boardHeight/2 + 15, boardWidth - 70, boardHeight/2 + 15);

        // Restart instruction
        g.setColor(Color.yellow);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        fm = g.getFontMetrics();
        String restart = "Press SPACE to Restart";
        g.drawString(restart, (boardWidth - fm.stringWidth(restart)) / 2, boardHeight/2 + 40);
    }

    public void move() {
        if (!gameStarted || gameOver) return; // freeze movement until game starts

        // Apply gravity to bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        // Move pipes and check collision/scoring
        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {

            // Start the game from the start screen
            if (!gameStarted && !gameOver) {
                gameStarted = true;
                placePipeTimer.start();
                velocityY = -9; // first flap on start
                return;
            }

            // Restart after game over
            if (gameOver) {
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                gameStarted = true;
                score = 0;
                placePipeTimer.start();
                return;
            }

            // Normal flap during gameplay
            velocityY = -9;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}