import java.awt.*;

public class Chaff implements Reflector {
    private static double REFLECTOR_STRENGTH = 1.0;
    private double airResistance = 0.991;
    private double x;
    private double y;
    private double speed;
    private double angle;
    private int age = 0;
    private final double CHAFF_SIZE = 2;
    private final double LIFESPAN = 6000;

    public Chaff(double x, double y, double speed, double angle) {
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
        g2d.setColor(new Color(177, 177, 177, 228)); // 半透明の灰色
        g2d.fillOval((int) (x - CHAFF_SIZE / 2), (int) (y - CHAFF_SIZE / 2), (int) CHAFF_SIZE,
                (int) CHAFF_SIZE);

        // 周囲の光の拡散を描画
        g2d.setColor(new Color(177, 177, 177, 124)); // より薄い半透明の灰色
        g2d.fillOval((int) (x - CHAFF_SIZE * 1.25), (int) (y - CHAFF_SIZE * 1.25),
                (int) (CHAFF_SIZE * 2.5),
                (int) (CHAFF_SIZE * 2.5));
    }

    public boolean isExpired() {
        return age >= LIFESPAN;
    }

    @Override
    public String getSourceType() {
        return "Chaff";
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
    public double getReflectanceStrength() {
        return REFLECTOR_STRENGTH;
    }

    @Override
    public String getReflectorType() {
        return "Chaff";
    }
}
