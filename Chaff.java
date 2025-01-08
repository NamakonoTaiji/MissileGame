// 対抗手段（チャフ）のクラス

import java.awt.*;

public class Chaff implements Reflector {
    // 定数
    private static final double REFLECTOR_STRENGTH = 1.0; // 反射強度
    private static final double CHAFF_SIZE = 2; // チャフの大きさ
    private static final int LIFESPAN = 2000; // チャフの寿命
    private static final double AIR_RESISTANCE = 0.991; // 空気抵抗

    // フィールド
    private double x;
    private double y;
    private double speed;
    private double angle;
    private int age = 0;

    // コンストラクタ
    public Chaff(double x, double y, double speed, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed;
    }

    // 更新メソッド
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
        g2d.setColor(new Color(177, 177, 177, 228));
        g2d.fillOval((int) (x - CHAFF_SIZE / 2), (int) (y - CHAFF_SIZE / 2), (int) CHAFF_SIZE,
                (int) CHAFF_SIZE);

        g2d.setColor(new Color(177, 177, 177, 124));
        g2d.fillOval((int) (x - CHAFF_SIZE * 1.25), (int) (y - CHAFF_SIZE * 1.25),
                (int) (CHAFF_SIZE * 2.5),
                (int) (CHAFF_SIZE * 2.5));
    }

    // 寿命が尽きたか否か
    public boolean isExpired() {
        return age >= LIFESPAN;
    }

    // 以下のメソッドはReflectorインターフェースの実装
    @Override
    public String getSourceType() {
        return "Chaff";
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
    public double getReflectanceStrength() {
        return REFLECTOR_STRENGTH;
    }
}
