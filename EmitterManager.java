import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmitterManager {
    private List<Emitter> emitters;

    public EmitterManager() {
        this.emitters = Collections.synchronizedList(new ArrayList<>());
    }

    public void addEmitter(Emitter emitter) {
        emitters.add(emitter);
    }

    public void removeEmitter(Emitter emitter) {
        emitters.remove(emitter);
    }

    public List<Emitter> getEmitters() {
        return emitters;
    }

    public void updateEmitters() {
        synchronized (emitters) {
            emitters.removeIf(emitter -> {
                if (emitter instanceof Flare) {
                    Flare flare = (Flare) emitter;
                    return flare.isExpired();
                }
                return false;
            });
        }
    }
}
