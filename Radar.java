import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Radar {
    private String detectionTargetType = "None";
    private double radarStrength = 30;
    private double radarFOV = Math.toRadians(5);
    private double radarRange;
    private double radarAngleMax = Math.toRadians(50);
    private double angle;
    private double currentAngle = 0;
    private double radarSweepSpeed;
    private String radarMode = "SWEEP";
    private String team;
    private boolean IFF;
    private double x;
    private double y;
    private boolean sweepingRight = true;
    private String id;

    private final ReflectorManager reflectorManager;
    private RWRManager rwrManager;

    public Radar(String id, String team, String radarMode, boolean IFF, ReflectorManager reflectorManager, double x,
            double y, double angle, RWRManager rwrManager, double radarRange, double radarSweepSpeed) {
        this.team = team;
        this.IFF = IFF;
        this.reflectorManager = reflectorManager;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.radarMode = radarMode;
        this.id = id;
        this.rwrManager = rwrManager;
        this.radarRange = radarRange;
        this.radarSweepSpeed = radarSweepSpeed;
    }

    public void update(String radarMode, double x, double y) {
        this.x = x;
        this.y = y;
        if (radarMode.equals("SWEEP")) {
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
        } else if (radarMode.equals("CLOCKWISE")) {
            currentAngle = MathUtils.normalizeAngle(currentAngle + radarSweepSpeed, 0);
        } else if (radarMode.equals("Track") || radarMode.equals("Launch")) {
            XYCoordinate targetXYCoordinate = getStrongestReflectXYCoordinate(x, y);
            if (targetXYCoordinate.x != 0) {
                double dx = targetXYCoordinate.x - this.x;
                double dy = targetXYCoordinate.y - this.y;
                currentAngle = MathUtils.normalizeAngle(angle, Math.atan2(dy, dx));
            }
        }
    }

    public List<DetectedReflector> scanForReflectors() {
        List<DetectedReflector> detectedReflectors = new ArrayList<>();
        List<Reflector> reflectors = reflectorManager.getReflectors();

        for (Reflector reflector : reflectors) {
            double dx = reflector.getX() - this.x;
            double dy = reflector.getY() - this.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double angleToReflector = Math.atan2(dy, dx);

            // レーダーの視野角内にあるかどうかを判定
            if (distance <= radarRange
                    && Math.abs(MathUtils.normalizeAngle(angleToReflector,
                            MathUtils.normalizeAngle(angle, this.currentAngle))) <= radarFOV / 2) {
                this.detectionTargetType = reflector.getSourceType();
                detectedReflectors.add(
                        new DetectedReflector(reflector.getX(), reflector.getY(), reflector.getReflectanceStrength()));
                if ("Player".equals(reflector.getSourceType())) {
                    if (rwrManager != null) {
                        SoundPlayer.playRWRSound(0, radarMode);
                        rwrManager.setReceiverStrength(radarStrength);
                        rwrManager.updateOrAddRWRInfo(this);
                    }
                }
            }
        }

        List<RWRInfo> rwrInfos = new CopyOnWriteArrayList<>(rwrManager.getRWRInfos());
        for (RWRInfo rwrInfo : rwrInfos) {
            double rwrDx = rwrInfo.getX() - this.x;
            double rwrDy = rwrInfo.getY() - this.y;
            double rwrDistance = Math.sqrt(rwrDx * rwrDx + rwrDy * rwrDy);
            double rwrAngleToReflector = Math.atan2(rwrDy, rwrDx);

            if (rwrDistance > radarRange
                    || Math.abs(MathUtils.normalizeAngle(rwrAngleToReflector,
                            MathUtils.normalizeAngle(angle, this.currentAngle))) > radarFOV / 2) {
                if (rwrInfo.getID().equals(this.id)) {
                    rwrManager.removeRWRInfo(rwrInfo);
                }
            }
        }

        return detectedReflectors;
    }

    public XYCoordinate getStrongestReflectXYCoordinate(double x, double y) {
        List<DetectedReflector> detectedReflectors = scanForReflectors();
        double maxStrength = 0;
        XYCoordinate strongestXYCoordinate = new XYCoordinate(0, 0);

        for (DetectedReflector reflector : detectedReflectors) {
            double dx = reflector.getX() - x;
            double dy = reflector.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double strength = reflector.getReflectanceStrength() / distance;

            if (strength > maxStrength) {
                maxStrength = strength;
                strongestXYCoordinate = new XYCoordinate(reflector.getX(), reflector.getY());
            }
        }

        return strongestXYCoordinate;
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

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void setRWR(RWRManager rwr) {
        this.rwrManager = rwr;
    }

    public double getRange() {
        return radarRange;
    }

    public String getDetectionTargetType() {
        return detectionTargetType;
    }

    public String getDetectionRadarMode() {
        return radarMode;
    }

    public String getRadarID() {
        return id;
    }

    public String getRadarMode() {
        return radarMode;
    }

    public String getDetectedTargetType() {
        return detectionTargetType;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}