import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    int boardWidth;
    int boardHeight;
    int tileSize = 25;

    //snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;

    //food
    Tile food;
    Random random;

    //game logic
    int velocityX;
    int velocityY;
    Timer gameLoop;
    boolean gameOver = false;
    boolean isPaused = false; // Added for pause functionality

    // Scoring and Difficulty
    int score = 0; // Track score explicitly
    int highScore = 0; // Track high score for the session
    int initialDelay = 120; // Initial timer delay (controls speed)
    int minDelay = 50; // Minimum delay (maximum speed)
    int speedIncrement = 2; // How much to decrease delay per food item

    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        random = new Random();
        initializeGame(); // Moved initialization logic to a separate method

        //game timer
        gameLoop = new Timer(initialDelay, this);
        gameLoop.start();
    }

    // Method to initialize or reset game state
    private void initializeGame() {
        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<Tile>();
        score = 0; // Reset score
        food = new Tile(10, 10); // Initial food position before placing properly
        placeFood(); // Place food ensuring it's not on the snake

        // Reset velocity
        velocityX = 1; // Start moving right
        velocityY = 0;

        gameOver = false;
        isPaused = false;

        // Reset game speed if timer exists
        if (gameLoop != null) {
             gameLoop.setDelay(initialDelay);
        }
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Cast Graphics to Graphics2D for better rendering options if needed later
        Graphics2D g2d = (Graphics2D) g;

        // Grid Lines (Optional, can be commented out for cleaner look)
        /*
        g.setColor(Color.darkGray);
        for (int i = 0; i < boardWidth / tileSize; i++) {
            g.drawLine(i * tileSize, 0, i * tileSize, boardHeight);
            g.drawLine(0, i * tileSize, boardWidth, i * tileSize);
        }
        */

        // Food
        g2d.setColor(Color.red);
        g2d.fill3DRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize, true);

        // Snake Head
        g2d.setColor(Color.green);
        g2d.fill3DRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize, true);

        // Snake Body
        g2d.setColor(new Color(0, 180, 0)); // Slightly different color for body
        for (Tile snakePart : snakeBody) {
            g2d.fill3DRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, true);
        }

        // Score and Game Over Text
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        if (gameOver) {
            g2d.setColor(Color.red);
            String gameOverText = "Game Over! Score: " + score;
            String restartText = "Press 'R' to Restart";
            // Center text
            FontMetrics fm = g2d.getFontMetrics();
            int gameOverWidth = fm.stringWidth(gameOverText);
            int restartWidth = fm.stringWidth(restartText);

            g2d.drawString(gameOverText, (boardWidth - gameOverWidth) / 2, boardHeight / 2 - fm.getHeight());
            g2d.drawString(restartText, (boardWidth - restartWidth) / 2, boardHeight / 2);
             // Display High Score on Game Over screen
            String highScoreText = "High Score: " + highScore;
            int highScoreWidth = fm.stringWidth(highScoreText);
            g2d.drawString(highScoreText, (boardWidth - highScoreWidth) / 2, boardHeight / 2 + fm.getHeight());

        } else {
            // Display current score and high score during gameplay
            g2d.setColor(Color.white);
            g2d.drawString("Score: " + score, tileSize - 16, tileSize);
            g2d.drawString("High Score: " + highScore, boardWidth - tileSize * 6, tileSize); // Position high score top-right

            if (isPaused) {
                 g2d.setColor(Color.yellow);
                 String pausedText = "Paused - Press 'P' to Resume";
                 FontMetrics fm = g2d.getFontMetrics();
                 int pausedWidth = fm.stringWidth(pausedText);
                 g2d.drawString(pausedText, (boardWidth - pausedWidth) / 2, boardHeight / 2);
            }
        }
    }

    // Improved food placement: ensures food does not spawn on the snake
    public void placeFood() {
        int newX, newY;
        boolean collision;
        do {
            collision = false;
            newX = random.nextInt(boardWidth / tileSize);
            newY = random.nextInt(boardHeight / tileSize);

            // Check collision with head
            if (collision(new Tile(newX, newY), snakeHead)) {
                collision = true;
                continue; // Try again
            }

            // Check collision with body
            for (Tile snakePart : snakeBody) {
                if (collision(new Tile(newX, newY), snakePart)) {
                    collision = true;
                    break; // No need to check further parts
                }
            }
        } while (collision); // Keep trying until a free spot is found

        food.x = newX;
        food.y = newY;
    }


    public void move() {
        //eat food
        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(food.x, food.y)); // Add new part at food's old location
            score++; // Increment score
            placeFood(); // Place new food

            // Increase speed (decrease delay)
            int currentDelay = gameLoop.getDelay();
            if (currentDelay > minDelay) {
                 gameLoop.setDelay(Math.max(minDelay, currentDelay - speedIncrement));
            }

        }

        //move snake body (from tail to head)
        for (int i = snakeBody.size() - 1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) { // Part closest to the head
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            } else {
                Tile prevSnakePart = snakeBody.get(i - 1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }

        //move snake head
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        //game over conditions
        // Check self-collision
        for (Tile snakePart : snakeBody) {
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
            }
        }

        // Check wall collision
        if (snakeHead.x < 0 || snakeHead.x >= boardWidth / tileSize || // Check against grid boundaries
            snakeHead.y < 0 || snakeHead.y >= boardHeight / tileSize) {
            gameOver = true;
        }

        // Handle game over state changes
        if (gameOver) {
             if(score > highScore) {
                 highScore = score; // Update high score if current score is higher
             }
             gameLoop.stop();
        }
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) { // Called by gameLoop timer
        if (!isPaused && !gameOver) { // Only move if not paused and not game over
             move();
        }
        repaint(); // Repaint happens regardless to show pause/game over messages
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // Restart Game
        if (gameOver && keyCode == KeyEvent.VK_R) {
            initializeGame(); // Reset the game state
            gameLoop.restart(); // Restart the timer
            return; // Don't process other keys if restarting
        }

        // Pause/Resume Game
        if (keyCode == KeyEvent.VK_P) {
            isPaused = !isPaused; // Toggle pause state
            if (isPaused) {
                gameLoop.stop();
            } else if (!gameOver) { // Only resume if not game over
                gameLoop.start();
            }
            repaint(); // Request repaint to show/hide pause message
            return; // Don't process movement keys if pausing/resuming
        }

        // Prevent snake from moving if paused or game over
        if (isPaused || gameOver) {
            return;
        }


        // Movement Controls (preventing 180-degree turns)
        if (keyCode == KeyEvent.VK_UP && velocityY != 1) { // Prevent moving down if currently moving up
            velocityX = 0;
            velocityY = -1;
        } else if (keyCode == KeyEvent.VK_DOWN && velocityY != -1) { // Prevent moving up if currently moving down
            velocityX = 0;
            velocityY = 1;
        } else if (keyCode == KeyEvent.VK_LEFT && velocityX != 1) { // Prevent moving right if currently moving left
            velocityX = -1;
            velocityY = 0;
        } else if (keyCode == KeyEvent.VK_RIGHT && velocityX != -1) { // Prevent moving left if currently moving right
            velocityX = 1;
            velocityY = 0;
        }
    }

    // Not needed for this game
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    // Main method to run the game (if you don't have a separate App class)
    public static void main(String[] args) {
         int boardWidth = 600;
         int boardHeight = 600;

         JFrame frame = new JFrame("Snake Game");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setResizable(false);

         SnakeGame snakeGame = new SnakeGame(boardWidth, boardHeight);
         frame.add(snakeGame);
         frame.pack(); // Sizes the frame based on the panel's preferred size
         frame.setLocationRelativeTo(null); // Center the window
         frame.setVisible(true);

         // Request focus for the panel so KeyListener works immediately
         snakeGame.requestFocusInWindow();
    }
}
