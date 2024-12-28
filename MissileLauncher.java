import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

public class MissileLauncher {
    private double x;
    private double y;
    private double angle;
    private boolean isLoaded;
    private String navigationMode = "PPN";
    private int missileCount;
    private double launchSpeed;
    private double reloadTime;
    private double launcherToTargetAngle = 0;
    private List<Missile> missiles;

    public MissileLauncher(double x, double y, double launchSpeed, double reloadTime) {
        this.x = x;
        this.y = y;
        this.angle = 0;
        this.isLoaded = false;
        this.missileCount = 0;
        this.launchSpeed = launchSpeed;
        this.reloadTime = reloadTime;
        this.missiles = Collections.synchronizedList(new ArrayList<>()); // スレッドセーフなリストに変更
    }

    public void loadMissile() {
        // 必要に応じて実装
    }

    public void launchMissile() {
        if (isLoaded) {
            Missile missile = new Missile(x, y, 0, launcherToTargetAngle, navigationMode);
            synchronized (missiles) {
                missiles.add(missile);
            }
            System.out.println("Missile launched at angle" + String.format("%.2f", Math.toDegrees(
                    launcherToTargetAngle)) + " with speed " + launchSpeed);
            isLoaded = false;
            missileCount = 0;
        } else {
            System.out.println("No missile loaded.");
        }
    }

    public void updateMissiles(double targetX, double targetY) {
        if (!isLoaded) {
            if (missileCount < reloadTime) {
                missileCount++;
            } else if (missileCount >= reloadTime) {
                isLoaded = true;
            }
        }
        synchronized (missiles) {
            Iterator<Missile> iterator = missiles.iterator();
            while (iterator.hasNext()) {
                Missile missile = iterator.next();
                missile.update(targetX, targetY);
                if (missile.isExpired()) {
                    iterator.remove();
                }
            }
        }
    }

    public void rotate(double newAngle) {
        this.angle = newAngle;
    }

    public boolean isReadyToLaunch() {
        return isLoaded;
    }

    public int getMissileCount() {
        return missileCount;
    }

    public void setMissileMode() {
        if (navigationMode == "PPN") {
            navigationMode = "PN";
        } else if (navigationMode == "PN") {
            navigationMode = "MPN";
        } else if (navigationMode == "MPN") {
            navigationMode = "PPN";
        }
    }

    public String getMissileMode() {
        return navigationMode;
    }

    public void draw(Graphics g, double targetX, double targetY) {
        launcherToTargetAngle = Math.atan2((targetY - y), (targetX - x));
        // 発射台の描画
        g.setColor(Color.BLUE);
        g.drawRect((int) x, (int) y, 30, 10);
        g.drawLine((int) x + 15, (int) y + 5, (int) ((x + 15) + 15 * Math.cos(launcherToTargetAngle)),
                (int) ((y + 5) + 15 * Math.sin(launcherToTargetAngle)));
        // ミサイルの描画
        synchronized (missiles) {
            for (Missile missile : missiles) {
                missile.draw(g);
            }
        }
    }

    public double getLauncherToTargetAngle() {
        return launcherToTargetAngle;
    }

    public List<Missile> getMissiles() {
        synchronized (missiles) {
            return missiles;
        }
    }
}
