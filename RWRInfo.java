// RWR情報保持クラス

public class RWRInfo {
    // フィールド
    private String id;
    private String radarMode;
    private String detectionTargetType;
    private double x;
    private double y;

    // コンストラクタ
    public RWRInfo(Radar radar) {
        this.id = radar.getRadarID();
        this.radarMode = radar.getRadarMode();
        this.detectionTargetType = radar.getDetectedTargetType();
        this.x = radar.getX();
        this.y = radar.getY();
    }

    // RWR情報の更新
    public void updateRWRInfo(Radar radar) {
        this.id = radar.getRadarID();
        this.radarMode = radar.getRadarMode();
        this.detectionTargetType = radar.getDetectedTargetType();
        this.x = radar.getX();
        this.y = radar.getY();
    }

    // ゲッターメソッド
    public String getID() {
        return id;
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
