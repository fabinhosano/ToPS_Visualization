package classes;

import java.util.ArrayList;
import org.jfree.data.general.DefaultPieDataset;

/**
 *
 * @author fabinhosano
 */
public class GraphicalData {
    
    private String name;
    private ArrayList<String> values = new ArrayList<>();
    private ArrayList<Double> probabilities = new ArrayList<>();

    public GraphicalData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    public ArrayList<Double> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(ArrayList<Double> probabilities) {
        this.probabilities = probabilities;
    }
    
    public String getValue(int pos) {
        return values.get(pos);
    }

    public void addValue(String value) {
        this.values.add(value);
    }
    
    public Double getProbabilitie(int pos) {
        return probabilities.get(pos);
    }

    public void addProbabilitie(double value) {
        this.probabilities.add(value);
    }
    
    public DefaultPieDataset generationDefaultPieDataset(){
        DefaultPieDataset defaultPieDataset = new DefaultPieDataset();
        for (int i=0; i < values.size() && i < probabilities.size(); i++) {
            defaultPieDataset.setValue(values.get(i), probabilities.get(i));
        }
        return defaultPieDataset;
    }
    
    public String getPathNode(){
        String pathNode = "";
        for(int i=0; i<name.length(); i++){
            if(name.charAt(i) != ' '){
                pathNode += name.charAt(i);
            }
        }
        return pathNode;
    }
    
}
