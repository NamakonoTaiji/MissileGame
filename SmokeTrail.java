import java.awt.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SmokeTrail {
    private static final int MAX_AGE = 3000; // 排煙の最大寿命
    private Queue<SmokeParticle> particles;

    public SmokeTrail() {
        particles = new ConcurrentLinkedQueue<>();
    }

    public void addParticle(double x, double y) {
        particles.add(new SmokeParticle(x, y));
    }

    public void update() {
        for (SmokeParticle particle : particles) {
            particle.update();
        }
        particles.removeIf(SmokeParticle::isExpired);
    }

    public void draw(Graphics g) {
        for (SmokeParticle particle : particles) {
            particle.draw(g);
        }
    }

    private class SmokeParticle {
        private double x;
        private double y;
        private int age;
        private int maxAge;
        private Color color;

        public SmokeParticle(double x, double y) {
            this.x = x;
            this.y = y;
            this.age = 0;
            this.maxAge = MAX_AGE;
            this.color = new Color(200, 200, 200, 255); // 初期の色（半透明の灰色）
        }

        public void update() {
            age++;
            int alpha = (int) (255 * (1.0 - (double) age / maxAge));
            color = new Color(200, 200, 200, alpha); // 時間経過で透明になる
        }

        public void draw(Graphics g) {
            g.setColor(color);
            g.fillOval((int) x - 2, (int) y - 2, 4, 4);
        }

        public boolean isExpired() {
            return age >= maxAge;
        }
    }
}
