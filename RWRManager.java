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

    public synchronized void updateOrAddRWRInfo(Radar radar) {
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

    public synchronized void removeRWRInfo(RWRInfo rwr) {
        synchronized (rwrInfos) {
            rwrInfos.remove(rwr);
        }
    }

    public List<RWRInfo> getRWRInfos() {
        synchronized (rwrInfos) {
            return rwrInfos;
        }
    }

    public void setDetectionRadarMode(String detectionRadarMode) {
        this.detectionRadarMode = detectionRadarMode;
    }

    public double getReceiverStrength() {
        return receiverStrength;
    }

    public void setReceiverStrength(double receiverStrength) {
        this.receiverStrength += receiverStrength;
    }
}