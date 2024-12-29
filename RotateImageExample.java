import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class RotateImageExample extends JPanel {
    private BufferedImage image;
    private double angle = 0;

    public RotateImageExample() {
        // 画像を読み込む
        try {
            image = ImageIO.read(new File("images/F-2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // タイマーを使って定期的に画像を回転
        Timer timer = new Timer(100, e -> {
            angle += Math.toRadians(5); // 回転角度を5度ずつ増加
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform originalTransform = g2d.getTransform();

        if (image != null) {
            AffineTransform transform = new AffineTransform();
            // 画像の中心に移動
            transform.translate(getWidth() / 2, getHeight() / 2);
            // 画像の中心を基準に回転
            transform.rotate(angle);
            // 画像の左上隅を原点に合わせる
            transform.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
            g2d.setTransform(transform);

            // 四角形を描画（画像の中心を基準に回転）
            g2d.setColor(Color.BLUE);
            g2d.drawRect(0, 0, 100, 100); // 画像の左上を基準に描画

            // 画像を描画
            g2d.drawImage(image, 0, 0, null);

            // 回転の中心に赤い点を描画
            g2d.setColor(Color.RED);
            g2d.fillOval(-3, -3, 6, 6); // 中心に赤い点を描画
        } else {
            // 画像が読み込めなかった場合の処理
            g2d.setColor(Color.RED);
            g2d.fillRect(getWidth() / 2 - 25, getHeight() / 2 - 25, 50, 50);
        }

        // 元の変換を復元
        g2d.setTransform(originalTransform);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Rotate Image Example");
        RotateImageExample panel = new RotateImageExample();
        frame.add(panel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
