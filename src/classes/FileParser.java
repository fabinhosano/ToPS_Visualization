package classes;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JFileChooser;
import tops.parser.ConfigurationReader;
import tops.parser.ProbabilisticModelParameterValue;
import tops.parser.ProbabilisticModelParameters;

/**
 *
 * @author fabinhosano
 */
public class FileParser {

    private String modelName = "";
    private boolean flag;
    private boolean ordenar = true;
    private ArrayList<GraphicalData> graphicalData = new ArrayList<>();
    private JFileChooser fileChooser;
    private HashMap<String, Integer> probabilitiesReader;
    private ProbabilisticModelParameters params;
    private ProbabilisticModelParameterValue probParamValue;
    private ArrayList<String> alpha;

    public FileParser(JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    public FileParser(String pathModel) {
        File file = new File(pathModel);
        fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(file);
    }

    public JFileChooser getFileChooser() {
        return fileChooser;
    }

    public ArrayList<GraphicalData> getGraphicalData() {
        return graphicalData;
    }

    public void setGraphicalData(ArrayList<GraphicalData> graphicalData) {
        this.graphicalData = graphicalData;
    }

    public String getModelName() {
        return modelName;
    }

    public void readProbabilisticModelFile() {
        probabilitiesReader = new HashMap<>();

        ConfigurationReader reader = new ConfigurationReader();
        params = reader.load(fileChooser.getSelectedFile().getAbsolutePath());

        //leitura do nome do modelo probabilistico
        probParamValue = params.getMandatoryParameterValue("model_name");
        modelName = probParamValue.getString();

        //leitura do alfabeto do modelo probabilitisco
        probParamValue = params.getMandatoryParameterValue("alphabet");
        alpha = probParamValue.getStringVector();

        if (modelName.equals("DiscreteIIDModel")) {
            readProbabilisticModelParametersDiscreteIID();
        } else {
            if (modelName.equals("VariableLengthMarkovChain")) {
                readProbabilisticModelParametersVLMC();
            }
        }
    }

    public void readProbabilisticModelParametersDiscreteIID() {
        probParamValue = params.getMandatoryParameterValue("probabilities");
        ArrayList<Double> prob_map = probParamValue.getDoubleVector();

        GraphicalData gd = new GraphicalData(modelName);
        for (int i = 0, j = 0; i < alpha.size() && j < prob_map.size(); i++, j++) {
            gd.addValue(alpha.get(i));
            gd.addProbabilitie(prob_map.get(i));
        }
        
        graphicalData.add(gd);
    }

    public void readProbabilisticModelParametersVLMC() {
        probParamValue = params.getMandatoryParameterValue("probabilities");
        HashMap<String, Double> prob_map = probParamValue.getDoubleMap();

        //criação de lista de comparação de probabilities.
        ArrayList<String> prob = new ArrayList<>();

        //percorrendo probabilities e preenchendo lista de comparação.
        for (String probabilitie : prob_map.keySet()) {
            prob.add(probabilitie);
        }

        //ordenando lista de probabilities para comparação.
        Collections.sort(prob);

        //percorrendo lista de probabilities ordenada para criação do grafo.
        for (int i = 0; i < prob.size(); i++) {
            for (String probabilitie : prob_map.keySet()) {
                if (probabilitie.equals(prob.get(i))) {
                    System.out.println("prob(i): " + prob.get(i));
                    flag = true;
                    String partA = "", partB = "";
                    String[] parts = probabilitie.split("\\|");
                    partA = parts[0];
                    partB = parts[1];

                    for (Map.Entry<String, Integer> entry : probabilitiesReader.entrySet()) {
                        String value = entry.getKey();
                        Integer probabilities = entry.getValue();
                        if (value.equals(partB)) {
                            entry.setValue(probabilities + 1);
                            for (Iterator<GraphicalData> it = graphicalData.iterator(); it.hasNext();) {
                                GraphicalData data = it.next();
                                if (data.getName().equals(partB)) {
                                    data.addValue(partA);
                                    data.addProbabilitie(prob_map.get(probabilitie));
                                    flag = false;
                                }
                            }
                        }
                    }

                    if (flag) {
                        probabilitiesReader.put(partB, 1);
                        GraphicalData gd = new GraphicalData(partB);
                        gd.addValue(partA);
                        gd.addProbabilitie(prob_map.get(probabilitie));
                        graphicalData.add(gd);
                    }

                    //removendo probabilitie encontrado para melhora do desempenho.
                    prob_map.remove(probabilitie);
                    break;
                }
            }
        }
    }
}
