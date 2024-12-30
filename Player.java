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
    private final double AOA_MAX = 0.1;
    private final double ENGINE_POWER = 2;
    private final double MASS = 7000;
    private final double DRAG_COEFFICIENT = 0.2; // 抗力係数
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
        double rudderRock = MathUtils.clamp(-1.8 * speed + 2.26, 0.1, 1);
        double AOA = AOA_MAX * rudderRock;

        // エンジンの加速
        if (upPressed) {
            acc = ENGINE_POWER / MASS; // エンジン稼働時の加速度
        }

        // 旋回操作
        if (leftPressed) {
            angle -= AOA * maxTurnRate;
        }
        if (rightPressed) {
            angle += AOA * maxTurnRate;
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

        // 横加速度による迎え角の増大に伴う追加の空気抵抗
        double lateralAcceleration = speed * speed / (MASS / liftForce); // 横加速度
        double inducedDrag = 0.5 * DRAG_COEFFICIENT * AIR_DENSITY * CROSS_SECTIONAL_AREA * lateralAcceleration * AOA;
        dragForce += inducedDrag;

        // 慣性効果の計算
        double inertiaEffect = acc - dragForce / MASS;

        // 加速度の反映と速度の更新
        speed += inertiaEffect;

        // 速度ベクトルの更新
        double moveAngle = Math.atan2(velocityY, velocityX);
        double alignmentFactor = (1 - Math.min(speed / 1.2, 1)) * 0.1; // 速度に応じた調整
        moveAngle += (angle - moveAngle) * (1 - alignmentFactor); // 速度に応じてベクトルの向きと機体の向きを調整

        velocityX += Math.cos(moveAngle) * speed * alignmentFactor;
        velocityY += Math.sin(moveAngle) * speed * alignmentFactor;

        // ベクトルを正規化し、速度に再度反映
        double velocityMagnitude = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        velocityX = (velocityX / velocityMagnitude) * speed;
        velocityY = (velocityY / velocityMagnitude) * speed;

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
