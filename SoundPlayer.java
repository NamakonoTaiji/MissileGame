// 音を再生するクラス

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundPlayer {
    // サウンド再生の最小間隔
    private static final int SOUND_PLAY_MIN_TIME = 40;

    // サウンド再生用のクリップ、フィールド
    private static Clip engineClip;
    private static Clip rwrTrackClip;
    private static Clip rwrContactClip;
    private static Clip rwrLaunchAlertClip;
    private static Clip rwrMissileIncomingClip;
    // サウンド再生のための時間管理用のフィールド
    private static int rwrLaunchSoundPlayTime = SOUND_PLAY_MIN_TIME;
    private static int rwrTrackSoundPlayTime = SOUND_PLAY_MIN_TIME;
    private static int rwrContactSoundPlayTime = SOUND_PLAY_MIN_TIME;
    private static int rwrLaunchSoundRequestedTime = SOUND_PLAY_MIN_TIME;
    private static int rwrTrackSoundRequestedTime = SOUND_PLAY_MIN_TIME;
    private static int rwrContactSoundRequestedTime = SOUND_PLAY_MIN_TIME;
    // RWRサウンド再生のためのフラグ
    private static boolean isLaunch = false;
    private static boolean isTrack = false;
    private static boolean isContact = false;

    // サウンド再生メソッド
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

    // エンジンサウンド再生メソッド
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

    // エンジンサウンド停止メソッド
    public static synchronized void stopEngineSound() {
        if (engineClip != null && engineClip.isRunning()) {
            engineClip.stop();
            engineClip.close();
        }
    }

    // RWRサウンド更新メソッド
    public static synchronized void updateRWRSound() {
        if (isLaunch) {
            playRWRLaunchSound(-1);
            rwrLaunchSoundPlayTime++;
        } else if (isTrack) {
            playRWRTrackSound(-1);
            rwrTrackSoundPlayTime++;
        } else if (isContact) {
            playRWRContactSound(-1);
        }

        if (rwrLaunchSoundRequestedTime >= SOUND_PLAY_MIN_TIME) {
            stopRWRLaunchAlertSound();
        }
        if (rwrTrackSoundRequestedTime >= SOUND_PLAY_MIN_TIME) {
            stopRWRTrackSound();
        }

        rwrLaunchSoundRequestedTime++;
        rwrTrackSoundRequestedTime++;
        rwrContactSoundPlayTime++;
    }

    // RWRサウンド再生リクエストメソッド
    public static synchronized void playRWRSound(float volume, String detectionRadarMode) {
        if (detectionRadarMode.equals("Launch")) {
            isLaunch = true;
            rwrLaunchSoundRequestedTime = 0;
        } else if (detectionRadarMode.equals("Track")) {
            rwrTrackSoundRequestedTime = 0;
            isTrack = true;
        } else {
            rwrContactSoundPlayTime = 0;
            isContact = true;
        }
    }

    // -----------------------------------------------------------------------------------
    // 発射探知再生メソッド
    public static synchronized void playRWRLaunchSound(float volume) {
        try {
            // 発射探知警報が再生されていない場合のみ再生
            if (rwrLaunchSoundPlayTime > 10 && (rwrLaunchAlertClip == null || !rwrLaunchAlertClip.isRunning())) {
                rwrLaunchSoundPlayTime = 0;

                System.out.println("Launch");

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
                stopRWRTrackSound();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // レーダー照射音再生メソッド
    public static synchronized void playRWRTrackSound(float volume) {
        try {
            // レーダー照射警報が再生されていない場合のみ再生
            if (rwrTrackSoundPlayTime > 10 && (rwrTrackClip == null || !rwrTrackClip.isRunning())) {
                rwrTrackSoundPlayTime = 0;

                System.out.println("Track");

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
                stopRWRLaunchAlertSound();
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // レーダーコンタクト再生メソッド
    public static synchronized void playRWRContactSound(float volume) {
        try {
            // レーダーコンタクト警報が再生されていない場合のみ再生
            if (rwrContactSoundPlayTime > 40 && (rwrContactClip == null || !rwrContactClip.isRunning())) {
                rwrContactSoundPlayTime = 0;

                System.out.println("Contact");

                File soundFile = new File("sounds/RWR/contact.wav");
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                rwrContactClip = AudioSystem.getClip();
                rwrContactClip.open(audioInputStream);

                // 音量を調整
                FloatControl volumeControl = (FloatControl) rwrContactClip
                        .getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(volume); // 音量をデシベル単位で設定
                rwrContactClip.start();
                isContact = false;
                stopRWRLaunchAlertSound();
                stopRWRTrackSound();
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // RWRサウンド停止メソッド
    public static void stopRWRLaunchAlertSound() {
        if (rwrLaunchAlertClip != null && rwrLaunchAlertClip.isRunning()) {
            rwrLaunchAlertClip.stop();
            rwrLaunchAlertClip.close();
            isLaunch = false;
        }
    }

    public static void stopRWRTrackSound() {
        if (rwrTrackClip != null && rwrTrackClip.isRunning()) {
            rwrTrackClip.stop();
            rwrTrackClip.close();
            isTrack = false;
        }
    }
}
