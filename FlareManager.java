// フレアの管理クラス

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FlareManager {

    // フィールド
    private List<Flare> flares;
    private EmitterManager emitterManager;

    // コンストラクタ
    public FlareManager(EmitterManager emitterManager) {
        this.flares = Collections.synchronizedList(new ArrayList<>());
        this.emitterManager = emitterManager;
    }

    // フレアの追加
    public void addFlare(double x, double y, double speed, double angle) {
        Flare flare = new Flare(x, y, speed, angle);
        flares.add(flare);
        emitterManager.addEmitter(flare);
    }

    // フレアの更新/削除
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

    // フレアの描画
    public void drawFlares(Graphics g) {
        synchronized (flares) {
            for (Flare flare : flares) {
                flare.draw(g);
            }
        }
    }

    // フレアの総数を取得
    public int getFlareSize() {
        return flares.size();
    }
}
