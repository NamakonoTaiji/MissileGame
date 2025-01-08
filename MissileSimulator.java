// メインクラス

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class MissileSimulator extends JPanel implements KeyListener {

    // 定数
    public static final int PANEL_WIDTH = 1920;
    public static final int PANEL_HEIGHT = 1080;
    private static final double SCALE_STEP = 0.1;
    private static final double INITIAL_SCALE = 1.0;
    private static final int TIMER_INTERVAL = 3;

    // フィールド
    private double scale; // 表示倍率

    private Player player;
    private MissileLauncher missileLauncher;
    private EmitterManager emitterManager;
    private ReflectorManager reflectorManager;
    private FlareManager flareManager;
    private ChaffManager chaffManager;
    private LabelManager labelManager;
    private Timer timer;
    private List<IrMissile> irMissiles;
    private List<ARHMissile> arhMissiles;
    private SmokeTrail smokeTrail;
    private RWRManager rwrManager;
    private CollisionDetector collisionDetector;

    // コンストラクタ
    public MissileSimulator() {
        initializeComponents();
        initializeLabels();
        setupTimer();
    }

    // 初期化メソッド
    private void initializeComponents() {
        setFocusable(true);
        addKeyListener(this);

        scale = INITIAL_SCALE;
        rwrManager = new RWRManager();
        emitterManager = new EmitterManager();
        reflectorManager = new ReflectorManager();
        player = new Player(200.0, 200.0, 0.9, emitterManager, reflectorManager, scale, rwrManager);
        emitterManager.addEmitter(player);
        reflectorManager.addReflector(player);

        missileLauncher = new MissileLauncher("SAM1", 150, 150, 0.0, 100, emitterManager, reflectorManager, player,
                rwrManager);
        flareManager = new FlareManager(emitterManager);
        chaffManager = new ChaffManager(reflectorManager);
        labelManager = new LabelManager(this);

        // ミサイルクラスを抽象化した場合工事予定
        irMissiles = Collections.synchronizedList(new ArrayList<>());
        this.collisionDetector = new CollisionDetector(6, irMissiles);
        smokeTrail = new SmokeTrail();
    }

    // ラベルの初期化
    private void initializeLabels() {
        labelManager.addLabel("Coordinates: ", 10, 10, 300, 30);
        labelManager.addLabel("Angle: ", 10, 50, 200, 30);
        labelManager.addLabel("Memory Usage: ", 10, 90, 300, 30);
        labelManager.addLabel("Navigation: ", 10, 130, 300, 30);
        labelManager.addLabel("Speed: ", 10, 170, 300, 30);
        labelManager.addLabel("DragForce: ", 10, 220, 300, 30);
    }

    // タイマーの設定
    private void setupTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
                repaint();
            }
        }, 0, TIMER_INTERVAL);
    }

    // 更新メソッド
    public void update() {
        irMissiles = missileLauncher.getIrMissiles();
        arhMissiles = missileLauncher.getArhMissiles();
        player.update(labelManager);
        missileLauncher.updateMissileLauncher();
        flareManager.updateFlares();
        chaffManager.updateChaffs();
        emitterManager.updateEmitters();
        reflectorManager.updateReflectors();
        smokeTrail.update();
        SoundPlayer.updateRWRSound();
        generateSmokeTrail();
        collisionDetector.checkCollisions(player.getX(), player.getY(), missileLauncher.getIrMissiles(),
                missileLauncher.getArhMissiles(), labelManager);
    }

    // 煙の生成
    private void generateSmokeTrail() {
        synchronized (irMissiles) {
            for (IrMissile irMissile : irMissiles) {
                double smokeAddDelay = MathUtils.clamp(-3.5 * irMissile.getSpeed() + 10, 2, 60);
                if (irMissile.getAge() - irMissile.getSmokeAddTime() >= smokeAddDelay
                        && irMissile.getAge() <= IrMissile.BURN_TIME_OF_BOOSTER) {
                    smokeTrail.addParticle(irMissile.getX(), irMissile.getY());
                    irMissile.setSmokeAddTime(irMissile.getAge());
                }
            }
        }

        synchronized (arhMissiles) {
            for (ARHMissile arhMissile : arhMissiles) {
                double smokeAddDelay = MathUtils.clamp(-3.5 * arhMissile.getSpeed() + 10, 2, 60);
                if (arhMissile.getAge() - arhMissile.getSmokeAddTime() >= smokeAddDelay
                        && arhMissile.getAge() <= ARHMissile.BURN_TIME_OF_BOOSTER) {
                    smokeTrail.addParticle(arhMissile.getX(), arhMissile.getY());
                    arhMissile.setSmokeAddTime(arhMissile.getAge());
                }
            }
        }
    }

    // 描画メソッド
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform originalTransform = g2d.getTransform();
        g2d.scale(scale, scale);

        centerPlayer(g2d);

        drawBackgroundGrid(g2d);
        smokeTrail.draw(g);
        player.draw(g2d);
        missileLauncher.draw(g2d, player.getX(), player.getY());

        g2d.setTransform(originalTransform);
        updateLabels();
    }

    // プレイヤーを中心に描画
    private void centerPlayer(Graphics2D g2d) {
        double offsetX = (getWidth() / 2.0) / scale - player.getX();
        double offsetY = (getHeight() / 2.0) / scale - player.getY();
        g2d.translate(offsetX, offsetY);
    }

    // 背景グリッドの描画
    private void drawBackgroundGrid(Graphics2D g2d) {
        int backGroundImageSize = 50;
        g2d.setColor(new Color(100, 100, 100));
        g2d.fillRect(-backGroundImageSize * getWidth(), -backGroundImageSize * getHeight(),
                backGroundImageSize * 2 * getWidth(), backGroundImageSize * 2 * getHeight());

        g2d.setColor(new Color(150, 150, 150, 50));
        int gridSize = 150;
        for (int i = -getWidth() * backGroundImageSize; i < getWidth() * backGroundImageSize; i += gridSize) {
            g2d.drawLine(i, -getHeight() * backGroundImageSize, i, getHeight() * backGroundImageSize);
        }
        for (int j = -getHeight() * backGroundImageSize; j < getHeight() * backGroundImageSize; j += gridSize) {
            g2d.drawLine(-getWidth() * backGroundImageSize, j, getWidth() * backGroundImageSize, j);
        }
    }

    // ラベルの更新
    private void updateLabels() {
        double launcherToTargetAngle = missileLauncher.getLauncherToTargetAngle();
        labelManager.updateLabel(0, String.format("Coordinates: (%.2f, %.2f)", player.getX(), player.getY()));
        labelManager.updateLabel(1, String.format("Angle: %.2f", Math.toDegrees(launcherToTargetAngle)));
        labelManager.updateLabel(2, String.format("Memory Usage: %,d KB", getMemoryUsage()));
        labelManager.updateLabel(3, String.format("Navigation: " + missileLauncher.getMissileMode()));
        labelManager.updateLabel(4, String.format("Speed: %.1f", player.getSpeed() * 1000));
        labelManager.updateLabel(5, String.format("DragForce: %.6f", player.getDragForce()));
    }

    // キーイベント処理
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
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
                scale *= 1 - SCALE_STEP;
                break;
            case KeyEvent.VK_2:
                scale *= 1 + SCALE_STEP;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
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
        // Do nothing
    }

    // メモリ使用量の取得
    public long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024;
    }

    // メインメソッド
    public static void main(String[] args) {
        JFrame frame = new JFrame("Missile Simulator");
        MissileSimulator simulator = new MissileSimulator();
        frame.add(simulator);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
