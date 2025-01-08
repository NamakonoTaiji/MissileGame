// プレイヤーのクラス

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.List;

public class Player implements Emitter, Reflector {
    // 定数
    private static final int ARROW_SIZE = 6;
    private static final int IMAGE_REDUCTION = 20; // 画像の縮小率
    private static final double AOA_MAX = 0.0030; // 最大迎角
    private static final double ENGINE_POWER = 4; // エンジン出力
    private static final double MASS = 15000; // 機体の質量
    private static final double DRAG_COEFFICIENT = 0.16; // 抗力係数
    private static final double LIFT_COEFFICIENT = 0.5; // 揚力係数
    private static final double AIR_DENSITY = 1.225; // 空気密度 (kg/m^3)
    private static final double CROSS_SECTIONAL_AREA = 20; // 物体の断面積 (m^2)
    private static final double INFRARED_EMISSION = 1.0; // 赤外線放射強度
    public static final double REFLECTOR_STRENGTH = 1.1; // レーダー反射強度
    private final String TEAM = "Alpha";

    // フィールド
    private double x;
    private double y;
    private double angle; // 機体の向き
    private double speed;
    private double velocityX;
    private double velocityY;
    private double dragForce = 0;
    private double liftForce = 0;
    private int imageWidth;
    private int imageHeight;
    private boolean upPressed;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean isBeforeZPressed = false;
    private boolean isZKeyPressed;
    private FlareManager flareManager;
    private ChaffManager chaffManager;
    private BufferedImage playerImage;
    private boolean mach = false;
    private boolean isSonicBoomed = false;
    private Radar radar;

    // コンストラクタ
    public Player(double x, double y, double speed, EmitterManager emitterManager,
            ReflectorManager reflectorManager, double scale, RWRManager rwrManager) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.angle = 0.0; // 初期の機体の向き
        this.velocityX = Math.cos(angle) * speed;
        this.velocityY = Math.sin(angle) * speed;
        this.flareManager = new FlareManager(emitterManager);
        this.chaffManager = new ChaffManager(reflectorManager);
        this.radar = new Radar("Player", TEAM, "SWEEP", true, reflectorManager, x + Math.cos(angle) * 10,
                y + Math.sin(angle) * 10, angle, rwrManager, 5000, 0.006);

        // 画像を読み込む
        try {
            playerImage = ImageIO.read(new File("images/F-2.png"));
            imageWidth = (int) (playerImage.getWidth() / IMAGE_REDUCTION * scale); // 縮小サイズを指定
            imageHeight = (int) (playerImage.getHeight() / IMAGE_REDUCTION * scale);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // エンジン音ループ再生
        SoundPlayer.playEngineSound("sounds/engine_66_exterior.wav", 0, true);
    }

    // プレイヤーの更新
    public void update(LabelManager labelManager) {
        double acc = calculateAcceleration();
        double adjustmentFactorConst = 0;

        // 旋回操作
        adjustmentFactorConst = handleTurning(adjustmentFactorConst);

        // 慣性効果の計算
        double inertiaEffect = acc - calculateDragAndLift();

        // 加速度の反映と速度の更新
        speed += inertiaEffect;
        speed = Math.max(speed, 0.1); // 静止バグ対策

        // 速度ベクトルの更新
        updateVelocity(adjustmentFactorConst);

        // 速度ベクトルを位置に反映
        x += velocityX;
        y += velocityY;

        handleSonicBoom();
        handleFlare();

        // レーダーの位置と向きを更新
        radar.setAngle(angle);
        radar.update("SWEEP", x + Math.cos(angle) * 10,
                y + Math.sin(angle) * 10);

        // checkCollisions();
        flareManager.updateFlares();
        chaffManager.updateChaffs();
    }

    // ↑キー入力時加速
    private double calculateAcceleration() {
        return upPressed ? ENGINE_POWER / MASS : 0; // エンジン稼働時の加速度
    }

    // 旋回操作
    private double handleTurning(double adjustmentFactorConst) {
        double rudderRock = MathUtils.clamp(-0.5 * speed + 1.45, 0.1, 1); // 高速域舵ロック
        double turnRate = MathUtils.clamp(1 - Math.abs(speed - 0.8) / 0.5, 0.3, 1); // 適正旋回速度の再現
        double AOA = AOA_MAX * turnRate * rudderRock; // 舵ロックと適正旋回速度を組み込んだ旋回

        if (leftPressed) {
            adjustmentFactorConst = 0.0024;
            angle -= AOA;
        }
        if (rightPressed) {
            adjustmentFactorConst = 0.0024;
            angle += AOA;
        }
        return adjustmentFactorConst;
    }

    // 慣性計算
    private double calculateDragAndLift() {
        liftForce = 0.5 * LIFT_COEFFICIENT * AIR_DENSITY * CROSS_SECTIONAL_AREA * speed * speed;
        dragForce = 0.5 * DRAG_COEFFICIENT * AIR_DENSITY * CROSS_SECTIONAL_AREA * speed * speed;

        // 速度ベクトルの角度と機体の向きの角度の違いによる追加の空気抵抗
        double velocityAngle = Math.atan2(velocityY, velocityX);
        double angleDifference = Math.abs(MathUtils.normalizeAngle(velocityAngle, angle));
        double turningAdditionalDrag = (leftPressed || rightPressed)
                ? MathUtils.clamp(1 - Math.abs(speed - 0.8) / 0.5, 0.3, 1) * 1.2
                : 0;

        return (dragForce + Math.min(DRAG_COEFFICIENT + Math.abs(Math.sin(angleDifference)) * 15, 4)
                + turningAdditionalDrag) / MASS;
    }

    // 速度ベクトルの更新
    private void updateVelocity(double adjustmentFactorConst) {
        double velocityMagnitude = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        double normalizedVelocityX = velocityX / velocityMagnitude;
        double normalizedVelocityY = velocityY / velocityMagnitude;

        double targetVelocityX = Math.cos(angle);
        double targetVelocityY = Math.sin(angle);

        // 速度によってドリフト角を調整する調整ファクター
        double adjustmentFactor = MathUtils.clamp(speed * 0.006, 0.0001, 1.4) + adjustmentFactorConst;

        // ベクトルを徐々に機体の向きに合わせる
        normalizedVelocityX += (targetVelocityX - normalizedVelocityX) * adjustmentFactor;
        normalizedVelocityY += (targetVelocityY - normalizedVelocityY) * adjustmentFactor;

        // ベクトルの正規化と速度の反映
        velocityX = normalizedVelocityX * speed;
        velocityY = normalizedVelocityY * speed;
    }

    // 音速突破時衝撃音再生
    private void handleSonicBoom() {
        if (speed >= 1.224) {
            mach = true;
        } else {
            mach = false;
            isSonicBoomed = false;
        }

        if (mach && !isSonicBoomed) {
            SoundPlayer.playSound("sounds/sonic_boom_close-002.wav", 5, false);
            isSonicBoomed = true;
        }
    }

    // フレア生成
    private void handleFlare() {
        if (isZKeyPressed) {
            if (!isBeforeZPressed) {
                flareManager.addFlare(x - Math.cos(angle) * 10, y - Math.sin(angle) * 10, speed - 0.3,
                        Math.atan2(velocityY, velocityX)); // 少し後方からフレアを展開
                chaffManager.addChaff(x - Math.cos(angle) * 10, y - Math.sin(angle) * 10, speed - 0.3,
                        Math.atan2(velocityY, velocityX)); // 少し後方からチャフを展開
                SoundPlayer.playSound("sounds/flare-001.wav", 0, false);
                isBeforeZPressed = true;
            }
        } else {
            isBeforeZPressed = false;
        }
    }

    // 描画メソッド
    public void draw(Graphics2D g2d) {
        AffineTransform originalTransform = g2d.getTransform();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (playerImage != null) {
            drawPlayerImage(g2d);
        } else {
            drawPlayerShape(g2d);
        }

        g2d.setTransform(originalTransform);

        // レーダーを描画
        radar.draw(g2d);

        // レーダーで取得した反射体の座標を四角で囲む
        List<DetectedReflector> reflectors = radar.scanForReflectors();
        g2d.setColor(new Color(0, 255, 0, 170));
        for (DetectedReflector reflector : reflectors) {
            g2d.drawRect((int) reflector.getX() - 10, (int) reflector.getY() - 10, 20, 20); // 反射体の座標を中心に10x10の四角を描画
        }

        // ベクトルの向きを描画
        g2d.setColor(Color.GREEN);
        g2d.drawLine((int) x, (int) y, (int) (x + velocityX * 100), (int) (y + velocityY * 100));

        // フレアを描画
        flareManager.drawFlares(g2d);
        chaffManager.drawChaffs(g2d);
    }

    private void drawPlayerImage(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        // プレイヤーの中心に移動
        g2d.translate(x, y);
        // 画像の中心を基準に回転
        g2d.rotate(angle);
        // 画像の縮小
        int drawX = -imageWidth / 2;
        int drawY = -imageHeight / 2;
        g2d.drawImage(playerImage, drawX, drawY, imageWidth, imageHeight, null);
    }

    // プレイヤーの形状を描画
    private void drawPlayerShape(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.translate((int) x, (int) y);
        g2d.rotate(angle);
        g2d.fillPolygon(new int[] { -ARROW_SIZE, ARROW_SIZE, -ARROW_SIZE },
                new int[] { -ARROW_SIZE / 2, 0, ARROW_SIZE / 2 }, 3);
    }

    // キーイベント設定メソッド
    public void setUpPressed(boolean upPressed) {
        this.upPressed = upPressed;
    }

    public void setLeftPressed(boolean leftPressed) {
        this.leftPressed = leftPressed;
    }

    public void setRightPressed(boolean rightPressed) {
        this.rightPressed = rightPressed;
    }

    public void setZPressed(boolean zPressed) {
        this.isZKeyPressed = zPressed;
    }

    // ゲッターメソッド
    public double getAngle() {
        return angle;
    }

    public double getSpeed() {
        return speed;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public double getDragForce() {
        return dragForce;
    }

    public double getLiftForce() {
        return liftForce;
    }

    public double getDistanceFromPlayer(double targetX, double targetY) {
        return Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));
    }

    // インターフェースの実装
    @Override
    public String getSourceType() {
        return "Player";
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
    public double getInfraredEmission() {
        return INFRARED_EMISSION;
    }

    @Override
    public double getReflectanceStrength() {
        return REFLECTOR_STRENGTH;
    }
}
