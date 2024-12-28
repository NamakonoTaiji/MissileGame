import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlareManager {
    private List<Flare> flares;

    public FlareManager() {
        flares = new ArrayList<>();
    }

    public void addFlare(double x, double y, double speed, double angle) {
        flares.add(new Flare(x, y, speed, angle));
    }

    public void updateFlares() {
        Iterator<Flare> iterator = flares.iterator();
        while (iterator.hasNext()) {
            Flare flare = iterator.next();
            flare.update();
            if (flare.isExpired()) {
                iterator.remove(); // フレアの寿命が尽きたらリストから削除
            }
        }
    }

    public void drawFlares(Graphics g) {
        for (Flare flare : flares) {
            flare.draw(g);
        }
    }
}
