import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundPlayer {
    private static final int SOUND_PLAY_MIN_TIME = 40;

    private static Clip engineClip;
    private static Clip rwrTrackClip;
    private static Clip rwrContactClip;
    private static Clip rwrLaunchAlertClip;
    private static Clip rwrMissileIncomingClip;
    private static int rwrSoundPlayTime = SOUND_PLAY_MIN_TIME;
    private static boolean isLaunch = false;
    private static boolean isTrack = false;
    private static boolean isContact = false;
    private static boolean isLaunchSoundPlayed = false;
    private static boolean isTrackSoundPlayed = false;
    private static boolean isContactSoundPlayed = false;

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

    public static synchronized void playEngineSound(String soundFileName, float volume, boolean loop) {
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

    public static synchronized void stopEngineSound() {
        if (engineClip != null && engineClip.isRunning()) {
            engineClip.stop();
            engineClip.close();
        }
    }

    public static synchronized void updateRWRSound() {
        if (rwrSoundPlayTime < SOUND_PLAY_MIN_TIME && isLaunch) {
            playRWRLaunchSound(-5);
        } else if (rwrSoundPlayTime < SOUND_PLAY_MIN_TIME && isTrack) {
            playRWRTrackSound(-5);
        } else if (rwrSoundPlayTime < SOUND_PLAY_MIN_TIME && isContact) {
            playRWRContactSound(-5);
        }

        if (rwrSoundPlayTime >= SOUND_PLAY_MIN_TIME) {
            stopRWRLaunchSound();
            stopRWRTrackSound();
            stopRWRLaunchAlertSound();
            isContactSoundPlayed = false;
        }

        rwrSoundPlayTime++;
    }

    public static synchronized void playRWRSound(float volume, String detectionRadarMode) {
        isLaunch = false;
        isTrack = false;
        isContact = false;
        if (detectionRadarMode.equals("Launch")) {
            isLaunch = true;
        } else if (detectionRadarMode.equals("Track")) {
            isTrack = true;
        } else {
            isContact = true;
        }
        rwrSoundPlayTime = 0;
        updateRWRSound();
    }

    public static synchronized void playRWRLaunchSound(float volume) {
        try {
            // 発射探知警報が再生されていない場合のみ再生
            if (!isLaunchSoundPlayed && (rwrLaunchAlertClip == null || !rwrLaunchAlertClip.isRunning())) {

                File soundFile = new File("sounds/RWR/missile_launch.wav");
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                rwrLaunchAlertClip = AudioSystem.getClip();
                rwrLaunchAlertClip.open(audioInputStream);

                // 音量を調整
                FloatControl volumeControl = (FloatControl) rwrLaunchAlertClip
                        .getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(volume); // 音量をデシベル単位で設定
                rwrLaunchAlertClip.loop(Clip.LOOP_CONTINUOUSLY);
                rwrLaunchAlertClip.start();
                isLaunchSoundPlayed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopRWRTrackSound();
    }

    public static synchronized void playRWRTrackSound(float volume) {
        try {
            // レーダー照射警報が再生されていない場合のみ再生
            if (!isTrackSoundPlayed && (rwrTrackClip == null || !rwrTrackClip.isRunning())) {
                File soundFile = new File("sounds/RWR/radar_lock.wav");
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                rwrTrackClip = AudioSystem.getClip();
                rwrTrackClip.open(audioInputStream);

                // 音量を調整
                FloatControl volumeControl = (FloatControl) rwrTrackClip
                        .getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(volume); // 音量をデシベル単位で設定
                rwrTrackClip.loop(Clip.LOOP_CONTINUOUSLY);
                rwrTrackClip.start();
                isTrackSoundPlayed = true;
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        stopRWRLaunchSound();
    }

    public static synchronized void playRWRContactSound(float volume) {
        try {
            // レーダーコンタクト警報が再生されていない場合のみ再生
            if (!isContactSoundPlayed && (rwrContactClip == null || !rwrContactClip.isRunning())) {
                File soundFile = new File("sounds/RWR/contact.wav");
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                rwrContactClip = AudioSystem.getClip();
                rwrContactClip.open(audioInputStream);

                // 音量を調整
                FloatControl volumeControl = (FloatControl) rwrContactClip
                        .getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(volume); // 音量をデシベル単位で設定
                rwrContactClip.start();
                isContactSoundPlayed = true;
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        stopRWRLaunchSound();
        stopRWRTrackSound();
    }

    public static void stopRWRLaunchSound() {
        if (rwrLaunchAlertClip != null && rwrLaunchAlertClip.isRunning()) {
            rwrLaunchAlertClip.stop();
            rwrLaunchAlertClip.close();
            isLaunchSoundPlayed = false;
        }
    }

    public static void stopRWRTrackSound() {
        if (rwrTrackClip != null && rwrTrackClip.isRunning()) {
            rwrTrackClip.stop();
            rwrTrackClip.close();
            isTrackSoundPlayed = false;
        }
    }

    public static void stopRWRLaunchAlertSound() {
        if (rwrLaunchAlertClip != null && rwrLaunchAlertClip.isRunning()) {
            rwrLaunchAlertClip.stop();
            rwrLaunchAlertClip.close();
        }
    }
}
