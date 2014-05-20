package classes;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import org.jfree.graphics2d.canvas.CanvasGraphics2D;

/**
 *
 * @author fabinhosano
 */
public class JImagePanel extends JPanel {

    private String pathImage = "";
    private BufferedImage bi;
    private Graphics2D graphics2D;
    private BufferedImage bufferedImage;

    public JImagePanel() {
    }

    public JImagePanel(String pathImage) {
        this.pathImage = pathImage;
    }
    
    public JImagePanel(BufferedImage bufferedImage){
        this.bufferedImage = bufferedImage;
    }

    @Override
    public void paintComponent(Graphics g) {

        graphics2D = (Graphics2D) g.create();

        try {

            if(pathImage.equals("")){
                bi = bufferedImage;
            }else{
                bi = ImageIO.read(new File(pathImage));
            }
            
            int left = (this.getWidth() - bi.getWidth()) / 2;
            int top = (this.getHeight() - bi.getHeight()) / 2;
            graphics2D.drawImage(bi, left, top, null);

        } catch (IOException ex) {
            Logger.getLogger(JImagePanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
