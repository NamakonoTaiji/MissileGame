import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ChaffManager {
    private List<Chaff> chaffs;
    private ReflectorManager reflectorManager;

    public ChaffManager(ReflectorManager reflectorManager) {
        this.chaffs = Collections.synchronizedList(new ArrayList<>());
        this.reflectorManager = reflectorManager;
    }

    public void addChaff(double x, double y, double speed, double angle) {
        Chaff chaff = new Chaff(x, y, speed, angle);
        chaffs.add(chaff);
        reflectorManager.addReflector(chaff);
    }

    public void updateChaffs() {
        synchronized (chaffs) {
            Iterator<Chaff> chaffIterator = chaffs.iterator();
            while (chaffIterator.hasNext()) {
                Chaff chaff = chaffIterator.next();
                chaff.update();

                if (chaff.isExpired()) {
                    chaffIterator.remove();
                    reflectorManager.removeReflector(chaff);
                }
            }
        }
    }

    public void drawChaffs(Graphics g) {
        synchronized (chaffs) {
            for (Chaff chaff : chaffs) {
                chaff.draw(g);
            }
        }
    }

    public int getChaffSize() {
        return chaffs.size();
    }
}
