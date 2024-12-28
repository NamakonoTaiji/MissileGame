import javax.swing.*;
import java.awt.*;

public class ArcExample extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // アンチエイリアシングを有効にする
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 円弧を描画
        g2d.setColor(Color.BLUE);
        g2d.drawArc(0, 0, 100, 100, 0, 90);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Arc Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ArcExample());
        frame.setSize(200, 200);
        frame.setVisible(true);
    }
}
