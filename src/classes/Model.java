/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 *
 * @author fabinhosano
 */
public final class Model extends javax.swing.JFrame {

    private final String VLMC = "VariableLengthMarkovChain";
    private final String DiscreteIID = "DiscreteIIDModel";
    private final String modelName = "model_name";
    private final String alphabet = "alphabet";
    private final String probabilities = "probabilities";
    private String pathFile = "";
    private Visualize_VLMC visualizar_vlmc;
    private Visualize_Discreteiid visualizar_discreteiid;
    private Train treinar;
    private Simulate simular;
    private BufferedReader bufferedReader;
    private FileReader fileReader;
    private JFileChooser fileChooser;

    public Model() {
        carregarMenuModelo();

        setVisible(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setTitle("ToPS Visualization - Modelo");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public Model(String pathFile) {
        carregarMenuModelo();

        this.pathFile = pathFile;

        setVisible(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle("ToPS Visualization - Modelo");
        setExtendedState(MAXIMIZED_BOTH);
    }

    public void carregarMenuModelo() {
        initComponents();

        pathFile = "";

        getContentPane().repaint();

        menu.remove(menuModelo);

        comboBoxModelo.removeAllItems();
        comboBoxModelo.addItem("Simular");
        comboBoxModelo.addItem("Treinar");
        comboBoxModelo.addItem("Visualizar");

        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
    }

    public void carregarModelo() {
        if (comboBoxModelo.getSelectedItem().equals("Simular")) {
            simulateModel();
        } else {
            if (comboBoxModelo.getSelectedItem().equals("Treinar")) {
                trainSequence();
            } else {
                if (comboBoxModelo.getSelectedItem().equals("Visualizar")) {
                    visualizationModel();
                }
            }
        }

        getContentPane().repaint();
        pack();
    }

    private void fillDataSimulate_DiscreteIID() {
        if (visualizar_discreteiid.isSimular()) {
            pathFile = visualizar_discreteiid.getPathModel();

            setModelo("Simular");
        }
    }

    private void fillDataSimulate_VLMC() {
        if (visualizar_vlmc.isSimular()) {
            pathFile = visualizar_vlmc.getPathModel();

            setModelo("Simular");
        }
    }

    private void fillDataTrain() {
        if (simular.isTreinar()) {
            pathFile = simular.getTextSaida();

            setModelo("Treinar");
        }
    }

    private void fillDataVisualization() {
        if (treinar.isVisualizar()) {
            pathFile = "/home/fabinhosano/examples/vlmc/vlmc.txt";

            setModelo("Visualizar");
        }
    }

    private boolean isAValidModel() {
        boolean alpha = false, mName = false, prob = false;
        try {
            fileReader = new FileReader(fileChooser.getSelectedFile());
            bufferedReader = new BufferedReader(fileReader);
            String leitura = null;

            while ((leitura = bufferedReader.readLine()) != null) {

                Pattern pAlphabet = Pattern.compile(alphabet);
                Matcher mAlphabet = pAlphabet.matcher(leitura);

                Pattern pModelName = Pattern.compile(modelName);
                Matcher mModelName = pModelName.matcher(leitura);

                Pattern pProbabilities = Pattern.compile(probabilities);
                Matcher mProbabilities = pProbabilities.matcher(leitura);

                if (mModelName.find()) {
                    mName = true;
                } else {
                    if (mAlphabet.find()) {
                        alpha = true;
                    } else {
                        if (mProbabilities.find()) {
                            prob = true;
                        }
                    }
                }
            }

            fileReader.close();
            bufferedReader.close();

            if (alpha && prob && mName) {
                return true;
            }

        } catch (FileNotFoundException ex) {
            System.out.println("Arquivo Inexistente");
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("Não foi possível ler o arquivo!");
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private void modelInvalid() {
        JOptionPane.showMessageDialog(fileChooser,
                "O arquivo selecionado não corresponde a um modelo válido.",
                "ToPS Visualization - Modelo", JOptionPane.INFORMATION_MESSAGE);

        pathFile = "";

        getContentPane().removeAll();

        carregarMenuModelo();
    }

    private JButton openSimulate_DiscreteIID() {
        JButton openSimulate = visualizar_discreteiid.getButtonVisualizar();

        openSimulate.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                fillDataSimulate_DiscreteIID();
            }
        });

        return openSimulate;
    }

    private JButton openSimulate_VLMC() {
        JButton openSimulate = visualizar_vlmc.getButtonVisualizar();

        openSimulate.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                fillDataSimulate_VLMC();
            }
        });

        return openSimulate;
    }

    private JButton openTrain() {
        JButton openTrain = simular.getButtonSimular();

        openTrain.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                fillDataTrain();
            }
        });

        return openTrain;
    }

    private JButton openVisualization() {
        JButton openVisualization = treinar.getButtonTreinar();

        openVisualization.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                fillDataVisualization();
            }
        });

        return openVisualization;
    }

    private void simulateModel() {
        if (pathFile.equals("")) {
            simular = new Simulate();
        } else {
            System.out.println(pathFile);
            simular = new Simulate(pathFile);
            pathFile = "";
        }

        simular.setSize(1360, 750);

        getContentPane().add(openTrain());
        setContentPane(simular);
        setTitle("ToPS Visualization - Modelo (Simular)");
    }

    private void trainSequence() {
        if (pathFile.equals("")) {
            treinar = new Train();
        } else {
            treinar = new Train(pathFile);
            pathFile = "";
        }

        treinar.setSize(1360, 750);

        getContentPane().add(openVisualization());
        setContentPane(treinar);
        setTitle("ToPS Visualization - Modelo (Treinar)");
    }

    private void visualizationModel() {
        if (pathFile.equals("")) {
            File file = new File("/home/fabinhosano/examples/vlmc/");
            fileChooser = new JFileChooser(file);
            if (fileChooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) {
                carregarMenuModelo();
            }
        } else {
            File file = new File(pathFile);
            fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(file);
        }


        if (isAValidModel()) {
            pathFile = fileChooser.getSelectedFile().getAbsolutePath();

            FileParser fileParser = new FileParser(pathFile);
            fileParser.readProbabilisticModelFile();

            pathFile = "";

            if (fileParser.getModelName().equals(VLMC)) {
                visualizar_vlmc = new Visualize_VLMC(fileParser);
                visualizar_vlmc.setSize(1360, 750);

                visualizar_vlmc.getBox().add(openSimulate_VLMC());

                setContentPane(visualizar_vlmc.getPanel());
                getContentPane().add(visualizar_vlmc.getBox(), BorderLayout.SOUTH);
                setTitle("ToPS Visualization - Modelo (Visualizar - " + VLMC + " )");
            } else {
                if (fileParser.getModelName().equals(DiscreteIID)) {
                    visualizar_discreteiid = new Visualize_Discreteiid(fileParser);
                    visualizar_discreteiid.setSize(1360, 750);

                    setContentPane(visualizar_discreteiid);
                    getContentPane().add(openSimulate_DiscreteIID());
                    setTitle("ToPS Visualization - Modelo (Visualizar - " + DiscreteIID + " )");
                } else {
                    modelInvalid();
                }
            }
        }
    }

    public void setModelo(String modelo) {
        comboBoxModelo.setSelectedItem(modelo);

        buttonSelecionar.doClick();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        comboBoxModelo = new javax.swing.JComboBox();
        buttonSelecionar = new javax.swing.JButton();
        menu = new javax.swing.JMenuBar();
        menuModelo = new javax.swing.JMenu();
        menuFecharModelo = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        comboBoxModelo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        buttonSelecionar.setText("Selecionar");
        buttonSelecionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelecionarActionPerformed(evt);
            }
        });

        menuModelo.setText("Modelo");
        menuModelo.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menuModeloMenuSelected(evt);
            }
        });
        menu.add(menuModelo);

        menuFecharModelo.setText("Fechar");
        menuFecharModelo.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menuFecharModeloMenuSelected(evt);
            }
        });
        menu.add(menuFecharModelo);

        setJMenuBar(menu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(comboBoxModelo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSelecionar, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboBoxModelo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelecionar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonSelecionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelecionarActionPerformed
        getContentPane().removeAll();

        //removendo o menu fechar modelo para organizar a barra de menus
        menu.remove(menuFecharModelo);

        //adcionando os menus da barra de menus
        menu.add(menuModelo);
        menu.add(menuFecharModelo);

        getContentPane().repaint();

        carregarModelo();
    }//GEN-LAST:event_buttonSelecionarActionPerformed

    private void menuModeloMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuModeloMenuSelected
        int retornarMenu = JOptionPane.showConfirmDialog(rootPane,
                "Deseja retornar ao menu principal?",
                "ToPS Visualization - Modelo", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (retornarMenu == JOptionPane.YES_OPTION) {
            getContentPane().removeAll();

            carregarMenuModelo();
        }
    }//GEN-LAST:event_menuModeloMenuSelected

    private void menuFecharModeloMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuFecharModeloMenuSelected
        int fechar = JOptionPane.showConfirmDialog(rootPane, "Deseja realmente fechar o modelo?",
                "ToPS Visualization", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (fechar == JOptionPane.YES_OPTION) {
            dispose();
        }
    }//GEN-LAST:event_menuFecharModeloMenuSelected
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonSelecionar;
    private javax.swing.JComboBox comboBoxModelo;
    private javax.swing.JMenuBar menu;
    private javax.swing.JMenu menuFecharModelo;
    private javax.swing.JMenu menuModelo;
    // End of variables declaration//GEN-END:variables
}
