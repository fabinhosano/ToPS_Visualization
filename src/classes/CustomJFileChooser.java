package classes;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author fabinhosano
 */
public class CustomJFileChooser extends JFileChooser {

    FileNameExtensionFilter filter;
    
    public CustomJFileChooser(){
        super();
        
        //Instanciando um filtro para arquivos de texto .txt
        filter = new FileNameExtensionFilter("Arquivos de Texto", "txt");
        
        //Setando o filtro do JFileChooser Customizado para apenas arquivos texto.
        setFileFilter(filter);
        //Setando o filtro para não aceitar todos tipos de arquivo.
        setAcceptAllFileFilterUsed(false);
    }
    
}
