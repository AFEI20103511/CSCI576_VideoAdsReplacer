package Player;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.*;


public class AudioPlayer implements Runnable {
    public static Clip clip;
    public InputStream stream;
    public AudioFormat format;
    public AudioPlayer(InputStream stream) throws LineUnavailableException {
        this.stream = stream;
        clip = AudioSystem.getClip();
    }
    @Override
    public void run() {
        try (AudioInputStream audioStreamIn = AudioSystem.getAudioInputStream(new BufferedInputStream(this.stream));){
            clip.open(audioStreamIn);
            clip.start();
            format = audioStreamIn.getFormat();
        } catch (Exception e) {
            System.err.println("ERROR: audio input is NULL!!!!!!!!!");
            e.printStackTrace();
        }
    }
}
