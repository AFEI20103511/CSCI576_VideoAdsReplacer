package Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;



class MyPanel extends JButton {
    public MyPanel(String buttonLabel) {
        this.setFont(new Font("Serif", Font.ITALIC, 12));
        this.setText(buttonLabel);
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    handleActions(getText());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        };
        this.addMouseListener(adapter);
    }

    public void handleActions(String buttonLabel) throws IOException {
        switch (buttonLabel) {
            case "PLAY":
                ImgPlayer.playAfterPause();
                AudioPlayer.clip.start();
                break;
            case "PAUSE":
                ImgPlayer.isPause = true;
                AudioPlayer.clip.stop();
                break;
            case "STOP":
                ImgPlayer.isStop = true;
                AudioPlayer.clip.stop();
                AudioPlayer.clip.setFramePosition(0);
                System.exit(0);
                break;
            default:
                System.err.println("ERROR: Unknown behavior");
        }
    }

}