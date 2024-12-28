import java.awt.*;

public class Flare implements Emitter {
    private double infraredEmission = 1.0;
    private double airResistance = 0.95;
    private double x;
    private double y;
    private double speed;
    private double angle;
    private int age = 0;
    private final double LIFESPAN = 1500;

    public Flare(double x, double y, double speed, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed;
    }

    public void update() {
        // 空気抵抗による減速
        speed = speed * airResistance;

        // 速度に基づいて移動
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;

        age += 1;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.YELLOW);
        g2d.drawOval((int) (x), (int) (y), 3, 3);
    }

    public boolean isExpired() {
        return age >= LIFESPAN;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getInfraredEmission() {
        return infraredEmission;
    }
}
