// 熱源（赤外線放出源）のインターフェース

public interface Emitter {
    String getSourceType();

    double getX();

    double getY();

    double getInfraredEmission();

}
