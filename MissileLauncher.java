import java.awt.*;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;

public class MissileLauncher {
    // 定数
    private static final String DEFAULT_NAVIGATION_MODE = "PPN";
    private static final Color LAUNCHER_COLOR = Color.BLUE;
    private static final int LAUNCHER_WIDTH = 30;
    private static final int LAUNCHER_HEIGHT = 10;
    private static final int LAUNCHER_ARM_LENGTH = 15;
    private final String TEAM = "Beta";

    // フィールド
    private double x;
    private double y;
    private boolean isLoaded;
    private String navigationMode;
    private int missileCount;
    private double launchSpeed;
    private double reloadTime;
    private double launcherToTargetAngle;
    private List<Missile> missiles;
    private EmitterManager emitterManager;
    private Player player;
    private Radar searchRadar;
    private Radar trackRadar;
    private String id;
    private String radarMode = "CLOCKWISE";

    // コンストラクタ
    public MissileLauncher(String id, double x, double y, double launchSpeed, double reloadTime,
            EmitterManager emitterManager,
            ReflectorManager reflectorManager,
            Player player, RWRManager rwrManager) {
        this.x = x;
        this.y = y;
        this.isLoaded = false;
        this.navigationMode = DEFAULT_NAVIGATION_MODE;
        this.missileCount = 0;
        this.launchSpeed = launchSpeed;
        this.reloadTime = reloadTime;
        this.missiles = Collections.synchronizedList(new ArrayList<>());
        this.emitterManager = emitterManager;
        this.player = player;
        this.searchRadar = new Radar("SAM1_Search", TEAM, this.radarMode, true, reflectorManager, x, y, 0, rwrManager);
        this.trackRadar = new Radar("SAM1_Track", TEAM, "Track", true, reflectorManager, x, y, 0, rwrManager);
        this.id = id;
    }

    // ミサイルの発射
    public void launchMissile() {
        double distanceFromPlayer = player.distanceFromPlayer(x, y);
        if (isLoaded) {
            Missile missile = new Missile(x, y, launchSpeed,
                    launcherToTargetAngle, navigationMode, emitterManager,
                    player, this);
            missiles.add(missile);
            if (distanceFromPlayer <= 1600) {
                float volume = (float) MathUtils.clamp(-0.01 * distanceFromPlayer + 1.3333, -16, 0);
                if (distanceFromPlayer < 200) {
                    SoundPlayer.playSound("sounds/missile_start_heavy-001.wav", 0, false);
                } else {
                    SoundPlayer.playSound("sounds/missile_start_heavy_far-002.wav", volume, false);
                }
            }
            System.out
                    .println("Missile launched at angle " + String.format("%.2f", Math.toDegrees(launcherToTargetAngle))
                            + " with speed " + launchSpeed);
            isLoaded = false;
            missileCount = 0;
        } else {
            System.out.println("No missile loaded.");
        }
    }

    // ミサイルの更新
    public void updateMissileLauncher() {
        reloadMissile();
        updateMissileList();

        // レーダーの位置と向きを更新
        searchRadar.setAngle(0);
        searchRadar.update("CLOCKWISE", x, y);
        searchRadar.scanForReflectors();

        trackRadar.setAngle(launcherToTargetAngle);
        trackRadar.update("Track", x, y);
        trackRadar.scanForReflectors();
    }

    private void reloadMissile() {
        if (!isLoaded) {
            if (missileCount < reloadTime) {
                missileCount++;
            } else {
                isLoaded = true;
            }
        }
    }

    private void updateMissileList() {
        synchronized (missiles) {
            Iterator<Missile> iterator = missiles.iterator();
            while (iterator.hasNext()) {
                Missile missile = iterator.next();
                missile.update();
                if (missile.isExpired()) {
                    iterator.remove();
                }
            }
        }
    }

    // 発射台とミサイルの描画
    public void draw(Graphics g, double targetX, double targetY) {
        // レーダーを描画
        searchRadar.draw(g);
        trackRadar.draw(g);
        updateLauncherAngle(targetX, targetY);
        drawLauncher(g);
        drawMissiles(g);
    }

    private void updateLauncherAngle(double targetX, double targetY) {
        launcherToTargetAngle = Math.atan2((targetY - y), (targetX - x));
    }

    private void drawLauncher(Graphics g) {
        g.setColor(LAUNCHER_COLOR);
        g.drawRect((int) x - LAUNCHER_WIDTH / 2, (int) y - LAUNCHER_HEIGHT / 2, LAUNCHER_WIDTH, LAUNCHER_HEIGHT);

        // SACLOS誘導時LOS描画
        double drawLauncherArmLength = LAUNCHER_ARM_LENGTH;
        if (navigationMode.equals("SACLOS")) {
            drawLauncherArmLength *= 1000;
        }
        g.drawLine((int) x, (int) y,
                (int) (x + drawLauncherArmLength * Math.cos(launcherToTargetAngle)),
                (int) (y + drawLauncherArmLength * Math.sin(launcherToTargetAngle)));
    }

    private void drawMissiles(Graphics g) {
        synchronized (missiles) {
            for (Missile missile : missiles) {
                missile.draw(g);
            }
        }
    }

    // ミサイルのモード設定
    public void setMissileMode() {
        if (navigationMode.equals("PPN")) {
            navigationMode = "PN";
        } else if (navigationMode.equals("PN")) {
            navigationMode = "MPN";
        } else if (navigationMode.equals("MPN")) {
            navigationMode = "SACLOS";
        } else if (navigationMode.equals("SACLOS")) {
            navigationMode = "PPN";
        }
    }

    // ゲッターメソッド
    public boolean isReadyToLaunch() {
        return isLoaded;
    }

    public int getMissileCount() {
        return missileCount;
    }

    public String getMissileMode() {
        return navigationMode;
    }

    public double getLauncherToTargetAngle() {
        return launcherToTargetAngle;
    }

    public double distanceFromMissileLauncher(double targetX, double targetY) {
        return Math.sqrt(
                Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));
    }

    public List<Missile> getMissiles() {
        return missiles;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
