// 反射体の管理クラス

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReflectorManager {
    private List<Reflector> reflectors;

    // コンストラクタ
    public ReflectorManager() {
        this.reflectors = Collections.synchronizedList(new ArrayList<>());
    }

    // 反射体の追加
    public void addReflector(Reflector reflector) {
        reflectors.add(reflector);
    }

    // 反射体の削除
    public void removeReflector(Reflector reflector) {
        synchronized (reflectors) {
            reflectors.remove(reflector);
        }
    }

    // 反射体のリストを取得
    public List<Reflector> getReflectors() {
        return reflectors;
    }

    // 反射体の更新/削除
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
