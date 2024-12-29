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

    private double playerIRSensitivity = 1.1;
    private double missileMaxTurnRate = 0.003;
    private int burnTimeOfBooster = 800;
    private double deltaVOfBooster = 0.0026;
    private double airResistance = 0.9991;
    private double seekerFOV = Math.toRadians(1);
    private double seekerAngle;
    private final int LIFESPAN = 2000;

    private int age = 0;
    private double targetX = 0;
    private double targetY = 0;
    private double targetAngle = 0;
    private double debugX = 0;
    private double debugY = 0;

    private String navigationMode;

    private final int TRAIL_LENGTH = 200; // 軌跡の長さ
    private Queue<Point> trail; // 軌跡を保存するキュー
    private Player player;

    public Missile(double x, double y, double speed, double angle, String navigationMode,
            EmitterManager emitterManager, Player player) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.angle = angle;
        this.navigationMode = navigationMode;
        this.seekerAngle = angle;
        this.oldAngle = angle;
        this.emitterManager = emitterManager;
        this.trail = new LinkedList<>();
        this.player = player;
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
            double angleDifferenceToEmitter = (seekerAngle - emitterLOSAngle + PI * 3) % (PI * 2) - PI; // 首振り角 - 熱源LOS角
            double infraredEmission = emitter.getInfraredEmission(); // 赤外線強度
            String sourceType = emitter.getSourceType();

            if (sourceType.equals("Player")) {
                double playerAngle = player.getAngle();
                infraredEmission *= 100 / emitterDistance; // 距離が遠いほど熱源が小さく見える
                infraredEmission *= playerIRSensitivity; // シーカーのプレイヤーとフレアの識別性能を実装
                infraredEmission /= MathUtils.clamp(Math.abs((seekerAngle - playerAngle + PI * 3) % (PI * 2) - PI), 0.6,
                        1.8) / 1.5; // 後方排気を捉えると強く熱源を認識する
            } else if (sourceType.equals("Flare")) {
                infraredEmission *= 130 / emitterDistance;
            }
            boolean isCloseEmitter = Math.sqrt(Math.pow(emitterX - x, 2) + Math.pow(emitterY - y, 2)) < 80; // 熱源に近いかどうか
            boolean isCloseAngle = Math.abs(angleDifferenceToEmitter) <= seekerFOV + Math.toRadians(0); // 熱源に視野角が近いかどうか

            if (Math.abs(angleDifferenceToEmitter) <= seekerFOV || (isCloseEmitter && isCloseAngle)) {
                // より大きい熱源に吸われる
                weightedSumX += emitterX * infraredEmission;
                weightedSumY += emitterY * infraredEmission;
                totalWeight += infraredEmission;
            }
        }

        if (totalWeight > 0) {
            targetX = weightedSumX / totalWeight;
            targetY = weightedSumY / totalWeight;
            double deltaX = targetX - x;
            double deltaY = targetY - y;
            targetAngle = Math.atan2(deltaY, deltaX);
            seekerAngle = targetAngle;
        }

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
                angleDifference = angleDifference * 3 + ((targetAngle - angle + PI * 3) % (PI * 2) - PI) * 0.001;
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
            speed *= (1 - Math.abs(angleDifference) * 1.0); // 旋回による運動エネルギーの消費
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

        // 視野角の範囲を描画（半透明の扇形）
        double arcStart = Math.toDegrees(((seekerFOV / 2 - seekerAngle) + PI * 3) % (PI * 2) - PI);
        double arcExtent = Math.toDegrees(-seekerFOV);
        g2d.setColor(new Color(255, 0, 0, 10));
        g2d.fillArc((int) (x - 1500), (int) (y - 1500), 3000, 3000, (int) arcStart, (int) arcExtent);
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
