public class MathUtils {
    public static final double PI = Math.PI;

    // クランプ関数: 値をminとmaxの範囲内に収める
    public static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    // 角度正規化関数: 360度をまたぐ二つの角度の差も調べることができる
    public static double normalizeAngle(double radian1, double radian2) {
        return ((radian1 - radian2 + PI * 3) % (PI * 2) - PI);
    }
}
