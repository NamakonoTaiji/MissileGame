// プレイヤーとミサイルの衝突判定を行うクラス

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CollisionDetector {
    // フィールド
    private double hitRadius;
    private Random random = new Random(); // 乱数生成器
    private List<MissileIR> irMissiles;
    private List<MissileARH> arhMissiles;
    private LabelManager labelManager;

    // 被弾音ファイルのリスト
    private static final String[] HIT_SOUNDS = {
            "sounds/module_damaged/module_damage-001.wav",
            "sounds/module_damaged/module_damage-002.wav",
            "sounds/module_damaged/module_damage-003.wav"
    };

    // コンストラクタ
    public CollisionDetector(double hitRadius, List<MissileIR> missiles) {
        this.hitRadius = hitRadius;
        this.irMissiles = missiles;
    }

    // 衝突判定メソッド
    public void checkCollisions(double playerX, double playerY, List<MissileIR> irMissiles,
            List<MissileARH> arhMissiles, LabelManager labelManager) {
        double missileHitRadius = 1.0;
        this.labelManager = labelManager;
        this.irMissiles = irMissiles;
        this.arhMissiles = arhMissiles;

        // プレイヤーとIRミサイルの距離を計算し、ミサイルがプレイヤーに当たっているか判定
        synchronized (irMissiles) {
            Iterator<MissileIR> iterator = irMissiles.iterator();
            while (iterator.hasNext()) {
                MissileIR irMissile = iterator.next();
                double distance = Math.sqrt(
                        Math.pow(irMissile.getX() - playerX, 2) + Math.pow(irMissile.getY() - playerY, 2));
                if (distance < hitRadius + missileHitRadius) {
                    handleHit(irMissile);
                    iterator.remove();
                }
            }
        }

        // プレイヤーとARHミサイルの距離を計算し、ミサイルがプレイヤーに当たっているか判定
        synchronized (arhMissiles) {
            Iterator<MissileARH> iterator = arhMissiles.iterator();
            while (iterator.hasNext()) {
                MissileARH arhMissile = iterator.next();
                double distance = Math.sqrt(
                        Math.pow(arhMissile.getX() - playerX, 2) + Math.pow(arhMissile.getY() - playerY, 2));
                if (distance < hitRadius + missileHitRadius) {
                    handleHit(arhMissile);
                    iterator.remove();
                }
            }
        }
    }

    // IRミサイル被弾時処理
    private void handleHit(MissileIR irMissile) {
        String message = "Hit detected!";
        System.out.println(message);
        if (labelManager != null) {
            labelManager.addLogMessage(message);
        }
        playHitSound();
    }

    // ARHミサイル被弾時処理
    private void handleHit(MissileARH arhMissile) {
        String message = "Hit detected!";
        System.out.println(message);
        if (labelManager != null) {
            labelManager.addLogMessage(message);
        }

        playHitSound();
    }

    // ランダムな被弾音を再生
    private void playHitSound() {
        String hitSound = HIT_SOUNDS[random.nextInt(HIT_SOUNDS.length)];
        SoundPlayer.playSound(hitSound, 0, false);
    }
}