import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RWRManager {
    private String detectionRadarMode = "SWEEP";
    private String detectionTargetType = "None";
    private List<double[]> radarCoordinates = new CopyOnWriteArrayList<>();
    private List<RWRInfo> rwrInfos;
    private double receiverStrength = 0;

    public RWRManager() {
        this.rwrInfos = Collections.synchronizedList(new ArrayList<>());
        ;
    }

    public void updateOrAddRWRInfo(Radar radar) {
        boolean isUpdate = false;
        RWRInfo newRWRInfo = new RWRInfo(radar);
        synchronized (rwrInfos) {
            for (RWRInfo rwrInfo : rwrInfos) {
                if (radar.getDetectedTargetType().equals("Player") && rwrInfo.getID().equals(radar.getRadarID())) {
                    rwrInfo.updateRWRInfo(radar);
                    isUpdate = true;
                    break;
                }
            }

            if (!isUpdate) {
                rwrInfos.add(newRWRInfo);
            }
        }
    }

    public void removeRWRInfo(RWRInfo rwr) {
        synchronized (rwrInfos) {
            rwrInfos.remove(rwr);
        }
    }

    public List<RWRInfo> getRWRInfos() {
        synchronized (rwrInfos) {
            return rwrInfos;
        }
    }

    public String getDetectionRadarMode() {
        return detectionRadarMode;
    }

    public void setDetectionRadarMode(String detectionRadarMode) {
        this.detectionRadarMode = detectionRadarMode;
    }

    public String getDetectionTargetType() {
        return detectionTargetType;
    }

    public List<double[]> getRadarCoordinates() {
        return radarCoordinates;
    }

    public double getReceiverStrength() {
        return receiverStrength;
    }

    public void setReceiverStrength(double receiverStrength) {
        this.receiverStrength += receiverStrength;
    }
}