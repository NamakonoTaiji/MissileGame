import java.awt.*;
import java.util.List;

public class Missile {
    // 定数
    private static final double DRAG_COEFFICIENT = 0.01; // 抗力係数
    private static final double AIR_DENSITY = 1.225; // 空気密度 (kg/m^3)
    private static final double CROSS_SECTIONAL_AREA = 0.04; // 物体の断面積 (m^2)
    private static final double PLAYER_IR_SENSITIVITY = 0.9;
    private static final double MISSILE_MAX_TURN_RATE = 0.0024;
    private static final double DELTA_V_OF_BOOSTER = 0.0027;
    private static final double IRCCM_SEEKER_FOV = Math.toRadians(2);
    private static final double NORMAL_SEEKER_FOV = Math.toRadians(8);
    private static final int LIFESPAN = 3200;
    public static final int BURN_TIME_OF_BOOSTER = 1100;

    // フィールド
    private double x;
    private double y;
    private double speed;
    private double angle;
    private double oldAngle;
    private double angleDifference;
    private double seekerAngle;
    private double seekerFOV;
    private double targetX;
    private double targetY;
    private double targetAngle;
    private int age;
    private int smokeAge = 0;
    private boolean isCloseSoundPlayed;
    private boolean isMissileSoundPlayed;

    private final EmitterManager emitterManager;
    private final Player player;
    private final String navigationMode;
    private MissileLauncher missileLauncher;

    // コンストラクタ
    public Missile(double x, double y, double speed, double angle, String navigationMode,
            EmitterManager emitterManager, Player player, MissileLauncher missileLauncher) {
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
        this.player = player;
        this.seekerFOV = NORMAL_SEEKER_FOV;
        this.missileLauncher = missileLauncher;
        this.isCloseSoundPlayed = false;
        this.isMissileSoundPlayed = false;
    }

    // 更新メソッド
    public void update() {
        updateSeekers();
        updateNavigation();
        updatePosition();
        updateSound();
        age++;
    }

    private void updateSeekers() {
        List<Emitter> emitters = emitterManager.getEmitters();
        double weightedSumX = 0;
        double weightedSumY = 0;
        double totalWeight = 0;

        for (Emitter emitter : emitters) {
            double emitterX = emitter.getX();
            double emitterY = emitter.getY();
            double emitterDistance = Math.sqrt(Math.pow(emitterX - x, 2) + Math.pow(emitterY - y, 2));
            double emitterLOSAngle = Math.atan2(emitterY - y, emitterX - x);
            double angleDifferenceToEmitter = MathUtils.normalizeAngle(seekerAngle, emitterLOSAngle); // 首振り角-熱源LOS角
            double infraredEmission = calculateInfraredEmission(emitter, emitterDistance, angleDifferenceToEmitter); // 熱源強度

            if (Math.abs(angleDifferenceToEmitter) <= seekerFOV / 2) {
                // 熱源強度に基づいた重みづけ
                weightedSumX += emitterX * infraredEmission;
                weightedSumY += emitterY * infraredEmission;
                totalWeight += infraredEmission;
            }
        }

        // 熱源強度に基づいて目標の座標を平均する
        if (totalWeight > 0) {
            targetX = weightedSumX / totalWeight;
            targetY = weightedSumY / totalWeight;
            double deltaX = targetX - x;
            double deltaY = targetY - y;
            targetAngle = Math.atan2(deltaY, deltaX); // 平均から割り出された目標LOS角
            seekerAngle = targetAngle;
            seekerFOV = IRCCM_SEEKER_FOV; // 熱源を検出してる場合は狭域シーカーを適応
        } else {
            seekerFOV = NORMAL_SEEKER_FOV; // 熱源を検出していない場合は通常シーカーを適応
        }
    }

    // 熱源強度計算メソッド
    private double calculateInfraredEmission(Emitter emitter, double emitterDistance, double angleDifferenceToEmitter) {
        double infraredEmission = emitter.getInfraredEmission();
        String sourceType = emitter.getSourceType();

        if (sourceType.equals("Player")) { // プレイヤーに対する熱源強度計算
            double playerAngle = player.getAngle();
            infraredEmission /= emitterDistance * 0.5; // 遠距離ほど熱源が小さい
            infraredEmission *= PLAYER_IR_SENSITIVITY; // プレイヤー機体に対する欺瞞耐性
            infraredEmission /= MathUtils.clamp(Math.abs(MathUtils.normalizeAngle(seekerAngle, playerAngle)), 0.4, 1.5) // 後方排気をより大きくとらえる
                    / 1.5;
        } else if (sourceType.equals("Flare")) { // フレアに対する熱源強度計算
            infraredEmission /= emitterDistance * 0.5;
        }

        return infraredEmission;
    }

    private void updateNavigation() {
        switch (navigationMode) {
            // 単追尾
            case "PPN" -> {
                angleDifference = MathUtils.normalizeAngle(targetAngle, angle);
            }
            // 比例航法
            case "PN" -> {
                angleDifference = MathUtils.normalizeAngle(targetAngle, oldAngle) * 3;
                oldAngle = targetAngle;
            }
            // 修正比例航法
            case "MPN" -> {
                angleDifference = MathUtils.normalizeAngle(targetAngle, oldAngle) * 3
                        + MathUtils.normalizeAngle(targetAngle, angle) * 0.0015;
                oldAngle = targetAngle;
            }
            // 半自動指令照準線一致誘導
            case "SACLOS" -> {
                double distFromMissileLauncher = missileLauncher.distanceFromMissileLauncher(x, y);
                double SACLOSTargetX = missileLauncher.getX()
                        + Math.cos(missileLauncher.getLauncherToTargetAngle()) * distFromMissileLauncher;
                double SACLOSTargetY = missileLauncher.getY()
                        + Math.sin(missileLauncher.getLauncherToTargetAngle()) * distFromMissileLauncher;
                double deltaX = SACLOSTargetX - x;
                double deltaY = SACLOSTargetY - y;
                angleDifference = MathUtils.normalizeAngle(Math.atan2(deltaY, deltaX), angle) * 0.0011;

            }
        }

        angleDifference = MathUtils.clamp(angleDifference, -MISSILE_MAX_TURN_RATE, MISSILE_MAX_TURN_RATE);
        angle += angleDifference;
    }

    // ミサイルの座標を更新
    private void updatePosition() {
        double acc = 0;
        double dragForce = 0.5 * DRAG_COEFFICIENT * AIR_DENSITY * CROSS_SECTIONAL_AREA * speed * speed;
        // ブースター燃焼中は加速
        if (age <= BURN_TIME_OF_BOOSTER) {
            acc = DELTA_V_OF_BOOSTER;
        }
        acc = acc - dragForce;
        speed += acc;
        speed *= (1 - Math.abs(angleDifference) * 0.1); // 旋回すると減速

        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;
    }

    private void updateSound() {
        double distFromPlayer = player.distanceFromPlayer(x, y);

        // プレイヤーとの速度差を計算
        double deltaX = player.getVelocityX() - Math.cos(angle) * speed;
        double deltaY = player.getVelocityY() - Math.sin(angle) * speed;
        double relativeSpeed = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // 近距離で風切り音を再生
        if (distFromPlayer <= 70 && !isCloseSoundPlayed) {
            String sound = speed < 1.8 ? "sounds/rocket_fly_by-007.wav" : "sounds/missile_sonic_boom-001.wav"; // 速度が1.8未満か否かで二種類の風切り音を切り替え
            SoundPlayer.playSound(sound, 0, false);
            isCloseSoundPlayed = true;
        } else if (distFromPlayer > 70) {
            isCloseSoundPlayed = false;
        }

        // 相対速度が大きい場合にのみ音を再生
        if (distFromPlayer <= 180 && !isMissileSoundPlayed && relativeSpeed > 2) {
            SoundPlayer.playSound("sounds/rocket_fly_by-004.wav", 0, false);
            isMissileSoundPlayed = true;
        } else if (distFromPlayer > 180) {
            isMissileSoundPlayed = false;
        }
    }

    // 描画メソッド
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // ミサイルを描画
        g2d.setColor(Color.BLUE);
        g2d.drawOval((int) (x - 1.5), (int) (y - 1.5), 3, 3);
        g.drawLine((int) x, (int) y, (int) ((x) + 5 * Math.cos(angle)), (int) ((y) + 5 * Math.sin(angle)));

        if (!navigationMode.equals("SACLOS")) {
            // 焦点を描画
            g2d.setColor(Color.BLACK);
            g2d.drawOval((int) targetX - 3, (int) targetY - 3, 6, 6);

            // シーカーの視点方向の視野角を描画
            double fovLeft = MathUtils.normalizeAngle(seekerAngle, -seekerFOV / 2);
            double fovRight = MathUtils.normalizeAngle(seekerAngle, seekerFOV / 2);
            g2d.setColor(new Color(255, 0, 0, 10));
            g2d.fillPolygon(
                    new int[] { (int) x, (int) (x + Math.cos(fovLeft) * 8000), (int) (x + Math.cos(fovRight) * 8000) },
                    new int[] { (int) y, (int) (y + Math.sin(fovLeft) * 8000), (int) (y + Math.sin(fovRight) * 8000) },
                    3);
        }
    }

    // 判定メソッド
    public boolean isExpired() {
        return age >= LIFESPAN;
    }

    // 排煙を記憶させた時刻の入力/出力
    public int getSmokeAddTime() {
        return smokeAge;
    }

    public void setSmokeAddTime(int age) {
        this.smokeAge = age;
    }

    public double getSpeed() {
        return speed;
    }

    // ゲッターメソッド
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getAge() {
        return age;
    }
}
