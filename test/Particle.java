package test;

import java.awt.*;
import java.util.Random;

public class Particle {
    private double x, y;
    private double vx, vy;
    private int life;
    private Color color;
    private float alpha;
    private int size;

    public Particle(double x, double y, double speed, int life, Color color, int size) {
        this.x = x;
        this.y = y;
        Random random = new Random();
        double angle = random.nextDouble() * 2 * Math.PI;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.life = life;
        this.color = color;
        this.alpha = 1.0f;
        this.size = size;
    }

    public void update() {
        x += vx;
        y += vy;
        life--;
        alpha = Math.max(0, alpha - 0.02f); // 徐々に透明にする
    }

    public void draw(Graphics2D g) {
        if (life > 0) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
            g.fillOval((int) x, (int) y, size, size); // 塵の大きさを指定したサイズにする
        }
    }

    public boolean isAlive() {
        return life > 0;
    }
}
