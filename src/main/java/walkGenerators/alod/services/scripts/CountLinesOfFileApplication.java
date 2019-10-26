package walkGenerators.alod.services.scripts;

import java.io.*;

/**
 * Counts the lines of a non-zipped file.
 *
 */
public class CountLinesOfFileApplication {

    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("./output/xl_plain_count_spaces.txt")));
            long lineNumber = 0;
            while (br.readLine() != null) {
                lineNumber++;
            }
            System.out.println("Lines: " + lineNumber);
            br.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
