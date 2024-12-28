public class MathUtils {

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
}
