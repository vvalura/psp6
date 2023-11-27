package org.example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class DemoThread extends JFrame {
    private BufferedImage backgroundImage;
    private BufferedImage ballImage;
    private BufferedImage bumImage;
    private int[] ballY;
    private boolean[] stopRequested;
    private Thread[] ballThreads;
    private boolean isRunning = true;

    public DemoThread() {
        setPreferredSize(new Dimension(800, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        centerWindowOnScreen();
        setVisible(true);
        loadImages();
        initializeBallPositions();
        stopRequested = new boolean[5];
        ballThreads = new Thread[5];

        JButton startButton = new JButton("Старт");

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isRunning) {
                    resetBallPositions();
                    startThreads();
                } else {
                    startThreads();
                    isRunning = true;
                }
            }
        });

        JButton stopButton = new JButton("Стоп");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopThreads();
                explodeBalls();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        add(buttonPanel, BorderLayout.NORTH);

        int ballSpacing = 20; // Расстояние между шариками
        int totalBallWidth = ballImage.getWidth() * 5 + ballSpacing * 4; // Общая ширина шариков и промежутков
        int startX = (getWidth() - totalBallWidth) / 2;

        JPanel ballPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) { //отрисовка фона и шариков
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, null);
                for (int i = 0; i < 5; i++) {
                    if (stopRequested[i]) {
                        g.drawImage(bumImage, startX + i * (ballImage.getWidth() + ballSpacing), ballY[i], null);
                    } else {
                        g.drawImage(ballImage, startX + i * (ballImage.getWidth() + ballSpacing), ballY[i], null);
                    }
                }
            }
        };
        add(ballPanel, BorderLayout.CENTER);
    }

    private class BallThread implements Runnable {
        private int ballIndex;
        private Random random;

        public BallThread(int ballIndex) {
            this.ballIndex = ballIndex;
            this.random = new Random();
        }

        @Override
        public void run() {
            while (!Thread.interrupted() && ballY[ballIndex] >= 60 && !stopRequested[ballIndex]) {
                ballY[ballIndex] -= 1;
                repaint();
                try {
                    int delay = random.nextInt(15) + 3; // Случайная задержка от 3 до 15 миллисекунд
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void loadImages() {
        try {
            backgroundImage = ImageIO.read(new File("src/main/resources/it.png"));
            ballImage = ImageIO.read(new File("src/main/resources/ball1.png"));
            bumImage = ImageIO.read(new File("src/main/resources/bum.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeBallPositions() {
        ballY = new int[]{500, 500, 500, 500, 500};
    }

    public void startThreads() {
        for (int i = 0; i < 5; i++) {
            ballThreads[i] = new Thread(new BallThread(i));
            ballThreads[i].start();
        }
    }

    public void stopThreads() {
        for (int i = 0; i < 5; i++) {
            stopRequested[i] = true;
        }
    }

    private void explodeBalls() {
        for (int i = 0; i < 5; i++) {
            if (stopRequested[i]) {
                ballY[i] -= ballImage.getHeight() / 2; // Смещение взрыва вверх, чтобы он был по центру шарика
            }
        }
        repaint();
    }

    private void centerWindowOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = screenSize.width / 2;
        int centerY = screenSize.height / 2;
        int windowWidth = getWidth();
        int windowHeight = getHeight();
        setLocation(centerX - windowWidth/ 2, centerY - windowHeight / 2);
    }

    private void resetBallPositions() {
        for (int i = 0; i < 5; i++) {
            ballY[i] = 500;
            stopRequested[i] = false;
        }
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DemoThread demo = new DemoThread();
        });
    }
}