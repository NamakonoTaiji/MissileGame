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
        System.out.println("Flare added: (" + x + ", " + y + ") with emission: " + flare.getInfraredEmission());
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
                    System.out.println("Flare and corresponding InfraredEmission removed");
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

    private class Flare implements Emitter {
        private double infraredEmission = 1.0;
        private double airResistance = 0.95;
        private double x;
        private double y;
        private double speed;
        private double angle;
        private int age = 0;
        private final double LIFESPAN = 400;

        public Flare(double x, double y, double speed, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.speed = speed;
        }

        public void update() {
            speed = speed * airResistance;
            x += Math.cos(angle) * speed;
            y += Math.sin(angle) * speed;
            age += 1;
        }

        public void draw(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.YELLOW);
            g2d.drawOval((int) (x), (int) (y), 3, 3);
        }

        public boolean isExpired() {
            return age >= LIFESPAN;
        }

        @Override
        public double getX() {
            return x;
        }

        @Override
        public double getY() {
            return y;
        }

        @Override
        public double getInfraredEmission() {
            return infraredEmission;
        }
    }
}
