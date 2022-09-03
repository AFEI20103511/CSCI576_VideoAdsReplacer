package Player;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;

public class VideoPlayer {

    public static JFrame playerFrame;
    public static final String STOP = "STOP";
    public static final String PLAY = "PLAY";
    public static final String PAUSE = "PAUSE";
    private static final int AUDIO_PRELOAD_MS = 200;
    static {
        playerFrame = new JFrame();
        playerFrame.setSize(500, 400);
        playerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel btnPan = new JPanel();
        btnPan.setLayout(new BoxLayout(btnPan, BoxLayout.X_AXIS));
        btnPan.setPreferredSize(new Dimension(150, 50));
        playerFrame.getContentPane().add(btnPan, BorderLayout.NORTH);
        btnPan.add(Box.createRigidArea(new Dimension(100, 20)));
        MyPanel btn = new MyPanel(PLAY);
        btnPan.add(btn);
        btnPan.add(Box.createRigidArea(new Dimension(25, 25)));
        btn = new MyPanel(PAUSE);
        btnPan.add(btn);
        btnPan.add(Box.createRigidArea(new Dimension(25, 25)));
        btn = new MyPanel(STOP);
        btnPan.add(btn);
        btnPan.add(Box.createRigidArea(new Dimension(0, 25)));
        playerFrame.setVisible(true);
    }



    public static void main(String[] args) throws IOException, InterruptedException, FileNotFoundException, LineUnavailableException {
        if (args.length != 2) {
            System.err.println("ERROR: there must be exactly 2 arguments");
            System.err.println("Usage: java VideoPlayer [rgb file name] [wav file name]");
            return;
        }
        String rgbPath = args[0];
        String wavPath = args[1];
        FileInputStream inputStream = new FileInputStream(wavPath);
        AudioPlayer audio = new AudioPlayer(inputStream);
        ImgPlayer img = new ImgPlayer(audio, rgbPath);
        new Thread(audio).start();
        Thread.sleep(AUDIO_PRELOAD_MS);
        new Thread(img).start();
    }
}

