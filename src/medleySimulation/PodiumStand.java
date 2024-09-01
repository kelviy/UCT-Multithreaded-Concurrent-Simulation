package medleySimulation;

import javax.swing.*;
import java.awt.*;

public class PodiumStand {

    private int frameX;
    private int frameY;

    private int[] teamWinners;

    public static void main(String[] args) {
        int[] winningTeams = {1, 2, 3};
        PodiumStand podiumStand = new PodiumStand(600, 500, winningTeams);
    }

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
    }

    protected class PaintPodium extends JPanel implements Runnable{
        private double rotation;
        private int direction;

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

        public double addRotation(double original, double rotation) {
            original += rotation;
            if (original > Math.PI*2) {
                original -= Math.PI*2;
                return original;
            }
            return original;
        }

        public void restrictRotation(double rotation) {
            this.rotation += rotation * direction;
            if (this.rotation > Math.PI/2 || this.rotation < 0) {
                direction *= -1;
            }
        }

        public double inverseLerp(double start, double end, double value) {
            return (double) (value - start) / (end - start);
        }

        // Lerp function based on rotation angle instead of time
        public int angleLerp(int start, int end) {
            return (int) Math.round(start + (end - start) * inverseLerp(0, Math.PI/2, this.rotation));
        }

        public void branch(Graphics2D g, int x, int y, double length, double rotation, int strokeWidth) {
            int b1x = getX(x, length, rotation);
            int b1y = getY(y, length, rotation);
            g.setStroke(new BasicStroke(strokeWidth));

            g.drawLine(x, y, b1x, b1y);
            length *= 0.67;
            strokeWidth *= 0.90;

            if (length > 3) {
                double rotation1 = rotation;
                rotation = addRotation(rotation, this.rotation);
                branch(g, b1x, b1y, length, rotation, strokeWidth);

                rotation = rotation1;
                rotation = addRotation(rotation, -this.rotation);

                branch(g, b1x, b1y, length, rotation, strokeWidth);
            }
        }

        public void drawCircles(Graphics2D g, int x, int y, int r, double rotation) {
            g.drawOval(x-r, y-r, r*2, r*2);

//            if (r > 4) {
//                r *= 0.75;
//                drawCircles(g, x, y, r);
//            }
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

            g2d.setFont(new Font("TimesRoman", Font.BOLD, 24));
            FontMetrics fm = g2d.getFontMetrics();
            String title = " ヾ(⌐■_■)/  Congratulations to the WINNERS! ヾ(⌐■_■)/";
            g2d.drawString(title, frameX/2 - fm.stringWidth(title)/2, 50);
            g2d.drawLine(150, 70, frameX - 150, 70);

            g2d.setFont(new Font("TimesRoman", Font.BOLD, 16));

            int yCircle = angleLerp(130, 200);
            int xCircle = angleLerp(150, 100);
            drawCircles(g2d, xCircle, yCircle, 50, this.rotation);
            drawCircles(g2d, 600-xCircle, yCircle, 50, this.rotation);

            int x = frameX / 2;
            int y = 270;
            branch(g2d, x, y, 50, 0, 4);
            int yString = angleLerp(y-170, y-100);
            g2d.drawString("1st: (Team " + teamWinners[0] + ")", x - 45, yString);

            x =  180;
            y = 400;
            branch(g2d, x, y, 50, 0, 4);
            yString = angleLerp(230, 300);
            g2d.drawString("2nd: (Team " + teamWinners[1] + ")", x - 45, yString);

            x = 420;
            y = 400;
            branch(g2d, x, y, 50, 0, 4);
            g2d.drawString("3rd: (Team " + teamWinners[2] + ")", x - 45, yString);

            restrictRotation(0.001);

        }

        public void run() {
            while (true) {
                repaint();
            }
        }
    }
}
