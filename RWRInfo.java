
public class RWRInfo {
    private String id;
    private String radarMode;
    private String detectionTargetType;
    private double x;
    private double y;

    public RWRInfo(Radar radar) {
        this.id = radar.getRadarID();
        this.radarMode = radar.getRadarMode();
        this.detectionTargetType = radar.getDetectedTargetType();
        this.x = radar.getX();
        this.y = radar.getY();
    }

    public String getID() {
        return id;
    }

    public void updateRWRInfo(Radar radar) {
        this.id = radar.getRadarID();
        this.radarMode = radar.getRadarMode();
        this.detectionTargetType = radar.getDetectedTargetType();
        this.x = radar.getX();
        this.y = radar.getY();
    }

    public String getRadarMode() {
        return radarMode;
    }

    public String getDetectionTargetType() {
        return detectionTargetType;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
