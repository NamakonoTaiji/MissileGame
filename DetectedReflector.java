public class DetectedReflector {
    private double x;
    private double y;
    private double reflectionStrength;

    public DetectedReflector(double x, double y, double reflectionStrength) {
        this.x = x;
        this.y = y;
        this.reflectionStrength = reflectionStrength;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getReflectanceStrength() {
        return reflectionStrength;
    }
}