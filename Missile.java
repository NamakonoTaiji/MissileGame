import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

public class Missile {
    private final double PI = Math.PI;

    private double x;
    private double y;
    private double speed;
    private double angle;
    private double oldAngle;
    private double angleDifference;

    private double missileMaxTurnRate = 0.01;
    private int burnTimeOfBooster = 200;
    private double deltaVOfBooster = 0.025;
    private double airResistance = 0.997;
    private double seekerFOV = Math.toRadians(5);
    private double seekerFOVAngle;
    private int lifeSpan = 800;
    private int age = 0;

    private String navigationMode;

    private final int TRAIL_LENGTH = 150; // 軌跡の長さ
    private Queue<Point> trail; // 軌跡を保存するキュー

    public Missile(double x, double y, double speed, double angle, String navigationMode) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.angle = angle;
        this.seekerFOVAngle = angle;
        this.navigationMode = navigationMode;
        this.oldAngle = angle;
        this.trail = new LinkedList<>();
    }

    public void update(double targetX, double targetY) {
        // 目標（自機）への角度を計算
        double deltaX = targetX - x;
        double deltaY = targetY - y;
        double targetAngle = Math.atan2(deltaY, deltaX);

        // 角度の差を計算し、正規化
        switch (navigationMode) {
            case "PPN": {
                // 単追尾(PPN)
                angleDifference = (targetAngle - angle + PI * 3) % (PI * 2) - PI;
            }
            case "PN": {
                // 比例航法(PN)
                angleDifference = (targetAngle - oldAngle + PI * 3) % (PI * 2) - PI;
                angleDifference = angleDifference * 3;
                oldAngle = targetAngle;
            }
            case "MPN": {
                // 修正比例航法(MPN)
                angleDifference = (targetAngle - oldAngle + PI * 3) % (PI * 2) - PI;
                angleDifference = angleDifference * 3 + ((targetAngle - angle + PI * 3) % (PI * 2) - PI) * 0.02;
                oldAngle = targetAngle;
            }

        }
        // 角度の差をクランプ
        angleDifference = MathUtils.clamp(angleDifference, -missileMaxTurnRate, missileMaxTurnRate);
        angle += angleDifference;

        // ブースター燃焼中は加速
        if (age <= burnTimeOfBooster) {
            speed += deltaVOfBooster;
            speed *= (1 - Math.abs(angleDifference)); // 旋回による減速
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
    }

    // ゲッターメソッド
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
