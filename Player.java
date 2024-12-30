import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;

public class Player implements Emitter {
    private static final int ARROW_SIZE = 6;
    private static final int IMAGE_REDUCTION = 20;
    private double x;
    private double y;
    private double angle; // 機体の向き
    private double speed;
    private double velocityX; // ベクトルのX成分
    private double velocityY; // ベクトルのY成分
    private double maxTurnRate = 0.05;
    private double infraredEmission = 1.0;
    private boolean upPressed;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean isBeforeZPressed = false;
    private boolean zPressed;
    private FlareManager flareManager;
    private BufferedImage playerImage;
    private int imageWidth;
    private int imageHeight;
    private double scale;
    private double dragForce = 0;
    private double liftForce = 0;
    private final double AOA_MAX = 0.0033;
    private final double ENGINE_POWER = 3.8;
    private final double MASS = 15000;
    private final double DRAG_COEFFICIENT = 0.16; // 抗力係数
    private final double LIFT_COEFFICIENT = 0.5; // 揚力係数
    private final double AIR_DENSITY = 1.225; // 空気密度 (kg/m^3)
    private final double CROSS_SECTIONAL_AREA = 20; // 物体の断面積 (m^2)

    public Player(double x, double y, double speed, EmitterManager emitterManager, double scale) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.angle = 0.0; // 初期の機体の向き
        this.velocityX = Math.cos(angle) * speed;
        this.velocityY = Math.sin(angle) * speed;
        this.flareManager = new FlareManager(emitterManager);
        this.scale = scale;

        // 画像を読み込む
        try {
            playerImage = ImageIO.read(new File("images/F-2.png"));
            imageWidth = (int) (playerImage.getWidth() / IMAGE_REDUCTION * scale); // 縮小サイズを指定
            imageHeight = (int) (playerImage.getHeight() / IMAGE_REDUCTION * scale);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        double acc = 0;
        double adjustmentFactorConst = 0;
        double rudderRock = MathUtils.clamp(-1.3333 * speed + 2.0667, 0.1, 1); // 高速域舵ロック
        double turnRate = MathUtils.clamp(1 - Math.abs(speed - 0.8) / 0.7, 0.4, 1); // 適正旋回速度の再現
        double turningAdditionalDrag = 0;
        double AOA = AOA_MAX * turnRate * rudderRock; // 舵ロックと適正旋回速度を組み込んだ旋回
        System.out.println(turnRate);
        // エンジンの加速
        if (upPressed) {
            acc = ENGINE_POWER / MASS; // エンジン稼働時の加速度
        }

        // 旋回操作
        if (leftPressed) {
            adjustmentFactorConst = 0.003;
            angle -= AOA;
            turningAdditionalDrag = turnRate;// 適正旋回速度で曲がるとエネルギー損失大
        }
        if (rightPressed) {
            adjustmentFactorConst = 0.003;
            angle += AOA;
            turningAdditionalDrag = turnRate;// 適正旋回速度で曲がるとエネルギー損失大
        }

        // フレアの操作
        if (zPressed) {
            if (!isBeforeZPressed) {
                flareManager.addFlare(x, y, 0.05, angle);
                isBeforeZPressed = true;
            }
        } else {
            isBeforeZPressed = false;
        }

        // 揚力と抗力の計算
        liftForce = 0.5 * LIFT_COEFFICIENT * AIR_DENSITY * CROSS_SECTIONAL_AREA * speed * speed;
        dragForce = 0.5 * DRAG_COEFFICIENT * AIR_DENSITY * CROSS_SECTIONAL_AREA * speed * speed;

        // 速度ベクトルの角度と機体の向きの角度の違いによる追加の空気抵抗
        double velocityAngle = Math.atan2(velocityY, velocityX);
        double angleDifference = Math.abs(velocityAngle - angle);
        double additionalDrag = Math.min(Math.abs(DRAG_COEFFICIENT + Math.sin(angleDifference) * 11), 3)
                + turningAdditionalDrag * 1.1; // ドリフト角が大きいほど空気抵抗増加

        // 慣性効果の計算
        double inertiaEffect = acc - (dragForce + additionalDrag) / MASS;

        // 加速度の反映と速度の更新
        speed += inertiaEffect;

        // 速度ベクトルの更新
        double velocityMagnitude = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        double normalizedVelocityX = velocityX / velocityMagnitude;
        double normalizedVelocityY = velocityY / velocityMagnitude;

        double targetVelocityX = Math.cos(angle);
        double targetVelocityY = Math.sin(angle);

        // 速度によってドリフト角を調整する調整ファクター
        // 速度が低いほど大きなドリフト角,直進時はadjustmentFactorConstが0になりドリフトがゆっくり戻る
        double adjustmentFactor = MathUtils.clamp(speed * 0.008, 0.0006, 1.4) + adjustmentFactorConst;

        // ベクトルを徐々に機体の向きに合わせる
        normalizedVelocityX += (targetVelocityX - normalizedVelocityX) * adjustmentFactor;
        normalizedVelocityY += (targetVelocityY - normalizedVelocityY) * adjustmentFactor;

        // ベクトルの正規化と速度の反映
        velocityX = normalizedVelocityX * speed;
        velocityY = normalizedVelocityY * speed;

        // 速度ベクトルを位置に反映
        x += velocityX;
        y += velocityY;

        flareManager.updateFlares();
    }

    public void draw(Graphics2D g2d) {
        AffineTransform originalTransform = g2d.getTransform();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (playerImage != null) {
            g2d.setColor(Color.YELLOW);
            // プレイヤーの中心に移動
            g2d.translate(x, y);
            // 画像の中心を基準に回転
            g2d.rotate(angle);
            // 画像の縮小
            int drawX = -imageWidth / 2;
            int drawY = -imageHeight / 2;
            g2d.drawImage(playerImage, drawX, drawY, imageWidth, imageHeight, null);
        } else {
            g2d.setColor(Color.RED);
            g2d.translate((int) x, (int) y);
            g2d.rotate(angle);
            g2d.fillPolygon(new int[] { -ARROW_SIZE, ARROW_SIZE, -ARROW_SIZE },
                    new int[] { -ARROW_SIZE / 2, 0, ARROW_SIZE / 2 }, 3);
        }
        g2d.setTransform(originalTransform);

        // ベクトルの向きを描画
        g2d.setColor(Color.GREEN);
        g2d.drawLine((int) x, (int) y, (int) (x + velocityX * 100), (int) (y + velocityY * 100));

        // フレアを描画
        flareManager.drawFlares(g2d);
    }

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
        this.zPressed = zPressed;
    }

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
        return infraredEmission;
    }
}
