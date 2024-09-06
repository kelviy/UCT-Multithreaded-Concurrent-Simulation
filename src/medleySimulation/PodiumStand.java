package medleySimulation;

import javax.swing.*;
import java.awt.*;

import static java.lang.Thread.sleep;

public class PodiumStand {

    // Size
    private int frameX;
    private int frameY;

    // Stores winner information to display passed by constructor
    private int[] teamWinners;

    // Game loop variables
    private final int FRAMES_PER_SECOND = 30;
    private final int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
    private long nextGameTick;
    private long sleepTime = 0;

    // Test animation independent of main simulation
    public static void main(String[] args) {
        int[] winningTeams = {1, 2, 3};
        PodiumStand podiumStand = new PodiumStand(600, 500, winningTeams);
    }

    // Constructor
    public PodiumStand(int frameX, int frameY, int[] teamWinners) {
        this.frameX = frameX;
        this.frameY = frameY;
        JFrame frame = new JFrame("Podium Stand");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(frameX, frameY);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        PaintPodium paintPodium = new PaintPodium();
        frame.add(paintPodium);
        Thread thread = new Thread(paintPodium);
        thread.start();
        frame.setVisible(true);
        this.teamWinners = teamWinners;
        nextGameTick = System.currentTimeMillis();
    }

    // Inner class that handles all the drawing of graphics
    protected class PaintPodium extends JPanel implements Runnable{
        // objects are drawing according to angle
        private double rotation;
        // used by updateRotation to determine which direction to modify rotation
        private int direction;

        // Constructor
        public PaintPodium() {
            super();
            this.rotation = Math.PI/6;
            direction = 1;
        }

        public int getX(int x, double length, double rotation) {
            return (int) (Math.sin(rotation) * length) + x;
        }

        public int getY(int y, double length, double rotation) {
            return -1 * (int) (Math.cos(rotation) * length) + y;
        }

        // restricts rotation to pi/2 and -pi/2
        public double addRotation(double original, double rotation) {
            original += rotation;
            if (original > Math.PI*2) {
                original -= Math.PI*2;
                return original;
            }
            return original;
        }

        // called after every frame to update animation values
        public void updateRotation(double rotation) {
            this.rotation += rotation * direction;
            if (this.rotation > Math.PI/2 || this.rotation < 0) {
                direction *= -1;
            }
        }

        // helper function to lerp
        public double inverseLerp(double start, double end, double value) {
            return (double) (value - start) / (end - start);
        }

        // Lerp function based on rotation angle instead of time
        public int angleLerp(int start, int end) {
            return (int) (start + (end - start) * inverseLerp(0, Math.PI/2, this.rotation));
        }

        // fractal recursive function to display branches (trees)
        public void branch(Graphics2D g, int x, int y, double length, double rotation, int strokeWidth) {
            int b1x = getX(x, length, rotation);
            int b1y = getY(y, length, rotation);
            g.setStroke(new BasicStroke(strokeWidth));

            g.drawLine(x, y, b1x, b1y);
            length *= 0.67;
            strokeWidth *= 0.90;

            if (length > 1) {
                double rotation1 = rotation;
                rotation = addRotation(rotation, this.rotation);
                branch(g, b1x, b1y, length, rotation, strokeWidth);

                rotation = rotation1;
                rotation = addRotation(rotation, -this.rotation);

                branch(g, b1x, b1y, length, rotation, strokeWidth);
            }
        }

        //fractal recursive function to display circles
        public void drawCircles(Graphics2D g, int x, int y, int r, double rotation) {
            g.drawOval(x-r, y-r, r*2, r*2);

            if (r > 16) {
                drawCircles(g, getX(x, r / 2, rotation), y, r / 2, rotation);
                drawCircles(g, getX(x, - r / 2, rotation), y, r / 2, rotation);
                drawCircles(g, x, getY(y, r/2, rotation), r / 2, rotation);
                drawCircles(g, x, getY(y, -r/2, rotation), r / 2, rotation);
            }

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();
            g.fillRect(0, 0, frameX, frameY);
            g2d.setColor(Color.WHITE);

            // Display Heading
            g2d.setFont(new Font("TimesRoman", Font.BOLD, 24));
            FontMetrics fm = g2d.getFontMetrics();
            String title = " ヾ(⌐■_■)/  Congratulations to the WINNERS! ヾ(⌐■_■)/";
            g2d.drawString(title, frameX/2 - fm.stringWidth(title)/2, 50);
            g2d.drawLine(150, 70, frameX - 150, 70);

            g2d.setFont(new Font("TimesRoman", Font.BOLD, 16));

            // Display moving circles
            int yCircle = angleLerp(130, 200);
            int xCircle = angleLerp(150, 100);
            drawCircles(g2d, xCircle, yCircle, 50, this.rotation);
            drawCircles(g2d, 600-xCircle, yCircle, 50, this.rotation);

            // Display first place
            int x = frameX / 2;
            int y = 270;
            branch(g2d, x, y, 50, 0, 4);
            int yString = angleLerp(y-170, y-100);
            g2d.drawString("1st: (Team " + teamWinners[0] + ")", x - 45, yString);

            // Display second place
            x =  180;
            y = 400;
            branch(g2d, x, y, 50, 0, 4);
            yString = angleLerp(230, 300);
            g2d.drawString("2nd: (Team " + teamWinners[1] + ")", x - 45, yString);

            // Display third place
            x = 420;
            branch(g2d, x, y, 50, 0, 4);
            g2d.drawString("3rd: (Team " + teamWinners[2] + ")", x - 45, yString);

            // update rotation (value determines how fast the animations move)
            updateRotation(0.05);

            // Game loop implementation (Constant frame rate of 30fps)
            nextGameTick += SKIP_TICKS;
            sleepTime = nextGameTick - System.currentTimeMillis();
            if( sleepTime >= 0 ) {
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void run() {
            while (true) {
                repaint();
            }
        }
    }
}
