//  アクティブレーダー誘導ミサイルのクラス

import java.awt.*;

public class ARHMissile {

    // 定数
    private static final double DRAG_COEFFICIENT = 0.01; // 抗力係数
    private static final double AIR_DENSITY = 1.225; // 空気密度 (kg/m^3)
    private static final double CROSS_SECTIONAL_AREA = 0.04; // 物体の断面積 (m^2)
    private static final double MISSILE_MAX_TURN_RATE = 0.0024; // ミサイルの最大旋回速度
    private static final double NORMAL_SEEKER_FOV = Math.toRadians(5); // シーカー視野角
    private static final int LIFESPAN = 3900; // ミサイルの寿命
    private static final double DELTA_V_OF_BOOSTER = 0.0032; // ブースターの加速度
    public static final int BURN_TIME_OF_BOOSTER = 1600; // ブースター燃焼時間

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

    private final Player player;
    private final String navigationMode;
    private Radar radar;

    // コンストラクタ
    public ARHMissile(String id, double x, double y, double speed, double angle, String navigationMode,
            ReflectorManager reflectorManager, Player player, RWRManager rwrManager) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.speed = speed;
        this.angle = angle;
        this.targetAngle = angle;
        this.oldAngle = angle;
        this.seekerAngle = angle;
        this.seekerFOV = NORMAL_SEEKER_FOV;
        this.navigationMode = navigationMode;
        this.player = player;
        this.isCloseSoundPlayed = false;
        this.isMissileSoundPlayed = false;
        this.radar = new Radar(id, "MSL", "Launch", false, reflectorManager, x, y, angle, rwrManager, 6000, 0.01);
    }

    // 更新メソッド
    public void update() {
        radar.update("Launch", x, y);
        updateSeekers();
        updateNavigation();
        updatePosition();
        updateSound();
        age++;
    }

    // レーダーで補足した反射体の座標を更新
    private void updateSeekers() {
        XYCoordinate targetXYCoordinate = radar.getStrongestReflectXYCoordinate(x, y);
        if (targetXYCoordinate.x != 0) {
            updateTargetPosition(targetXYCoordinate);
        }
    }

    // レーダーで補足した反射体の座標を更新
    private void updateTargetPosition(XYCoordinate targetXYCoordinate) {
        targetX = targetXYCoordinate.x;
        targetY = targetXYCoordinate.y;
        double deltaX = targetX - x;
        double deltaY = targetY - y;
        targetAngle = Math.atan2(deltaY, deltaX);
    }

    // ミサイルの角度を更新
    private void updateNavigation() {
        switch (navigationMode) {
            case "ARH" -> updateModifiedProportionalNavigation();
        }
        angleDifference = MathUtils.clamp(angleDifference, -MISSILE_MAX_TURN_RATE, MISSILE_MAX_TURN_RATE);
        angle += angleDifference;
    }

    // 修正比例航法による目標角速度の算出
    private void updateModifiedProportionalNavigation() {
        angleDifference = MathUtils.normalizeAngle(targetAngle, oldAngle) * 3
                + MathUtils.normalizeAngle(targetAngle, angle) * 0.0015;
        oldAngle = targetAngle;
    }

    // ミサイルの座標を更新
    private void updatePosition() {
        double acc = 0;
        double dragForce = 0.5 * DRAG_COEFFICIENT * AIR_DENSITY * CROSS_SECTIONAL_AREA * speed * speed; // 空気抵抗
        // ブースター燃焼中は加速
        if (age <= BURN_TIME_OF_BOOSTER) {
            acc = DELTA_V_OF_BOOSTER;
        }
        acc = acc - dragForce;
        speed += acc;
        speed *= (1 - Math.abs(angleDifference) * 0.2); // 旋回すると減速

        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;
    }

    // 音声の再生
    private void updateSound() {
        double distFromPlayer = player.getDistanceFromPlayer(x, y);

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

        // 相対速度が大きい場合にのみrocket_fly_by-004.wavを再生
        if (distFromPlayer <= 140 && !isMissileSoundPlayed && relativeSpeed > 2) {
            SoundPlayer.playSound("sounds/rocket_fly_by-004.wav", 0, false);
            isMissileSoundPlayed = true;
        } else if (distFromPlayer > 140) {
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
        radar.draw(g);

        g2d.setColor(new Color(255, 0, 0));
        g2d.drawOval((int) targetX - 10, (int) targetY - 10, 20, 20);
    }

    // ミサイルの寿命が尽きたか否か
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

    // ゲッターメソッド
    public double getSpeed() {
        return speed;
    }

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
