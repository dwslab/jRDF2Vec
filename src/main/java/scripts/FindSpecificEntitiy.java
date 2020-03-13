package scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class FindSpecificEntitiy {

    public static void main(String[] args) throws Exception{

        String entityName = "Boolean_logic#Boolean_logic-n";

        String file = "C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\Experiments\\WordNet_RDF\\wordnet.nt";
        // String file = "./cache/wordnet_entities.txt";

        BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
        String line;
        while((line = reader.readLine()) != null){
            if(line.contains(entityName)) System.out.println(line);
        }
        reader.close();
    }
}
