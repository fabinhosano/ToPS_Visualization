/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.awt.event.KeyAdapter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author fabinhosano
 */
public class Simulate extends javax.swing.JPanel {

    private final String modelName = "model_name";
    private final String alphabet = "alphabet";
    private final String probabilities = "probabilities";
    private String pathModel = "";
    private String modelo = "";
    private boolean treinar = false;
    private JFileChooser fileChooser;
    private FileReader fileReader;
    private FileWriter fileWriter;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    /**
     * Creates new form Simulate
     */
    public Simulate() {
        //Inicializa os componentes da classe
        initComponents();

        //Carrega o painal para simular modelo
        carregarPanelSimular();

        //Inicializa o componente para escolha dos arquivos em uma pasta padrão
        fileChooser = new JFileChooser(
                new File("/home/fabinhosano/examples/vlmc/"));
    }

    public Simulate(String configurationFile) {
        //Inicializa os componentes da classe
        initComponents();

        //Carrega o painal para simular modelo
        carregarPanelSimular();

        //Inicializando fileChooser e definindo o arquivo a ser simulado
        File file = new File(configurationFile);
        fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(file);
        
        //Setando o modelo e o camimho do modelo
        modelo = fileChooser.getSelectedFile().getName();

        pathModel = fileChooser.getSelectedFile().getAbsolutePath();
        pathModel = pathModel.replaceFirst(fileChooser.getSelectedFile().getName(), "");

        //Exibe no textModelo o modelo a ser simulado
        textModelo.setText(fileChooser.getSelectedFile().getAbsolutePath());
        //Desabilita o botão para escolha do modelo
        buttonModelo.setEnabled(false);
    }

    public void carregarPanelSimular() {
        //Desabilitando o campo de texto de modelo e saída
        textModelo.setEditable(false);
        textSaida.setEditable(false);

        //Desabilitando o botão simular para preenchimento correto dos campos
        buttonSimular.setEnabled(false);

        //Criando mecanismos de interação do Panel Simular
        textModelo.getDocument().addDocumentListener(criaMecanismosText());
        textComprimento.getDocument().addDocumentListener(criaMecanismosText());
        textQuant.getDocument().addDocumentListener(criaMecanismosText());
        textSaida.getDocument().addDocumentListener(criaMecanismosText());

        //Criando validações numéricas para os campos tamanho e quantidade
        criaValidacaoNumerica(textQuant);
        criaValidacaoNumerica(textComprimento);

        //Quebrando linhas automáticas do modelo simulado
        textAreaSimular.setLineWrap(true);
        textAreaSimular.setWrapStyleWord(true);
    }

    private DocumentListener criaMecanismosText() {
        DocumentListener dl = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
            }

            public void removeUpdate(DocumentEvent e) {
                verificaCamposPreenchidos();
            }

            public void insertUpdate(DocumentEvent e) {
                verificaCamposPreenchidos();
            }
        };

        return dl;
    }

    private void criaValidacaoNumerica(final JTextField textField) {
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {

                textField.setText(textField.getText().replaceAll("[^0-9]", ""));

            }
        });
    }

    private boolean executeCommand(String directory) {
        try {
            String command = "simulate -m " + modelo + " -l " + textComprimento.getText()
                    + " -n " + textQuant.getText();

            Runtime runtime = Runtime.getRuntime();
            Process p = runtime.exec(command, null, new File(directory).getAbsoluteFile());
            p.waitFor();

            bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String leitura = null, description = "";

            while ((leitura = bufferedReader.readLine()) != null) {
                description += leitura + "\n";
            }

            String error = "";
            if (description.equals("")) {
                bufferedReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                error = bufferedReader.readLine();
            }

            bufferedReader.close();

            if (error.contains("error:") || error.contains("ERROR:")) {
                return false;
            } else {
                readSimulatedFile(writeSimulatedFile(description));
                return true;
            }


        } catch (InterruptedException ex) {
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("Não foi possível executar o comando");
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private boolean isAModel() {
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

    private void readSimulatedFile(File trainedFile) {
        try {
            fileReader = new FileReader(trainedFile);
            bufferedReader = new BufferedReader(fileReader);
            String leitura = null;

            textAreaSimular.setText("");

            while ((leitura = bufferedReader.readLine()) != null) {
                textAreaSimular.setText(textAreaSimular.getText() + leitura + "\n");
            }

            fileReader.close();
            bufferedReader.close();

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível encontrar o arquivo");
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Não foi ler o arquivo");
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void verificaCamposPreenchidos() {
        //Verifica se todos os campos estão preenchidos
        if (textModelo.getText().equals("") || textComprimento.getText().equals("")
                || textQuant.getText().equals("") || textSaida.getText().equals("")) {
            //Ativa o botão simular se todos campos estiverem preenchidos
            buttonSimular.setEnabled(false);
        } else {
            //Desativa o botão simular se algum campo não estiver preenchido
            buttonSimular.setEnabled(true);
        }
    }

    private File writeSimulatedFile(String description) {
        try {
            File file = new File(textSaida.getText());

            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(description);

            bufferedWriter.flush();

            fileWriter.close();
            bufferedWriter.close();

            return file;
        } catch (IOException ex) {
            System.out.println("Erro ao escrever arquivo treinado.");
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getTextSaida() {
        return textSaida.getText();
    }

    public boolean isTreinar() {
        return treinar;
    }

    public JButton getButtonSimular() {
        return buttonSimular;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelQuant = new javax.swing.JLabel();
        textQuant = new javax.swing.JTextField();
        scrollPaneSimular = new javax.swing.JScrollPane();
        textAreaSimular = new javax.swing.JTextArea();
        textSaida = new javax.swing.JTextField();
        buttonSaida = new javax.swing.JButton();
        labelModelo = new javax.swing.JLabel();
        buttonModelo = new javax.swing.JButton();
        textComprimento = new javax.swing.JTextField();
        labelSaida = new javax.swing.JLabel();
        buttonSimular = new javax.swing.JButton();
        labelSimular = new javax.swing.JLabel();
        textModelo = new javax.swing.JTextField();
        labelComprimento = new javax.swing.JLabel();

        labelQuant.setText("Quantidade");

        textAreaSimular.setColumns(20);
        textAreaSimular.setRows(5);
        scrollPaneSimular.setViewportView(textAreaSimular);

        buttonSaida.setText("Selecionar");
        buttonSaida.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaidaActionPerformed(evt);
            }
        });

        labelModelo.setText("Modelo");

        buttonModelo.setText("Selecionar");
        buttonModelo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonModeloActionPerformed(evt);
            }
        });

        labelSaida.setText("Saída");

        buttonSimular.setText("SIMULAR");
        buttonSimular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSimularActionPerformed(evt);
            }
        });

        labelSimular.setText("SIMULAR");

        labelComprimento.setText("Tamanho");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelQuant)
                    .addComponent(textQuant, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSimular, javax.swing.GroupLayout.PREFERRED_SIZE, 396, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelComprimento)
                    .addComponent(labelSaida)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(textComprimento, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(labelModelo)
                                .addGap(49, 49, 49))
                            .addComponent(textModelo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonModelo))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(textSaida, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonSaida))
                    .addComponent(scrollPaneSimular, javax.swing.GroupLayout.PREFERRED_SIZE, 396, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(labelSimular)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelSimular)
                .addGap(18, 18, 18)
                .addComponent(labelModelo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textModelo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonModelo, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelComprimento)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(textComprimento, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelQuant)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(textQuant, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelSaida)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textSaida, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSaida, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(buttonSimular)
                .addGap(18, 18, 18)
                .addComponent(scrollPaneSimular, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonSaidaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaidaActionPerformed
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

            textSaida.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_buttonSaidaActionPerformed

    private void buttonModeloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonModeloActionPerformed
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION
                && buttonModelo.isVisible()) {

            textModelo.setText(fileChooser.getSelectedFile().getAbsolutePath());

            if (isAModel()) {
                modelo = fileChooser.getSelectedFile().getName();

                pathModel = fileChooser.getSelectedFile().getAbsolutePath();
                pathModel = pathModel.replaceFirst(fileChooser.getSelectedFile().getName(), "");
            } else {
                JOptionPane.showMessageDialog(fileChooser,
                        "O arquivo selecionado não corresponde a um modelo válido.",
                        "ToPS Visualization - Simular", JOptionPane.INFORMATION_MESSAGE);

                textModelo.setText("");
            }
        }
    }//GEN-LAST:event_buttonModeloActionPerformed

    private void buttonSimularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSimularActionPerformed
        if (!treinar && !executeCommand(pathModel)) {
            textAreaSimular.setText("");

            JOptionPane.showMessageDialog(fileChooser,
                    "O modelo selecionado não pode ser simulado! \n"
                    + "Talvez ele tenha sofrido alterações depois de selecionado ou "
                    + "seja um modelo inválido. \n Verifique todos os seus parametros.",
                    "ToPS Visualization - Simular", JOptionPane.INFORMATION_MESSAGE);

        } else {
            if (!treinar && JOptionPane.showConfirmDialog(buttonSimular, "Gostaria de treinar "
                    + "o modelo simulado?", "ToPS Visualization - Simular",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                    == JOptionPane.YES_OPTION) {

                treinar = true;

                buttonSimular.doClick();
            }
        }

        buttonModelo.setEnabled(true);
    }//GEN-LAST:event_buttonSimularActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonModelo;
    private javax.swing.JButton buttonSaida;
    private javax.swing.JButton buttonSimular;
    private javax.swing.JLabel labelComprimento;
    private javax.swing.JLabel labelModelo;
    private javax.swing.JLabel labelQuant;
    private javax.swing.JLabel labelSaida;
    private javax.swing.JLabel labelSimular;
    private javax.swing.JScrollPane scrollPaneSimular;
    private javax.swing.JTextArea textAreaSimular;
    private javax.swing.JTextField textComprimento;
    private javax.swing.JTextField textModelo;
    private javax.swing.JTextField textQuant;
    private javax.swing.JTextField textSaida;
    // End of variables declaration//GEN-END:variables
}
