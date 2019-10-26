package walkGenerators.alod.services.scripts;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Outputs a line where a specific string occurs for a gzipped file.
 *
 */
public class FindSpecificLinesInGzippedFileApplication {

    public static void main(String[] args) {
        String filePath = "./output/classic_shortened_with_confidence.txt";
        String termToFind = "_";

        //------------------------------------------------------------------
        // no changes below this point
        //------------------------------------------------------------------
        
        try {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(new File(filePath)));
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));
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
