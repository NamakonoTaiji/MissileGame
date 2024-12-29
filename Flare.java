import java.awt.*;

public class Flare implements Emitter {
    private double infraredEmission = 1.0;
    private double airResistance = 0.95;
    private double x;
    private double y;
    private double speed;
    private double angle;
    private int age = 0;
    private final double FLARE_EFFECT_SIZE = 2;
    private final double LIFESPAN = 1000;

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

    public void draw(Graphics g2d) {
        g2d.setColor(new Color(255, 255, 0, 128)); // 半透明の黄色
        g2d.fillOval((int) (x - FLARE_EFFECT_SIZE / 2), (int) (y - FLARE_EFFECT_SIZE / 2), (int) FLARE_EFFECT_SIZE,
                (int) FLARE_EFFECT_SIZE);

        // 周囲の光の拡散を描画
        g2d.setColor(new Color(255, 255, 0, 64)); // より薄い半透明の黄色
        g2d.fillOval((int) (x - FLARE_EFFECT_SIZE * 1.25), (int) (y - FLARE_EFFECT_SIZE * 1.25),
                (int) (FLARE_EFFECT_SIZE * 2.5),
                (int) (FLARE_EFFECT_SIZE * 2.5));

        g2d.setColor(new Color(255, 255, 0, 32)); // さらに薄い半透明の黄色
        g2d.fillOval((int) (x - FLARE_EFFECT_SIZE * 2.7), (int) (y - FLARE_EFFECT_SIZE * 2.7),
                (int) (FLARE_EFFECT_SIZE * 5.4),
                (int) (FLARE_EFFECT_SIZE * 5.4));
    }

    public boolean isExpired() {
        return age >= LIFESPAN;
    }

    @Override
    public String getSourceType() {
        return "Flare";
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
