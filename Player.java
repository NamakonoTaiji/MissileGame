import java.awt.*;
import java.awt.geom.AffineTransform;

public class Player implements Emitter {
    private static final int ARROW_SIZE = 6;
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

    public Player(double x, double y, double speed, double maxTurnRate, EmitterManager emitterManager) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.maxTurnRate = maxTurnRate;
        this.angle = 0.0;
        this.flareManager = new FlareManager(emitterManager);
    }

    public void update() {
        if (upPressed) {
            x += Math.cos(angle) * speed;
            y += Math.sin(angle) * speed;
        }
        if (leftPressed) {
            angle -= maxTurnRate;
        }
        if (rightPressed) {
            angle += maxTurnRate;
        }

        if (zPressed) {
            if (!isBeforeZPressed) {
                flareManager.addFlare(x, y, 1.0, angle);
                isBeforeZPressed = true;
            }
        } else {
            isBeforeZPressed = false;
        }

        if (x < ARROW_SIZE)
            x = ARROW_SIZE;
        if (x > MissileSimulator.PANEL_WIDTH - ARROW_SIZE)
            x = MissileSimulator.PANEL_WIDTH - ARROW_SIZE;
        if (y < ARROW_SIZE)
            y = ARROW_SIZE;
        if (y > MissileSimulator.PANEL_HEIGHT - ARROW_SIZE)
            y = MissileSimulator.PANEL_HEIGHT - ARROW_SIZE;

        flareManager.updateFlares();
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
