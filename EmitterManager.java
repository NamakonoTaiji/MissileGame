// 熱源を管理するクラス

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmitterManager {
    // フィールド
    private List<Emitter> emitters;

    // コンストラクタ
    public EmitterManager() {
        this.emitters = Collections.synchronizedList(new ArrayList<>());
    }

    // 熱源の追加
    public void addEmitter(Emitter emitter) {
        emitters.add(emitter);
    }

    // 熱源の削除
    public void removeEmitter(Emitter emitter) {
        synchronized (emitters) {
            emitters.remove(emitter);
        }
    }

    // 熱源のリストを取得
    public List<Emitter> getEmitters() {
        return emitters;
    }

    // 熱源の更新/削除
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
