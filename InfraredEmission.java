import java.util.ArrayList;
import java.util.List;

public class InfraredEmission {
    private double x;
    private double y;
    private double infraredEmission;

    private static List<InfraredEmission> irEmissions = new ArrayList<>();

    public InfraredEmission(double x, double y, double infraredEmission) {
        this.x = x;
        this.y = y;
        this.infraredEmission = infraredEmission;
    }

    // 熱源の生成
    public static void addInfraredEmission(double x, double y, double infraredEmission) {
        irEmissions.add(new InfraredEmission(x, y, infraredEmission));
        System.out.println("infraredEmission added:");
    }

    // リスト内の要素数を取得するメソッド
    public static int getEmissionCount() {
        return irEmissions.size();
    }

    public void update(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getInfraredEmission() {
        return infraredEmission;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
