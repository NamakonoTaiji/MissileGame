import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Player implements HeatSource {
    private double x;
    private double y;
    private double infraredEmission;
    private String sourceType = "player";
    private BufferedImage playerImage;

    public Player(double x, double y, double infraredEmission) {
        this.x = x;
        this.y = y;
        this.infraredEmission = infraredEmission;

        // 画像を読み込む
        try {
            playerImage = ImageIO.read(new File("src/resources/player.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @Override
    public String getSourceType() {
        return sourceType;
    }

    public void draw(Graphics2D g2d) {
        if (playerImage != null) {
            // 画像の中心をプレイヤーの座標に合わせて描画
            g2d.drawImage(playerImage, (int) (x - playerImage.getWidth() / 2), (int) (y - playerImage.getHeight() / 2),
                    null);
        } else {
            // 画像が読み込めなかった場合、プレースホルダーを描画
            g2d.setColor(Color.BLUE);
            g2d.fillOval((int) (x - 10), (int) (y - 10), 20, 20);
        }
    }
}
