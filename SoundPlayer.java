import javax.sound.sampled.*;
import java.io.File;

public class SoundPlayer {
    private static Clip engineClip;
    private static Clip rwrClip;

    public static void playSound(String soundFileName, float volume, boolean loop) {
        try {
            File soundFile = new File(soundFileName);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            // 音量を調整
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(volume); // 音量をデシベル単位で設定
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playEngineSound(String soundFileName, float volume, boolean loop) {
        try {
            if (engineClip != null && engineClip.isRunning()) {
                engineClip.stop();
                engineClip.close();
            }

            File soundFile = new File(soundFileName);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            engineClip = AudioSystem.getClip();
            engineClip.open(audioInputStream);

            // 音量を調整
            FloatControl volumeControl = (FloatControl) engineClip.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(volume); // 音量をデシベル単位で設定

            if (loop) {
                engineClip.loop(Clip.LOOP_CONTINUOUSLY);
            }

            engineClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopEngineSound() {
        if (engineClip != null && engineClip.isRunning()) {
            engineClip.stop();
            engineClip.close();
        }
    }

    public static void playRWRLockSound(float volume) {

        try {
            if (rwrClip == null || !rwrClip.isRunning()) {
                File soundFile = new File("sounds/RWR/radar_lock.wav");
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                rwrClip = AudioSystem.getClip();
                rwrClip.open(audioInputStream);

                // 音量を調整
                FloatControl volumeControl = (FloatControl) rwrClip.getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(volume); // 音量をデシベル単位で設定
                rwrClip.loop(Clip.LOOP_CONTINUOUSLY);
                rwrClip.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void stopRWRSound() {
        if (rwrClip != null && rwrClip.isRunning()) {
            rwrClip.stop();
            rwrClip.close();
        }
    }
}
