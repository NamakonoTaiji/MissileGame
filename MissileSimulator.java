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
    public static final int PANEL_WIDTH = 1600;
    public static final int PANEL_HEIGHT = 900;
    private double scale = 1.0;

    private Player player;
    private MissileLauncher missileLauncher;
    private EmitterManager emitterManager;
    private FlareManager flareManager;

    private JLabel coordinatesLabel;
    private JLabel angleLabel;
    private JLabel memoryLabel;
    private JLabel missileModeLabel;
    private JLabel debugLabel;

    private Timer timer;

    public MissileSimulator() {
        setFocusable(true);
        addKeyListener(this);

        emitterManager = new EmitterManager();

        player = new Player(200.0, 200.0, 0.4, 0.003, emitterManager, scale); // EmitterManagerを渡す
        emitterManager.addEmitter(player);

        missileLauncher = new MissileLauncher(150, 150, 0.0, 30, emitterManager, player);
        flareManager = new FlareManager(emitterManager); // FlareManagerの初期化時にEmitterManagerを渡す

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
                repaint();
            }
        }, 0, 3);

        // ラベルの作成
        coordinatesLabel = new JLabel();
        coordinatesLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        coordinatesLabel.setBounds(10, 10, 300, 30);
        add(coordinatesLabel);

        angleLabel = new JLabel();
        angleLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        angleLabel.setBounds(10, 30, 200, 30);
        add(angleLabel);

        memoryLabel = new JLabel();
        memoryLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        memoryLabel.setBounds(10, 50, 300, 30);
        add(memoryLabel);

        missileModeLabel = new JLabel();
        missileModeLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        missileModeLabel.setBounds(10, 70, 300, 30);
        add(missileModeLabel);

        debugLabel = new JLabel();
        debugLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        debugLabel.setBounds(10, 90, 900, 30);
        add(debugLabel);

        setLayout(null);
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
                    System.out.println("Hit detected!");
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

        g.setColor(new Color(170, 170, 170));
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        player.draw(g2d);
        missileLauncher.draw(g2d, player.getX(), player.getY());

        g2d.setTransform(originalTransform);

        g.setColor(Color.GRAY);
        coordinatesLabel.setText(String.format("Coordinates: (%.2f, %.2f)", player.getX(), player.getY()));
        double launcherToTargetAngle = missileLauncher.getLauncherToTargetAngle();
        angleLabel.setText(String.format("Angle: %.2f", Math.toDegrees(launcherToTargetAngle)));
        memoryLabel.setText(String.format("Memory Usage: %,d KB", getMemoryUsage()));
        missileModeLabel.setText(String.format("Navigation: " + missileLauncher.getMissileMode()));
        debugLabel.setText(String.format("Emitters: " + emitterManager.getEmitters().size()));
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
        frame.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
