import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlareManager {
    private List<Flare> flares;

    public FlareManager() {
        flares = new ArrayList<>();
    }

    // フレアの生成
    public void addFlare(double x, double y, double speed, double angle) {
        flares.add(new Flare(x, y, speed, angle));
    }

    // フレアの更新
    public void updateFlares() {
        Iterator<Flare> iterator = flares.iterator();
        while (iterator.hasNext()) {
            Flare flare = iterator.next();
            flare.update();
            if (flare.isExpired()) {
                iterator.remove(); // フレアの寿命が尽きたらリストから削除
            }
        }
    }

    // フレアの描画
    public void drawFlares(Graphics g) {
        for (Flare flare : flares) {
            flare.draw(g);
        }
    }

    // フレアの赤外線放出取得
    /*
     * public List<InfraredEmission> getFlareEmissions() {
     * List<InfraredEmission> emissions = new ArrayList<>();
     * synchronized (flares) {
     * for (Flare flare : flares) {
     * emissions.add(flare.getInfraredEmission());
     * }
     * }
     * return emissions;
     * }
     */

    // 内部クラスとしてFlareを定義
    private class Flare {
        private double infraredEmission = 1.0;
        private double airResistance = 0.95;
        private double x;
        private double y;
        private double speed;
        private double angle;
        private int age = 0;
        private final double LIFESPAN = 400;

        public Flare(double x, double y, double speed, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.speed = speed;
        }

        public void update() {
            // 空気抵抗による減速
            speed = speed * airResistance;

            // 速度に基づいて移動
            x += Math.cos(angle) * speed;
            y += Math.sin(angle) * speed;

            age += 1;
        }

        public void draw(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            // フレアを描画
            g2d.setColor(Color.YELLOW);
            g2d.drawOval((int) (x), (int) (y), 3, 3);
        }

        public boolean isExpired() {
            return age >= LIFESPAN;
        }

        /*
         * public InfraredEmission getInfraredEmission() {
         * return new InfraredEmission(infraredEmission, x, y);
         * }
         */

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
}
