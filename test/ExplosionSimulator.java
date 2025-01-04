package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ExplosionSimulator extends JPanel implements ActionListener {
    private List<Explosion> explosions;
    private Timer timer;

    public ExplosionSimulator() {
        explosions = new ArrayList<>();
        timer = new Timer(20, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        for (Explosion explosion : explosions) {
            explosion.draw(g2d);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Explosion explosion : explosions) {
            explosion.update();
        }
        explosions.removeIf(Explosion::isFinished);
        repaint();
    }

    public void addExplosion(double x, double y) {
        explosions.add(new Explosion(x, y, 50, Color.ORANGE));
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Explosion Simulator");
        ExplosionSimulator simulator = new ExplosionSimulator();
        frame.add(simulator);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // 爆発を追加
        simulator.addExplosion(400, 300);
    }
}
