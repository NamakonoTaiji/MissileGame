package test;

import javax.sound.sampled.*;

import java.io.File;
import java.io.IOException;

public class SoundPlayer {

    public static void playSound(String soundFileName, float volume)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File soundFile = new File(soundFileName);
        if (!soundFile.exists()) {
            System.err.println("Sound file not found: " + soundFileName);
            return;
        }

        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);

        // 音量を調整
        FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        volumeControl.setValue(volume); // 音量をデシベル単位で設定

        clip.start();
        clip.close(); // 再生終了後にリソースを解放
    }

    public static void main(String[] args) {
        try {
            playSound("E:\\programmingPractice\\MissileGame\\test\\flare-001.wav", 4); // 音量を0.5 (半分) に設定
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }
}