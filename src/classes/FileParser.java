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

    private boolean flag;
    private boolean ordenar = true;
    private ArrayList<GraphicalData> graphicalData = new ArrayList<>();

    public FileParser() {
    }

    public ArrayList<GraphicalData> getGraphicalData() {
        return graphicalData;
    }

    public void setGraphicalData(ArrayList<GraphicalData> graphicalData) {
        this.graphicalData = graphicalData;
    }

    public void readProbabilistcModelFile() {
        HashMap<String, Integer> probabilitiesReader = new HashMap<>();

        JFileChooser filechooser = new JFileChooser(
                new File("/home/fabinhosano/Dropbox/Iniciação Científica/Modelos"));
        filechooser.showOpenDialog(null);

        ConfigurationReader reader = new ConfigurationReader();
        ProbabilisticModelParameters params = reader.load(filechooser.getSelectedFile().getAbsolutePath());

        //leitura do nome do modelo probabilistico
        ProbabilisticModelParameterValue v = params.getMandatoryParameterValue("model_name");
        String modelName = v.getString();
        System.out.println(modelName);

        //leitura do alfabeto do modelo probabilitisco
        v = params.getMandatoryParameterValue("alphabet");
        ArrayList<String> alpha = v.getStringVector();

        v = params.getMandatoryParameterValue("probabilities");
        HashMap<String, Double> prob_map = v.getDoubleMap();

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
