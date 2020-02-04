package walkGenerators.classic.alod.services.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Outputs a line where a specific string occurs.
 *
 */
public class FindSpecificLinesInFileApplication {

    public static void main(String[] args) {
        
    	String filePath = "./output/classic_all_concepts.txt";
        String termToFind = "squash";

        //------------------------------------------------------------------
        // no changes below this point
        //------------------------------------------------------------------
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));

            String readLine;
            while ((readLine = reader.readLine()) != null) {
                if (readLine.contains(termToFind)) {
                    System.out.println(readLine);
                }
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
