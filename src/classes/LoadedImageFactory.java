/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.awt.Color;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DefaultPieDataset;
import prefuse.render.ImageFactory;

/**
 *
 * @author fabinhosano
 */
public class LoadedImageFactory extends ImageFactory {

    private HashMap<String, DefaultPieDataset> graficos = new HashMap<>();
    private Map<String, LegendTitle> legends = new HashMap<>();

    public LoadedImageFactory() {
        this(-1, -1);
    }

    public void setGraficos(HashMap<String, DefaultPieDataset> graficos) {
        this.graficos = graficos;
    }

    public LoadedImageFactory(int maxImageWidth, int maxImageHeight) {
        setMaxImageDimensions(maxImageWidth, maxImageHeight);
    }

    @Override
    public void setMaxImageDimensions(int width, int height) {
        this.m_maxImageWidth = width;
        this.m_maxImageHeight = height;
    }

    @Override
    public Image getImage(String imageName) {
        Image image = (Image) this.imageCache.get(imageName);
        if (image == null && !this.loadMap.containsKey(imageName)) {
            int id = ++this.nextTrackerID;

            image = generateImage(imageName);

            if (!m_asynch) {
                waitForImage(image);
                addImage(imageName, image);
            } else {
                id = ++nextTrackerID;
                tracker.addImage(image, id);
                loadMap.put(imageName, new LoadMapEntry(id, image));
            }
        } else if (image == null && this.loadMap.containsKey(imageName)) {
            LoadMapEntry entry = (LoadMapEntry) this.loadMap.get(imageName);
            if (this.tracker.checkID(entry.id, true)) {
                addImage(imageName, entry.image);
                this.loadMap.remove(imageName);
                this.tracker.removeImage(entry.image, entry.id);
            }
        } else {
            return image;
        }
        return (Image) this.imageCache.get(imageName);
    }

    @Override
    public Image addImage(String name, Image image) {
        if (this.m_maxImageWidth > -1 || this.m_maxImageHeight > -1) {
            image = getScaledImage(image);
            image.getWidth(null); // trigger image load
        }
        this.imageCache.put(name, image);
        return image;
    }

    private Image generateImage(String imageName) {
        JFreeChart jFreeChart = ChartFactory.createPieChart3D(
                "", graficos.get(imageName), true, true, false);
        legends.put(imageName, jFreeChart.getLegend());
        jFreeChart.removeLegend();
        PiePlot plotagem = (PiePlot) jFreeChart.getPlot();
        plotagem.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0} ({2})"));//define porcentagem no gráfico
        plotagem.setLabelBackgroundPaint(new Color(220, 220, 220));
        plotagem.setBackgroundPaint(new Color(220, 220, 220));
        
        return jFreeChart.createBufferedImage(375, 215);

    }

    public LegendTitle getLegend(String No) {
        for (Map.Entry<String, LegendTitle> legendas : legends.entrySet()) {
            // pegando dados das legendas
            String nome = legendas.getKey();

            if (nome.equals(No)) {
                return legendas.getValue();
            }

        }
        return null;
    }
    

    public void cleanImageCache() {
        imageCache.clear();
    }

    private class LoadMapEntry {

        public int id;
        public Image image;

        public LoadMapEntry(int id, Image image) {
            this.id = id;
            this.image = image;
        }
    }
}
