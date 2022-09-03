package Player;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImgComponent extends JComponent {

    private BufferedImage img;

    public ImgComponent(){ }
    public void setImg(BufferedImage img) {
        this.img = img;
    }
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(img, 0, 0, this);
    }

    @Override
    public String toString() {
        return "ImgComponent{" +
                "img=" + img +
                '}';
    }
}
