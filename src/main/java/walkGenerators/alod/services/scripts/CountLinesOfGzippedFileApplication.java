package walkGenerators.alod.services.scripts;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;


/**
 * Counts the lines of a gzipped file.
 *
 */
public class CountLinesOfGzippedFileApplication {

    public static void main(String[] args) {
        try {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("./output/xl_filter_10_entities.gz"));
            BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
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
