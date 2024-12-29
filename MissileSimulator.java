import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MissileSimulator extends JPanel implements KeyListener {
    public static final int PANEL_WIDTH = 1920;
    public static final int PANEL_HEIGHT = 1080;
    private double scale = 1.0;

    private Player player;
    private MissileLauncher missileLauncher;
    private EmitterManager emitterManager;
    private FlareManager flareManager;
    private LabelManager labelManager;

    private Timer timer;

    public MissileSimulator() {
        setFocusable(true);
        addKeyListener(this);

        emitterManager = new EmitterManager();

        player = new Player(200.0, 200.0, 0.4, 0.003, emitterManager, scale); // EmitterManagerを渡す
        emitterManager.addEmitter(player);

        missileLauncher = new MissileLauncher(150, 150, 0.0, 30, emitterManager, player);
        flareManager = new FlareManager(emitterManager); // FlareManagerの初期化時にEmitterManagerを渡す

        labelManager = new LabelManager(this);

        // ラベルの追加（座標やサイズを変更）
        labelManager.addLabel("Coordinates: ", 10, 10, 300, 30);
        labelManager.addLabel("Angle: ", 10, 50, 200, 30);
        labelManager.addLabel("Memory Usage: ", 10, 90, 300, 30);
        labelManager.addLabel("Navigation: ", 10, 130, 300, 30);
        labelManager.addLabel("Speed: ", 10, 170, 900, 30);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
                repaint();
            }
        }, 0, 3);
    }

    public void update() {
        player.update();
        missileLauncher.updateMissiles(player.getX(), player.getY());
        flareManager.updateFlares();
        emitterManager.updateEmitters();
        checkCollisions();
    }

    private void checkCollisions() {
        List<Missile> missiles = missileLauncher.getMissiles();
        double hitRadius = 6;
        double missileHitRadius = 1.0;

        synchronized (missiles) {
            Iterator<Missile> iterator = missiles.iterator();
            while (iterator.hasNext()) {
                Missile missile = iterator.next();
                double distance = Math.sqrt(
                        Math.pow(missile.getX() - player.getX(), 2) + Math.pow(missile.getY() - player.getY(), 2));
                if (distance < hitRadius + missileHitRadius) {
                    String message = "Hit detected! Missile at (" + missile.getX() + ", " + missile.getY() + ")";
                    System.out.println(message);
                    labelManager.addLogMessage(message);
                    iterator.remove();
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform originalTransform = g2d.getTransform();

        // スケールを適用
        g2d.scale(scale, scale);

        // プレイヤーを画面の中央に配置
        double offsetX = (getWidth() / 2.0) / scale - player.getX();
        double offsetY = (getHeight() / 2.0) / scale - player.getY();
        g2d.translate(offsetX, offsetY);

        g.setColor(new Color(100, 100, 100));
        g.fillRect(this.getWidth() * -10, this.getHeight() * -10, this.getWidth() * 20, this.getHeight() * 20);

        player.draw(g2d);
        missileLauncher.draw(g2d, player.getX(), player.getY());

        g2d.setTransform(originalTransform);

        // ラベルの更新
        labelManager.updateLabel(0, String.format("Coordinates: (%.2f, %.2f)", player.getX(), player.getY()));
        double launcherToTargetAngle = missileLauncher.getLauncherToTargetAngle();
        labelManager.updateLabel(1, String.format("Angle: %.2f", Math.toDegrees(launcherToTargetAngle)));
        labelManager.updateLabel(2, String.format("Memory Usage: %,d KB", getMemoryUsage()));
        labelManager.updateLabel(3, String.format("Navigation: " + missileLauncher.getMissileMode()));
        labelManager.updateLabel(4, String.format("Speed: %.3f", player.getSpeed()));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_UP:
                player.setUpPressed(true);
                break;
            case KeyEvent.VK_LEFT:
                player.setLeftPressed(true);
                break;
            case KeyEvent.VK_RIGHT:
                player.setRightPressed(true);
                break;
            case KeyEvent.VK_SPACE:
                missileLauncher.launchMissile();
                break;
            case KeyEvent.VK_C:
                missileLauncher.setMissileMode();
                break;
            case KeyEvent.VK_Z:
                player.setZPressed(true);
                break;
            case KeyEvent.VK_1:
                scale *= 0.9;
                break;
            case KeyEvent.VK_2:
                scale *= 1.1;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_UP:
                player.setUpPressed(false);
                break;
            case KeyEvent.VK_LEFT:
                player.setLeftPressed(false);
                break;
            case KeyEvent.VK_RIGHT:
                player.setRightPressed(false);
                break;
            case KeyEvent.VK_Z:
                player.setZPressed(false);
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Missile Simulator");
        MissileSimulator simulator = new MissileSimulator();
        frame.add(simulator);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
