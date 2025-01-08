// レーダーのクラス

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Radar {
    // フィールド
    private double radarStrength = 30; // レーダー出力強度
    private double radarFOV = Math.toRadians(5); // レーダー視野角
    private double radarRange; // レーダー範囲
    private double radarAngleMax = Math.toRadians(50); // レーダー最大角度
    private double angle; // レーダーの角度
    private double currentAngle = 0; // 現在の角度
    private double radarSweepSpeed; // レーダーのスイープ速度
    private double x; // レーダーのX座標
    private double y; // レーダーのY座標
    private String radarMode = "SWEEP"; // レーダーモード
    private String team; // チーム
    private String detectionTargetType = "None"; // 検出対象タイプ
    private String id; // レーダーID
    private boolean IFF; // 識別友軍
    private boolean sweepingRight = true; // スイープ方向

    private final ReflectorManager reflectorManager; // 反射体マネージャー
    private RWRManager rwrManager; // RWRマネージャー

    // コンストラクタ
    public Radar(String id, String team, String radarMode, boolean IFF, ReflectorManager reflectorManager, double x,
            double y, double angle, RWRManager rwrManager, double radarRange, double radarSweepSpeed) {
        this.team = team;
        this.IFF = IFF;
        this.reflectorManager = reflectorManager;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.radarMode = radarMode;
        this.id = id;
        this.rwrManager = rwrManager;
        this.radarRange = radarRange;
        this.radarSweepSpeed = radarSweepSpeed;
    }

    // レーダーの更新
    public void update(String radarMode, double x, double y) {
        this.x = x;
        this.y = y;
        switch (radarMode) {
            case "SWEEP" -> updateSweepMode();
            case "CLOCKWISE" -> updateClockwiseMode();
            case "Track" -> updateTrackOrLaunchMode(x, y);
            case "Launch" -> updateTrackOrLaunchMode(x, y);
        }
    }

    // レーダー角度の更新（スイープモード）
    private void updateSweepMode() {
        if (sweepingRight) {
            currentAngle += radarSweepSpeed;
            if (currentAngle >= radarAngleMax) {
                sweepingRight = false;
            }
        } else {
            currentAngle -= radarSweepSpeed;
            if (currentAngle <= -radarAngleMax) {
                sweepingRight = true;
            }
        }
    }

    // レーダー角度の更新（時計回りモード）
    private void updateClockwiseMode() {
        currentAngle = MathUtils.normalizeAngle(currentAngle - radarSweepSpeed, 0);
    }

    // レーダー角度の更新（トラックまたは発射モード）
    private void updateTrackOrLaunchMode(double x, double y) {
        XYCoordinate targetXYCoordinate = getStrongestReflectXYCoordinate(x, y);
        if (targetXYCoordinate.x != 0) {
            double dx = targetXYCoordinate.x - this.x;
            double dy = targetXYCoordinate.y - this.y;
            currentAngle = MathUtils.normalizeAngle(angle, Math.atan2(dy, dx));
        }
    }

    // 視野角内の反射体を出力
    public List<DetectedReflector> scanForReflectors() {
        List<DetectedReflector> detectedReflectors = new ArrayList<>();
        List<Reflector> reflectors = reflectorManager.getReflectors();

        for (Reflector reflector : reflectors) {
            if (isReflectorInFOV(reflector)) {
                detectedReflectors.add(
                        new DetectedReflector(reflector.getX(), reflector.getY(), reflector.getReflectanceStrength()));
                handlePlayerReflector(reflector);
            }
        }

        removeOutOfRangeRWRInfos();
        return detectedReflectors;
    }

    // 反射体が視野角内にあるか判定
    private boolean isReflectorInFOV(Reflector reflector) {
        double dx = reflector.getX() - this.x;
        double dy = reflector.getY() - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double angleToReflector = Math.atan2(dy, dx);

        return distance <= radarRange
                && Math.abs(MathUtils.normalizeAngle(angleToReflector,
                        MathUtils.normalizeAngle(angle, this.currentAngle))) <= radarFOV / 2;
    }

    // プレイヤーのRWR警報を鳴らす
    private void handlePlayerReflector(Reflector reflector) {
        if ("Player".equals(reflector.getSourceType())) {
            if (rwrManager != null) {
                SoundPlayer.playRWRSound(0, radarMode);
                rwrManager.setReceiverStrength(radarStrength);
                rwrManager.updateOrAddRWRInfo(this);
            }
        }
    }

    // レーダー範囲外のRWR情報を削除
    private void removeOutOfRangeRWRInfos() {
        List<RWRInfo> rwrInfos = new CopyOnWriteArrayList<>(rwrManager.getRWRInfos());
        for (RWRInfo rwrInfo : rwrInfos) {
            if (isRWRInfoOutOfRange(rwrInfo)) {
                if (rwrInfo.getID().equals(this.id)) {
                    rwrManager.removeRWRInfo(rwrInfo);
                }
            }
        }
    }

    // RWR情報が範囲外にあるか判定
    private boolean isRWRInfoOutOfRange(RWRInfo rwrInfo) {
        double rwrDx = rwrInfo.getX() - this.x;
        double rwrDy = rwrInfo.getY() - this.y;
        double rwrDistance = Math.sqrt(rwrDx * rwrDx + rwrDy * rwrDy);
        double rwrAngleToReflector = Math.atan2(rwrDy, rwrDx);

        return rwrDistance > radarRange
                || Math.abs(MathUtils.normalizeAngle(rwrAngleToReflector,
                        MathUtils.normalizeAngle(angle, this.currentAngle))) > radarFOV / 2;
    }

    // 最も強い反射体の座標を取得
    public XYCoordinate getStrongestReflectXYCoordinate(double x, double y) {
        List<DetectedReflector> detectedReflectors = scanForReflectors();
        double maxStrength = 0;
        XYCoordinate strongestXYCoordinate = new XYCoordinate(0, 0);

        for (DetectedReflector reflector : detectedReflectors) {
            double dx = reflector.getX() - x;
            double dy = reflector.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double strength = reflector.getReflectanceStrength() / distance;

            if (strength > maxStrength) {
                maxStrength = strength;
                strongestXYCoordinate = new XYCoordinate(reflector.getX(), reflector.getY());
            }
        }
        return strongestXYCoordinate;
    }

    // レーダーの描画
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // レーダーの視野角を描画
        double fovLeft = MathUtils.normalizeAngle(angle - currentAngle, -radarFOV / 2);
        double fovRight = MathUtils.normalizeAngle(angle - currentAngle, radarFOV / 2);
        g2d.setColor(new Color(0, 255, 0, 10)); // 半透明の緑色
        g2d.fillPolygon(
                new int[] { (int) x, (int) (x + Math.cos(fovLeft) * radarRange),
                        (int) (x + Math.cos(fovRight) * radarRange) },
                new int[] { (int) y, (int) (y + Math.sin(fovLeft) * radarRange),
                        (int) (y + Math.sin(fovRight) * radarRange) },
                3);
    }

    // ゲッター,セッターメソッド
    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void setRWR(RWRManager rwr) {
        this.rwrManager = rwr;
    }

    public double getRange() {
        return radarRange;
    }

    public String getDetectionTargetType() {
        return detectionTargetType;
    }

    public String getDetectionRadarMode() {
        return radarMode;
    }

    public String getRadarID() {
        return id;
    }

    public String getRadarMode() {
        return radarMode;
    }

    public String getDetectedTargetType() {
        return detectionTargetType;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}