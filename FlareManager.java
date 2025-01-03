import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FlareManager {
    private List<Flare> flares;
    private EmitterManager emitterManager;

    public FlareManager(EmitterManager emitterManager) {
        this.flares = Collections.synchronizedList(new ArrayList<>());
        this.emitterManager = emitterManager;
    }

    public void addFlare(double x, double y, double speed, double angle) {
        Flare flare = new Flare(x, y, speed, angle);
        flares.add(flare);
        emitterManager.addEmitter(flare);
    }

    public void updateFlares() {
        synchronized (flares) {
            Iterator<Flare> flareIterator = flares.iterator();
            while (flareIterator.hasNext()) {
                Flare flare = flareIterator.next();
                flare.update();

                if (flare.isExpired()) {
                    flareIterator.remove();
                    emitterManager.removeEmitter(flare);
                }
            }
        }
    }

    public void drawFlares(Graphics g) {
        synchronized (flares) {
            for (Flare flare : flares) {
                flare.draw(g);
            }
        }
    }

    public int getFlareSize() {
        return flares.size();
    }
}
