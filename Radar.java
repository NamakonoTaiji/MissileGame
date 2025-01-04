import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;

public class Radar {
    private double radarStrength = 30;
    private double radarFOV = Math.toRadians(5);
    private double radarRange = 5000;
    private double radarAngleMax = Math.toRadians(50);
    private double angle;
    private double currentAngle = 0;
    private double radarSweepSpeed = 0.007;
    private String radarMode = "SRC";
    private String team;
    private boolean IFF;
    private double x;
    private double y;
    private boolean sweepingRight = true;

    private final ReflectorManager reflectorManager;

    public Radar(String team, String radarMode, boolean IFF, ReflectorManager reflectorManager, double x,
            double y, double angle) {
        this.team = team;
        this.IFF = IFF;
        this.reflectorManager = reflectorManager;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.radarMode = radarMode;
    }

    public void update() {

        if (radarMode.equals("SRC")) {
            if (sweepingRight) {
                currentAngle += radarSweepSpeed;
                if (currentAngle >= radarAngleMax) {
                    sweepingRight = false;
                }
            } else {
                currentAngle -= radarSweepSpeed;
                if (currentAngle <= -radarAngleMax) {
                    sweepingRight = true;
                }
            }
        }
    }

    public List<Point> scanForReflectors() {
        List<Point> detectedReflectors = new ArrayList<>();
        List<Reflector> reflectors = reflectorManager.getReflectors();

        for (Reflector reflector : reflectors) {
            if (!reflector.getReflectorType().equals("Player")) {

                double dx = reflector.getX() - this.x;
                double dy = reflector.getY() - this.y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                double angleToReflector = Math.atan2(dy, dx);

                if (distance <= radarRange
                        && Math.abs(MathUtils.normalizeAngle(angleToReflector,
                                MathUtils.normalizeAngle(angle, this.currentAngle))) <= radarFOV / 2) {
                    detectedReflectors.add(new Point((int) reflector.getX(), (int) reflector.getY()));
                }
            }
        }

        return detectedReflectors;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // レーダーの視野角を描画
        double fovLeft = MathUtils.normalizeAngle(angle - currentAngle, -radarFOV / 2);
        double fovRight = MathUtils.normalizeAngle(angle - currentAngle, radarFOV / 2);
        g2d.setColor(new Color(0, 255, 0, 10)); // 半透明の緑色
        g2d.fillPolygon(
                new int[] { (int) x, (int) (x + Math.cos(fovLeft) * radarRange),
                        (int) (x + Math.cos(fovRight) * radarRange) },
                new int[] { (int) y, (int) (y + Math.sin(fovLeft) * radarRange),
                        (int) (y + Math.sin(fovRight) * radarRange) },
                3);
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}