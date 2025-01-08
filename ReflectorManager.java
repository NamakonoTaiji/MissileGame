import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReflectorManager {
    private List<Reflector> reflectors;

    public ReflectorManager() {
        this.reflectors = Collections.synchronizedList(new ArrayList<>());
    }

    public void addReflector(Reflector reflector) {
        reflectors.add(reflector);
    }

    public void removeReflector(Reflector reflector) {
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
                if (reflector instanceof Chaff) {
                    Chaff chaff = (Chaff) reflector;
                    return chaff.isExpired();
                }
                return false;
            });
        }
    }
}
