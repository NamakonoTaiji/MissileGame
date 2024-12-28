import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

public class Missile {
    private final double PI = Math.PI;

    private double x;
    private double y;
    private double speed;
    private double angle;
    private double oldAngle;
    private double angleDifference;
    private EmitterManager emitterManager;

    private double missileMaxTurnRate = 0.0025;
    private int burnTimeOfBooster = 800;
    private double deltaVOfBooster = 0.0026;
    private double airResistance = 0.9991;
    private double seekerFOV = Math.toRadians(25);
    private double seekerAngle;
    private int lifeSpan = 3000;
    private int age = 0;
    private double targetX = 0;
    private double targetY = 0;
    private double targetAngle = 0;
    private double debugX = 0;
    private double debugY = 0;

    private String navigationMode;

    private final int TRAIL_LENGTH = 200; // 軌跡の長さ
    private Queue<Point> trail; // 軌跡を保存するキュー

    public Missile(double x, double y, double speed, double angle, String navigationMode,
            EmitterManager emitterManager) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.angle = angle;
        this.navigationMode = navigationMode;
        this.seekerAngle = angle;
        this.oldAngle = angle;
        this.emitterManager = emitterManager;
        this.trail = new LinkedList<>();
    }

    public void update() {
        List<Emitter> emitters = emitterManager.getEmitters();
        double sumX = 0;
        double sumY = 0;
        int count = 0;

        for (Emitter emitter : emitters) {
            double emitterX = emitter.getX();
            double emitterY = emitter.getY();
            double emitterLOSAngle = Math.atan2(emitterY, emitterX);
            double angleDifferenceToEmitter = (seekerAngle - emitterLOSAngle + PI * 3) % (PI * 2) - PI;
            boolean isCloseEmitter = Math.sqrt(Math.pow(emitterX - x, 2) + Math
                    .pow(emitterY - y, 2)) < 80;
            boolean isCloseAngle = Math.abs(angleDifferenceToEmitter) <= seekerFOV + Math.toDegrees(10);
            if (Math.abs(angleDifferenceToEmitter) <= seekerFOV || (isCloseEmitter && isCloseAngle)) {
                sumX += emitterX;
                sumY += emitterY;
                count++;
            }
        }

        if (count > 0) {
            targetX = sumX / count;
            targetY = sumY / count;
            double deltaX = targetX - x;
            double deltaY = targetY - y;
            targetAngle = Math.atan2(deltaY, deltaX);
            seekerAngle = targetAngle;
        }

        System.out.println(count);
        debugX = targetX;
        debugY = targetY;
        // 角度の差を計算し、正規化
        switch (navigationMode) {
            case "PPN": {
                // 単追尾(PPN)
                angleDifference = (targetAngle - angle + PI * 3) % (PI * 2) - PI;
                break;
            }
            case "PN": {
                // 比例航法(PN)
                angleDifference = (targetAngle - oldAngle + PI * 3) % (PI * 2) - PI;
                angleDifference = angleDifference * 3;
                oldAngle = targetAngle;
                break;
            }
            case "MPN": {
                // 修正比例航法(MPN)
                angleDifference = (targetAngle - oldAngle + PI * 3) % (PI * 2) - PI;
                angleDifference = angleDifference * 3 + ((targetAngle - angle + PI * 3) % (PI * 2) - PI) * 0.0007;
                oldAngle = targetAngle;
                break;
            }
        }
        // 角度の差をクランプ
        angleDifference = MathUtils.clamp(angleDifference, -missileMaxTurnRate, missileMaxTurnRate);
        angle += angleDifference;

        // ブースター燃焼中は加速
        if (age <= burnTimeOfBooster) {
            speed += deltaVOfBooster;
            speed *= (1 - Math.abs(angleDifference) * 1.4); // 旋回による減速
        }

        // 空気抵抗による減速
        speed = speed * airResistance;

        // 速度に基づいて移動
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;

        // 軌跡を更新
        if (trail.size() >= TRAIL_LENGTH) {
            trail.poll(); // 古いポイントを削除
        }
        if (age <= burnTimeOfBooster) {
            trail.add(new Point((int) x, (int) y));
        }
        // ミサイルの寿命を更新
        age += 1;
    }

    public boolean isExpired() {
        return age >= lifeSpan;
    }

    // ミサイルの描画
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // 軌跡を描画
        g2d.setColor(Color.GRAY);
        for (Point point : trail) {
            g2d.fillOval(point.x - 1, point.y - 1, 2, 2);
        }
        // ミサイル自体を描画
        g2d.setColor(Color.BLUE);
        g2d.drawOval((int) (x - 2.5), (int) (y - 2.5), 5, 5);
        g.drawLine((int) x, (int) y, (int) ((x) + 10 * Math.cos(angle)),
                (int) ((y) + 10 * Math.sin(angle)));

        // シーカーの視点
        g2d.setColor(Color.BLACK);
        g2d.drawOval((int) debugX - 5, (int) debugY - 5, 10, 10);

        // seekerAngleの方向

        g2d.drawLine((int) x, (int) y, (int) (x + 30 * Math.cos(seekerAngle)),
                (int) (y + 30 * Math.sin(seekerAngle)));
        double arcStart = Math.toDegrees(((seekerFOV / 2 - seekerAngle) + PI * 3) % (PI * 2) - PI);
        double arcExtent = Math.toDegrees(-seekerFOV);
        g2d.setColor(new Color(255, 0, 0, 10));
        g2d.fillArc((int) (x - 1000), (int) (y - 1000), 2000, 2000, (int) arcStart, (int) arcExtent);
    }

    // ゲッターメソッド
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
