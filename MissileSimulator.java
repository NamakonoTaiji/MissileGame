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

        player = new Player(200.0, 200.0, 0.4, emitterManager, scale); // EmitterManagerを渡す
        emitterManager.addEmitter(player);

        missileLauncher = new MissileLauncher(150, 150, 0.0, 30, emitterManager, player);
        flareManager = new FlareManager(emitterManager); // FlareManagerの初期化時にEmitterManagerを渡す

        labelManager = new LabelManager(this);

        // ラベルの追加（座標やサイズを変更）
        labelManager.addLabel("Coordinates: ", 10, 10, 300, 30);
        labelManager.addLabel("Angle: ", 10, 50, 200, 30);
        labelManager.addLabel("Memory Usage: ", 10, 90, 300, 30);
        labelManager.addLabel("Navigation: ", 10, 130, 300, 30);
        labelManager.addLabel("Speed: ", 10, 170, 300, 30);
        labelManager.addLabel("DragForce: ", 10, 220, 300, 30);
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
        player.update(missileLauncher.getMissiles(), labelManager);
        missileLauncher.updateMissiles(player.getX(), player.getY());
        flareManager.updateFlares();
        emitterManager.updateEmitters();
        checkCollisions();
    }

    private void checkCollisions() {
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        double launcherToTargetAngle = missileLauncher.getLauncherToTargetAngle();

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform originalTransform = g2d.getTransform();

        // スケールを適用
        g2d.scale(scale, scale);

        // プレイヤーを画面の中央に配置
        double offsetX = (getWidth() / 2.0) / scale - player.getX();
        double offsetY = (getHeight() / 2.0) / scale - player.getY();
        g2d.translate(offsetX, offsetY);

        // グリッドの背景を描画
        int backGroundImageSize = 50;
        g.setColor(new Color(100, 100, 100));
        g.fillRect(this.getWidth() * -backGroundImageSize, this.getHeight() * -backGroundImageSize,
                this.getWidth() * backGroundImageSize * 2, this.getHeight() * backGroundImageSize * 2);

        g2d.setColor(new Color(150, 150, 150));
        int gridSize = 150;
        for (int i = -getWidth() * backGroundImageSize; i < getWidth() * backGroundImageSize; i += gridSize) {
            g2d.drawLine(i, -getHeight() * backGroundImageSize, i, getHeight() * backGroundImageSize);
        }
        for (int j = -getHeight() * backGroundImageSize; j < getHeight() * backGroundImageSize; j += gridSize) {
            g2d.drawLine(-getWidth() * backGroundImageSize, j, getWidth() * backGroundImageSize, j);
        }

        player.draw(g2d);
        missileLauncher.draw(g2d, player.getX(), player.getY());

        g2d.setTransform(originalTransform);

        // ラベルの更新
        labelManager.updateLabel(0, String.format("Coordinates: (%.2f, %.2f)", player.getX(), player.getY()));
        labelManager.updateLabel(1, String.format("Angle: %.2f", Math.toDegrees(launcherToTargetAngle)));
        labelManager.updateLabel(2, String.format("Memory Usage: %,d KB", getMemoryUsage()));
        labelManager.updateLabel(3, String.format("Navigation: " + missileLauncher.getMissileMode()));
        labelManager.updateLabel(4, String.format("Speed: %.1f", player.getSpeed() * 1000));
        labelManager.updateLabel(5, String.format("DragForce: %.6f", player.getDragForce()));
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
