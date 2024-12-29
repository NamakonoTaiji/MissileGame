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
    private double angle;
    private double speed;
    private double maxTurnRate;
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
    private final double AIR_RESISTANCE = 0.999;
    private final double ACCELERATION = 0.0008;

    public Player(double x, double y, double speed, double maxTurnRate, EmitterManager emitterManager, double scale) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.maxTurnRate = maxTurnRate;
        this.angle = 0.0;
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
        if (upPressed) {
            speed += ACCELERATION;
        }
        if (leftPressed) {
            angle -= maxTurnRate;
            speed *= (1 - maxTurnRate * 0.4);
        }
        if (rightPressed) {
            angle += maxTurnRate;
            speed *= (1 - maxTurnRate * 0.4);
        }

        if (zPressed) {
            if (!isBeforeZPressed) {
                flareManager.addFlare(x, y, 0.05, angle);
                isBeforeZPressed = true;
            }
        } else {
            isBeforeZPressed = false;
        }

        speed *= AIR_RESISTANCE;
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;

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

            // 回転の中心に赤い点を描画
            // g2d.setColor(Color.RED);
            // g2d.fillOval(-3, -3, 6, 6);
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
