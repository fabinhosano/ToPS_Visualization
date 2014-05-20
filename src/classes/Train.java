package classes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author fabinhosano
 */
public class Train extends javax.swing.JPanel {

    private final String modelDiscreteiid = "discreteiid.txt_";
    private final String modelVlmc = "vlmc.txt_";
    private final String discreteiid = "discreteiid";
    private final String vlmc = "vlmc";
    private final String configurationFile = "ConfigurationFile.txt";
    private final String pathTemp = "/tmp/";
    private boolean visualizar = false;
    private String pathConfigFile = "";
    private JFileChooser fileChooser;
    private FileReader fileReader;
    private FileWriter fileWriter;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    /**
     * Creates new form Train
     */
    public Train() {
        initComponents();

        fileChooser = new JFileChooser(
                new File("/home/fabinhosano/examples"));

        carregarPanelTreinar();
    }

    public Train(String arquivoSeq) {
        initComponents();

        File file = new File(arquivoSeq);
        fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(file);

        carregarPanelTreinar();

        textArqSeq.setText(fileChooser.getSelectedFile().getAbsolutePath());
        textAlfabeto.setText(readAlphabet(readSequence()));

        pathConfigFile = fileChooser.getSelectedFile().getAbsolutePath();
        pathConfigFile = pathConfigFile.replaceFirst(fileChooser.getSelectedFile().getName(), "");

        buttonArqSeq.setEnabled(false);
    }

    public void carregarPanelTreinar() {
        //Desabilitando os campos de texto do panel Train
        textSaida.setEditable(false);
        textArqSeq.setEditable(false);
        textAlfabeto.setEditable(false);
        textAreaTreinar.setEditable(false);

        //Removendo os valores e desabilitando comboBoxModelo
        comboBoxModelo.removeAllItems();
        comboBoxModelo.setEditable(false);
        comboBoxModelo.setEnabled(false);

        //Desabilitando o botão simular para preenchimento correto dos campos
        buttonTreinar.setEnabled(false);

        //Setando o filtro do JFileChooser para apenas arquivos texto.
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Texto", "txt"));
        //Setando o JfileChooser para não aceitar todos tipos de arquivo.
        fileChooser.setAcceptAllFileFilterUsed(false);
        //Setando o texto da janela para escolha do arquivo
        fileChooser.setDialogTitle("ToPS Visualization - Treinar");

        //Criando mecanismos de interação do Panel Train
        textArqSeq.getDocument().addDocumentListener(criaMecanismosText());
        textAlfabeto.getDocument().addDocumentListener(criaMecanismosText());
        textSaida.getDocument().addDocumentListener(criaMecanismosText());
    }

    private DocumentListener criaMecanismosText() {
        DocumentListener dl = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                verificaCamposPreenchidos();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                verificaCamposPreenchidos();
            }
        };

        return dl;
    }

    private boolean executeCommand(String directory) {
        try {

            String command = "train -c" + directory + configurationFile + " > "
                    + textSaida.getText();

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

            if (error.equals("ERROR: Invalid sequence !")) {
                return false;
            } else {
                readTrainedFile(writeTrainedFile(description));
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

    private boolean isASequence() {
        try {
            fileReader = new FileReader(fileChooser.getSelectedFile());
            bufferedReader = new BufferedReader(fileReader);
            String leitura = null;

            int countSeq = 0;

            while ((leitura = bufferedReader.readLine()) != null) {

                Pattern pDiscreteiid = Pattern.compile(modelDiscreteiid + countSeq + ":\t");
                Matcher mDiscreteiid = pDiscreteiid.matcher(leitura);

                Pattern pVLMC = Pattern.compile(modelVlmc + countSeq + ":\t");
                Matcher mVLMC = pVLMC.matcher(leitura);

                if (mDiscreteiid.find()) {
                    return true;
                } else {
                    if (mVLMC.find()) {
                        return true;
                    }
                }

                ++countSeq;
            }

            fileReader.close();
            bufferedReader.close();

        } catch (FileNotFoundException ex) {
            System.out.println("Arquivo Inexistente");
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("Não foi possível ler o arquivo!");
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private String readAlphabet(String alphabet) {
        String alpha = "";

        for (int i = 0; i < alphabet.length(); i++) {
            if ((i != 0) && (i < alphabet.length() - 1) && (alphabet.charAt(i) != ' ')
                    && (alphabet.charAt(i) != ',')) {
                alpha += "\"" + alphabet.charAt(i) + "\", ";
            }
        }

        alpha = "(" + alpha.substring(0, alpha.length() - 2) + ")";

        return alpha;
    }

    private void readTrainedFile(File trainedFile) {
        try {
            fileReader = new FileReader(trainedFile);
            bufferedReader = new BufferedReader(fileReader);
            String leitura = null;

            textAreaTreinar.setText("");

            while ((leitura = bufferedReader.readLine()) != null) {
                textAreaTreinar.setText(textAreaTreinar.getText() + leitura + "\n");
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

    private String readSequence() {
        try {
            int countSequences = 0;

            fileReader = new FileReader(fileChooser.getSelectedFile());
            bufferedReader = new BufferedReader(fileReader);
            String leitura = null;
            TreeSet<String> alphabet = new TreeSet<String>();

            while ((leitura = bufferedReader.readLine()) != null) {
                leitura = removeNameSequence(leitura, countSequences++);

                for (int i = 0; i < leitura.length(); i++) {
                    if (leitura.charAt(i) != ' ' && leitura.charAt(i) != '\n') {
                        alphabet.add("" + leitura.charAt(i));
                    }
                }
            }

            fileReader.close();
            bufferedReader.close();

            return alphabet.clone().toString();

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível encontrar o arquivo");
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Não foi ler o arquivo");
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String removeNameSequence(String line, int number) {
        String d, v;

        Pattern patternDiscreteiid = Pattern.compile(modelDiscreteiid + number + ":\t");
        Matcher matcherDiscreteiid = patternDiscreteiid.matcher(line);

        Pattern patternVLMC = Pattern.compile(modelVlmc + number + ":\t");
        Matcher matcherVLMC = patternVLMC.matcher(line);

        //Removendo itens do Modelo e deixando como vazio
        comboBoxModelo.removeAllItems();

        if (matcherDiscreteiid.find()) {
            d = matcherDiscreteiid.replaceAll("");
            comboBoxModelo.addItem(discreteiid);
            return d;
        } else {
            if (matcherVLMC.find()) {
                v = matcherVLMC.replaceAll("");
                comboBoxModelo.addItem(vlmc);
                return v;
            }
        }
        return line;
    }

    private void verificaCamposPreenchidos() {
        if (textArqSeq.getText().equals("") || textSaida.getText().equals("")
                || textAlfabeto.getText().equals("")) {
            buttonTreinar.setEnabled(false);
        } else {
            buttonTreinar.setEnabled(true);
        }
    }

    private File writeConfigurationFile(String directory) {
        try {
            File file = new File(directory + configurationFile);
            file.createNewFile();

            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);


            if (comboBoxModelo.getSelectedItem().equals(discreteiid)) {
                bufferedWriter.write("training_algorithm=\"DiscreteIIDModel\"");
                bufferedWriter.newLine();
                bufferedWriter.write("alphabet=" + textAlfabeto.getText());
                bufferedWriter.newLine();
                bufferedWriter.write("training_set=\"sequence_from_" + discreteiid + ".txt\"");
            } else {
                if (comboBoxModelo.getSelectedItem().equals(vlmc)) {
                    bufferedWriter.write("training_algorithm=\"ContextAlgorithm\"");
                    bufferedWriter.newLine();
                    bufferedWriter.write("alphabet=" + textAlfabeto.getText());
                    bufferedWriter.newLine();
                    bufferedWriter.write("training_set=\"sequence_from_" + vlmc + ".txt\"");
                    bufferedWriter.newLine();
                    bufferedWriter.write("model_selection_criteria=\"BIC\"");
                    bufferedWriter.newLine();
                    bufferedWriter.write("begin=(\"cut\": 0.0)");
                    bufferedWriter.newLine();
                    bufferedWriter.write("end = (\"cut\": 3.0)");
                    bufferedWriter.newLine();
                    bufferedWriter.write("step=(\"cut\": 0.1)");
                }
            }

            bufferedWriter.flush();

            fileWriter.close();
            bufferedWriter.close();

            return file;
        } catch (IOException ex) {
            System.out.println("Não foi possível escrever no arquivo.");
            Logger.getLogger(Train.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private File writeTrainedFile(String description) {
        try {
            System.out.println(description);
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

    public boolean isVisualizar() {
        return visualizar;
    }

    public JButton getButtonTreinar() {
        return buttonTreinar;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonArqSeq = new javax.swing.JButton();
        labelAlfabeto = new javax.swing.JLabel();
        buttonSaida = new javax.swing.JButton();
        labelSaida = new javax.swing.JLabel();
        labelTreinar = new javax.swing.JLabel();
        textAlfabeto = new javax.swing.JTextField();
        textArqSeq = new javax.swing.JTextField();
        textSaida = new javax.swing.JTextField();
        labelArqSeq = new javax.swing.JLabel();
        comboBoxModelo = new javax.swing.JComboBox();
        scrollPaneTreinar = new javax.swing.JScrollPane();
        textAreaTreinar = new javax.swing.JTextArea();
        buttonTreinar = new javax.swing.JButton();
        labelModelo = new javax.swing.JLabel();

        buttonArqSeq.setText("Selecionar");
        buttonArqSeq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonArqSeqActionPerformed(evt);
            }
        });

        labelAlfabeto.setText("Alfabeto");

        buttonSaida.setText("Selecionar");
        buttonSaida.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaidaActionPerformed(evt);
            }
        });

        labelSaida.setText("Saída");

        labelTreinar.setText("TREINAR");

        labelArqSeq.setText("Arquivo de Sequência");

        comboBoxModelo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        textAreaTreinar.setColumns(20);
        textAreaTreinar.setRows(5);
        scrollPaneTreinar.setViewportView(textAreaTreinar);

        buttonTreinar.setText("TREINAR");
        buttonTreinar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTreinarActionPerformed(evt);
            }
        });

        labelModelo.setText("Modelo");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPaneTreinar, javax.swing.GroupLayout.PREFERRED_SIZE, 396, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelAlfabeto, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textArqSeq, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textAlfabeto, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(comboBoxModelo, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonArqSeq)
                            .addComponent(labelModelo)))
                    .addComponent(buttonTreinar, javax.swing.GroupLayout.PREFERRED_SIZE, 396, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelArqSeq)
                    .addComponent(labelSaida)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(textSaida, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(buttonSaida)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(labelTreinar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTreinar)
                .addGap(18, 18, 18)
                .addComponent(labelArqSeq)
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textArqSeq, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonArqSeq, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelAlfabeto)
                    .addComponent(labelModelo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textAlfabeto, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBoxModelo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelSaida)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textSaida, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSaida, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(buttonTreinar)
                .addGap(18, 18, 18)
                .addComponent(scrollPaneTreinar, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonArqSeqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonArqSeqActionPerformed
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

            textArqSeq.setText(fileChooser.getSelectedFile().getAbsolutePath());

            if (isASequence()) {
                textAlfabeto.setText(readAlphabet(readSequence()));

                pathConfigFile = fileChooser.getSelectedFile().getAbsolutePath();
                pathConfigFile = pathConfigFile.replaceFirst(fileChooser.getSelectedFile().getName(), "");
            } else {
                JOptionPane.showMessageDialog(fileChooser,
                        "O arquivo selecionado não corresponde a uma sequência válida.",
                        "ToPS Visualization - Treinar", JOptionPane.INFORMATION_MESSAGE);

                textArqSeq.setText("");
                textAlfabeto.setText("");
                pathConfigFile = "";
                comboBoxModelo.removeAllItems();
            }

        }
    }//GEN-LAST:event_buttonArqSeqActionPerformed

    private void buttonSaidaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaidaActionPerformed
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

            textSaida.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_buttonSaidaActionPerformed

    private void buttonTreinarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTreinarActionPerformed
        writeConfigurationFile(pathConfigFile);

        if (!visualizar && !executeCommand(pathConfigFile)) {
            textAreaTreinar.setText("");

            JOptionPane.showMessageDialog(fileChooser,
                    "A sequência selecionada não pode ser treinada! \n"
                    + "Talvez ela tenha sofrido alterações depois de selecionada."
                    + "\n Verifique os paramentros da sequência",
                    "ToPS Visualization - Treinar", JOptionPane.INFORMATION_MESSAGE);

        } else {
            if (!visualizar && JOptionPane.showConfirmDialog(buttonTreinar, "Gostaria de visualizar "
                    + "o modelo treinado?", "ToPS Visualization - Treinar",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                    == JOptionPane.YES_OPTION) {

                visualizar = true;

                buttonTreinar.doClick();
            }
        }

        buttonArqSeq.setEnabled(true);
    }//GEN-LAST:event_buttonTreinarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonArqSeq;
    private javax.swing.JButton buttonSaida;
    private javax.swing.JButton buttonTreinar;
    private javax.swing.JComboBox comboBoxModelo;
    private javax.swing.JLabel labelAlfabeto;
    private javax.swing.JLabel labelArqSeq;
    private javax.swing.JLabel labelModelo;
    private javax.swing.JLabel labelSaida;
    private javax.swing.JLabel labelTreinar;
    private javax.swing.JScrollPane scrollPaneTreinar;
    private javax.swing.JTextField textAlfabeto;
    private javax.swing.JTextArea textAreaTreinar;
    private javax.swing.JTextField textArqSeq;
    private javax.swing.JTextField textSaida;
    // End of variables declaration//GEN-END:variables
}
