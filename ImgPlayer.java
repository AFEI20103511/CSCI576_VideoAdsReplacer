package Player;

import java.awt.image.BufferedImage;
import java.io.*;


public class ImgPlayer implements Runnable {
    public static boolean isPause;
    public static boolean isStop;
    private static final int WIDTH = 480;
    private static final int HEIGHT = 270;
    private static final int BUFFER_SIZE = WIDTH * HEIGHT * 3;
    private static final Object threadLock = new Object();
    private AudioPlayer audio;
    private static InputStream inputStream;

    private byte[] imgBuffer;
    private String inputFileName;
    private static BufferedImage img;

    public ImgPlayer(AudioPlayer playSound, String fileName) {
        this.inputFileName = fileName;
        img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        this.audio = playSound;
        imgBuffer = new byte[BUFFER_SIZE];
    }

    @Override
    public void run() {
        try {
            playVideo();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void readBytes() {
        synchronized (this) {
            while (isPause == true) {
                Thread.interrupted();
            }
        }

        try {
            int pos = 0, bytesRead;

            while (pos < imgBuffer.length && (bytesRead = inputStream.read(imgBuffer, pos, imgBuffer.length - pos)) >= 0) {
                pos = pos + bytesRead;
            }

            int idx = 0;
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    byte r = imgBuffer[idx];
                    byte g = imgBuffer[idx + HEIGHT * WIDTH];
                    byte b = imgBuffer[idx + 2 * HEIGHT * WIDTH];
                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    img.setRGB(x, y, pix);
                    idx++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playAfterPause() {
        synchronized (threadLock) {
            isPause = false;
            threadLock.notify();
        }
    }

    private int getFrameDiff(int offset){
        return (int) (Math.round(AudioPlayer.clip.getFramePosition()) / (audio.format.getSampleRate() / 30) + offset);
    }

    public void playVideo() throws InterruptedException, FileNotFoundException {

        File videoFile = new File(inputFileName);
        inputStream = new FileInputStream(videoFile);
        long numberOfFrames = videoFile.length() / BUFFER_SIZE;

        ImgComponent component = new ImgComponent();

        int offset = 0;
        int i2=0;
        if (!isStop)
        {
            while (i2 < getFrameDiff(0)) {
                renderImg(component);
                i2++;
            }
            while (i2 > getFrameDiff(offset));
            for (int i = i2; i < numberOfFrames; i++) {
                while (i > getFrameDiff(offset));
                while (i < getFrameDiff(offset)) {
                    renderImg(component);
                    i++;
                }
                renderImg(component);
            }
        }

    }

    private void renderImg(ImgComponent component) {
        readBytes();
        component.setImg(img);
        VideoPlayer.playerFrame.add(component);
        VideoPlayer.playerFrame.repaint();
        VideoPlayer.playerFrame.setVisible(true);
    }








}