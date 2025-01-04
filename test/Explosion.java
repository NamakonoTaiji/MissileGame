package test;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random; // Random クラスをインポート

public class Explosion {
    private double x, y;
    private int radius;
    private int maxRadius;
    private Color color;
    private boolean finished;
    private List<Particle> particles;

    public Explosion(double x, double y, int maxRadius, Color color) {
        this.x = x;
        this.y = y;
        this.radius = 0;
        this.maxRadius = maxRadius;
        this.color = color;
        this.finished = false;
        this.particles = new ArrayList<>();
        int particleSize = maxRadius / 5;
        for (int i = 0; i < 10; i++) {
            particles.add(new Particle(x, y, 1.0 + new Random().nextDouble() * 2.0, 50, Color.GRAY, particleSize));
        }
    }

    public void update() {
        if (radius < maxRadius) {
            radius += 2; // 爆発の成長速度
        } else {
            finished = true;
        }
        for (Particle particle : particles) {
            particle.update();
        }
        particles.removeIf(particle -> !particle.isAlive());
    }

    public void draw(Graphics2D g) {
        if (!finished) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 128)); // 爆心地の円を半透明に
            g.fillOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
        }
        for (Particle particle : particles) {
            particle.draw(g);
        }
    }

    public boolean isFinished() {
        return finished && particles.isEmpty();
    }
}
