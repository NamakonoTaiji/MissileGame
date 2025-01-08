// 対抗手段（フレア）のクラス

import java.awt.*;

public class Flare implements Emitter {

    // 定数
    private static final double AIR_RESISTANCE = 0.995; // 空気抵抗
    private static final double FLARE_EFFECT_SIZE = 2; // フレアの大きさ
    private static final double LIFESPAN = 1000; // フレアの寿命

    // フィールド
    private double infraredEmission = 1.0; // 赤外線放射強度
    private double x;
    private double y;
    private double speed;
    private double angle;
    private int age = 0;

    // コンストラクタ
    public Flare(double x, double y, double speed, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed;
    }

    public void update() {
        // 空気抵抗による減速
        speed = speed * AIR_RESISTANCE;

        // 速度に基づいて移動
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;

        age += 1;
    }

    // 描画メソッド
    public void draw(Graphics g2d) {
        g2d.setColor(new Color(255, 255, 0, 128));
        g2d.fillOval((int) (x - FLARE_EFFECT_SIZE / 2), (int) (y - FLARE_EFFECT_SIZE / 2), (int) FLARE_EFFECT_SIZE,
                (int) FLARE_EFFECT_SIZE);

        g2d.setColor(new Color(255, 255, 0, 64));
        g2d.fillOval((int) (x - FLARE_EFFECT_SIZE * 1.25), (int) (y - FLARE_EFFECT_SIZE * 1.25),
                (int) (FLARE_EFFECT_SIZE * 2.5),
                (int) (FLARE_EFFECT_SIZE * 2.5));

        g2d.setColor(new Color(255, 255, 0, 32));
        g2d.fillOval((int) (x - FLARE_EFFECT_SIZE * 2.7), (int) (y - FLARE_EFFECT_SIZE * 2.7),
                (int) (FLARE_EFFECT_SIZE * 5.4),
                (int) (FLARE_EFFECT_SIZE * 5.4));
    }

    // 寿命が尽きたか否か
    public boolean isExpired() {
        return age >= LIFESPAN;
    }

    // 以下のメソッドはEmitterインターフェースの実装
    @Override
    public String getSourceType() {
        return "Flare";
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
}
