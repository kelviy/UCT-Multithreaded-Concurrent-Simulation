package medleySimulation;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Timer implements Runnable {
    private JLabel time;
    private long startTime;
    private long elapsedTime;
    private boolean record;

    public Timer(JLabel time) {
        elapsedTime = 0;
        record = false;
        startTime = 0;
        this.time = time;
        this.time.setFont(new Font("Arial", Font.PLAIN, 15));
    }

    public String getTime() {
        if (record) {
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void startRecord() {
        this.record = true;
        startTime = System.currentTimeMillis();
    }

    public void stopRecord() {
        this.record = false;
    }

    public void run() {
        while (true) {
            time.setText("Time: " + getTime());
            if (record) {
                time.setForeground(Color.GREEN);
            } else {
                time.setForeground(Color.RED);
            }
        }
    }
}
