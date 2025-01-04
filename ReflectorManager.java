import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReflectorManager {
    private List<Reflector> reflectors;

    public ReflectorManager() {
        this.reflectors = Collections.synchronizedList(new ArrayList<>());
    }

    public void addReflector(Reflector reflector) {
        System.out.println("Adding reflector");
        reflectors.add(reflector);
    }

    public void removeReflector(Reflector reflector) {
        System.out.println("Removing reflector");
        synchronized (reflectors) {
            reflectors.remove(reflector);
        }
    }

    public List<Reflector> getReflectors() {
        return reflectors;
    }

    public void updateReflectors() {
        synchronized (reflectors) {
            reflectors.removeIf(reflector -> {
                if (reflector instanceof Flare) {
                    Flare flare = (Flare) reflector;
                    return flare.isExpired();
                }
                return false;
            });
        }
    }
}
