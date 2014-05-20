package classes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;

/**
 *
 * @author fabinhosano
 */
public final class Visualize_Discreteiid extends JPanel {

    private boolean simular = false;
    private FileParser fileParser;
    private JButton buttonSimulate;
    private BufferedImage bufferedImage;
    private JPanel jPanel;

    public Visualize_Discreteiid(FileParser fileParser) {
        this.fileParser = fileParser;

        setSize(1360, 750);

        setBackgroundPanel();
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D gr = (Graphics2D) g.create();

        gr.drawImage(bufferedImage, null, 0, 0);
    }

    private void setBackgroundPanel() {
        //Setando gráfico para o modelo DiscreteIID
        for (int i = 0; i < fileParser.getGraphicalData().size(); i++) {
            //verificando se o nó é a raiz;
            JFreeChart jFreeChart = ChartFactory.createPieChart(
                    "", fileParser.getGraphicalData().get(i)
                    .generationDefaultPieDataset(), true, true, false);
            PiePlot plotagem = (PiePlot) jFreeChart.getPlot();
            plotagem.setLabelGenerator(new StandardPieSectionLabelGenerator(
                    "{0} ({2})"));//define porcentagem no gráfico
            plotagem.setLabelBackgroundPaint(new Color(220, 220, 220));

            bufferedImage = jFreeChart.createBufferedImage(getWidth(), getHeight() - 55);

            JImagePanel panel = new JImagePanel(bufferedImage);
            panel.setSize(1360, 750);

            removeAll();
            add(panel);
            add(createButtonSimulate());
            
            repaint();
            revalidate();
            updateUI();
        }
    }

    private JButton createButtonSimulate() {
        buttonSimulate = new JButton();

        buttonSimulate.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        buttonSimulate.setName("buttonSimular");
        buttonSimulate.setText("Simular");
        buttonSimulate.setEnabled(true);
        buttonSimulate.setVisible(true);
        buttonSimulate.setMaximumSize(new Dimension(10, 5));

        buttonSimulate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!simular && JOptionPane.showConfirmDialog(buttonSimulate, "Gostaria de simular "
                        + "o modelo que está visualizando?", "ToPS Visualization - Visualizar",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                        == JOptionPane.YES_OPTION) {

                    simular = true;

                    buttonSimulate.doClick();
                }
            }
        });

        return buttonSimulate;
    }

    public String getPathModel() {
        return fileParser.getFileChooser().getSelectedFile().getAbsolutePath();
    }

    public boolean isSimular() {
        return simular;
    }

    public JButton getButtonVisualizar() {
        return buttonSimulate;
    }
    
    private void setPanel(JPanel jPanel) {
        this.jPanel = jPanel;
    }

    public JPanel getPanel() {
        return jPanel;
    }
}
