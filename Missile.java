import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

public class Missile {

    private double x;
    private double y;
    private double speed;
    private double angle;
    private double oldAngle;
    private double angleDifference;
    private EmitterManager emitterManager;

    private double playerIRSensitivity = 0.5;
    private double missileMaxTurnRate = 0.0031;
    private int burnTimeOfBooster = 1200;
    private double deltaVOfBooster = 0.0037;
    private double airResistance = 0.9995;
    private double IRCCMSeekerFOV = Math.toRadians(5);
    private double normalSeekerFOV = Math.toRadians(5);
    private double seekerFOV;
    private double seekerAngle;
    private final int LIFESPAN = 3700;

    private int age = 0;
    private double targetX = 0;
    private double targetY = 0;
    private double targetAngle = 0;

    private String navigationMode;

    private final int TRAIL_LENGTH = 200; // 軌跡の長さ
    private Queue<Point> trail; // 軌跡を保存するキュー
    private Player player;

    public Missile(double x, double y, double speed, double angle, String navigationMode,
            EmitterManager emitterManager, Player player) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.speed = speed;
        this.angle = angle;
        this.targetAngle = angle;
        this.navigationMode = navigationMode;
        this.seekerAngle = angle;
        this.oldAngle = angle;
        this.emitterManager = emitterManager;
        this.trail = new LinkedList<>();
        this.player = player;
        this.seekerFOV = normalSeekerFOV;
    }

    public void update() {
        List<Emitter> emitters = emitterManager.getEmitters();
        double weightedSumX = 0;
        double weightedSumY = 0;
        double totalWeight = 0;

        for (Emitter emitter : emitters) {
            double emitterX = emitter.getX();
            double emitterY = emitter.getY();
            double emitterDistance = Math.sqrt((Math.pow(emitterX - x, 2) + Math.pow(emitterY - y, 2)));
            double emitterLOSAngle = Math.atan2(emitterY - y, emitterX - x); // ミサイルを起点とする視線角度に修正
            double angleDifferenceToEmitter = MathUtils.normalizeAngle(seekerAngle, emitterLOSAngle); // 首振り角 - 熱源LOS角
            double infraredEmission = emitter.getInfraredEmission(); // 赤外線強度
            String sourceType = emitter.getSourceType();

            if (sourceType.equals("Player")) {
                double playerAngle = player.getAngle();
                infraredEmission /= emitterDistance * 0.5; // 距離が遠いほど熱源が小さく見える
                infraredEmission *= playerIRSensitivity; // シーカーのプレイヤーとフレアの識別性能を実装
                infraredEmission /= MathUtils.clamp(Math.abs(MathUtils.normalizeAngle(seekerAngle, playerAngle)), 0.4,
                        1.5) / 1.5; // 後方排気を捉えると強く熱源を認識する
            } else if (sourceType.equals("Flare")) {
                infraredEmission /= emitterDistance * 0.5;
            }
            if (Math.abs(angleDifferenceToEmitter) <= seekerFOV / 2) {
                // より大きい熱源に吸われる
                weightedSumX += emitterX * infraredEmission;
                weightedSumY += emitterY * infraredEmission;
                totalWeight += infraredEmission;
            }
        }

        // 視界に熱源がある場合の処理
        if (totalWeight > 0) {
            targetX = weightedSumX / totalWeight;
            targetY = weightedSumY / totalWeight;
            double deltaX = targetX - x;
            double deltaY = targetY - y;
            targetAngle = Math.atan2(deltaY, deltaX);
            seekerAngle = targetAngle;
            seekerFOV = IRCCMSeekerFOV; // 視界に熱源がある場合はIRCCMの視野角を適応
        } else {
            seekerFOV = normalSeekerFOV; // ない場合は起動時視野角
        }

        // 角度の差を計算し、正規化
        switch (navigationMode) {
            case "PPN": {
                // 単追尾(PPN)
                angleDifference = MathUtils.normalizeAngle(targetAngle, angle);
                break;
            }
            case "PN": {
                // 比例航法(PN)
                angleDifference = MathUtils.normalizeAngle(targetAngle, oldAngle);
                angleDifference = angleDifference * 3;
                oldAngle = targetAngle;
                break;
            }
            case "MPN": {
                // 修正比例航法(MPN)
                angleDifference = MathUtils.normalizeAngle(targetAngle, oldAngle);
                angleDifference = angleDifference * 3 + MathUtils.normalizeAngle(targetAngle, angle) * 0.004;
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
        }
        speed *= (1 - Math.abs(angleDifference) * 0.4); // 旋回による運動エネルギーの消費
        // 空気抵抗による減速
        speed = speed * airResistance;

        // 速度に基づいて移動
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;

        // 軌跡を更新
        if (trail.size() >= TRAIL_LENGTH) {
            trail.poll(); // 古いポイントを削除
        }
        if (age <= burnTimeOfBooster && age % 5 == 0) {
            trail.add(new Point((int) x, (int) y));
        }
        // ミサイルの寿命を更新
        age += 1;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // 軌跡を描画
        g2d.setColor(new Color(255, 255, 255, 90));
        for (Point point : trail) {
            g2d.fillOval(point.x - 2, point.y - 2, 4, 4);
        }
        // ミサイル自体を描画
        g2d.setColor(Color.BLUE);
        g2d.drawOval((int) (x - 1.5), (int) (y - 1.5), 3, 3);
        g.drawLine((int) x, (int) y, (int) ((x) + 5 * Math.cos(angle)),
                (int) ((y) + 5 * Math.sin(angle)));

        // シーカーの視点
        g2d.setColor(Color.BLACK);
        g2d.drawOval((int) targetX - 3, (int) targetY - 3, 6, 6);

        // 視野角の範囲を描画（半透明の扇形）
        double fovLeft = MathUtils.normalizeAngle(seekerAngle, -seekerFOV / 2);
        double fovRight = MathUtils.normalizeAngle(seekerAngle, seekerFOV / 2);
        g2d.setColor(new Color(255, 0, 0, 10));
        g2d.fillPolygon(
                new int[] { (int) x, (int) (x + Math.cos(fovLeft) * 8000),
                        (int) (x + Math.cos(fovRight) * 8000) },
                new int[] { (int) y, (int) (y + Math.sin(fovLeft) * 8000), (int) (y + Math.sin(fovRight) * 8000) },
                3);
        // g2d.fillArc((int) (x - 1500), (int) (y - 1500), 3000, 3000, (int) arcStart,
        // (int) arcExtent);
    }

    public boolean isExpired() {
        return age >= LIFESPAN;
    }

    // ゲッターメソッド
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
