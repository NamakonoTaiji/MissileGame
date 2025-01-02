import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

public class SmokeTrailManager {
    private List<SmokeTrail> smokeTrails;

    public SmokeTrailManager() {
        smokeTrails = new LinkedList<>();
    }

    public void addSmokeTrail(SmokeTrail smokeTrail) {
        smokeTrails.add(smokeTrail);
    }

    public void update() {
        Iterator<SmokeTrail> iterator = smokeTrails.iterator();
        while (iterator.hasNext()) {
            SmokeTrail smokeTrail = iterator.next();
            smokeTrail.update();
        }
    }

    public void draw(Graphics2D g2d) {
        for (SmokeTrail smokeTrail : smokeTrails) {
            smokeTrail.draw(g2d);
        }
    }
}
