import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

public class Player {
    private static final int ARROW_SIZE = 6; // 矢印の半サイズ
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
    private List<Flare> flares;

    public Player(double x, double y, double speed, double maxTurnRate) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.maxTurnRate = maxTurnRate;
        this.angle = 0.0;
        this.flares = Collections.synchronizedList(new ArrayList<>());
    }

    public void update() {
        if (upPressed) {
            x += Math.cos(angle) * speed; // 前進の速度
            y += Math.sin(angle) * speed; // 前進の速度
        }
        if (leftPressed) {
            angle -= maxTurnRate; // 左に旋回の速度
        }
        if (rightPressed) {
            angle += maxTurnRate; // 右に旋回の速度
        }

        // フレアの発射処理
        if (zPressed) {
            if (!isBeforeZPressed) {
                // 新しいフレアをリストに追加
                flares.add(new Flare(x, y, 1.0, angle));
                isBeforeZPressed = true;
            }
        } else {
            isBeforeZPressed = false;
        }

        // 画面の端での座標の制限
        if (x < ARROW_SIZE)
            x = ARROW_SIZE;
        if (x > MissileSimulator.PANEL_WIDTH - ARROW_SIZE)
            x = MissileSimulator.PANEL_WIDTH - ARROW_SIZE;
        if (y < ARROW_SIZE)
            y = ARROW_SIZE;
        if (y > MissileSimulator.PANEL_HEIGHT - ARROW_SIZE)
            y = MissileSimulator.PANEL_HEIGHT - ARROW_SIZE;

        // フレアの更新
        updateFlares();
    }

    private void updateFlares() {
        synchronized (flares) {
            Iterator<Flare> iterator = flares.iterator();
            while (iterator.hasNext()) {
                Flare flare = iterator.next();
                flare.update();
                if (flare.isExpired()) {
                    iterator.remove();
                }
            }
        }
    }

    public void draw(Graphics2D g2d) {
        AffineTransform originalTransform = g2d.getTransform();

        g2d.setColor(Color.RED);
        g2d.translate((int) x, (int) y);
        g2d.rotate(angle);
        g2d.fillPolygon(new int[] { -ARROW_SIZE, ARROW_SIZE, -ARROW_SIZE },
                new int[] { -ARROW_SIZE / 2, 0, ARROW_SIZE / 2 }, 3);
        g2d.setTransform(originalTransform);

        g2d.drawOval((int) (x - ARROW_SIZE), (int) (y - ARROW_SIZE), ARROW_SIZE * 2, ARROW_SIZE * 2);

        // フレアの描画
        synchronized (flares) {
            for (Flare flare : flares) {
                flare.draw(g2d);
            }
        }
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

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSpeed() {
        return speed;
    }

    public double getAngle() {
        return angle;
    }

    public double getIREmission() {
        return infraredEmission;
    }
}
