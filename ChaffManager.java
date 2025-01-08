// チャフの管理クラス

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ChaffManager {
    // フィールド
    private List<Chaff> chaffs;
    private ReflectorManager reflectorManager;

    // コンストラクタ
    public ChaffManager(ReflectorManager reflectorManager) {
        this.chaffs = Collections.synchronizedList(new ArrayList<>());
        this.reflectorManager = reflectorManager;
    }

    // チャフの追加
    public void addChaff(double x, double y, double speed, double angle) {
        Chaff chaff = new Chaff(x, y, speed, angle);
        chaffs.add(chaff);
        reflectorManager.addReflector(chaff);
    }

    // チャフの更新/削除
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

    // チャフの描画
    public void drawChaffs(Graphics g) {
        synchronized (chaffs) {
            for (Chaff chaff : chaffs) {
                chaff.draw(g);
            }
        }
    }

    // チャフの総数を取得
    public int getChaffSize() {
        return chaffs.size();
    }
}
