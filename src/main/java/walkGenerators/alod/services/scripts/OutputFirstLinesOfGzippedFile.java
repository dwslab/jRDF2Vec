package walkGenerators.alod.services.scripts;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * Script that will print the first lines of a gzipped file.
 */
public class OutputFirstLinesOfGzippedFile {

    public static void main(String[] args) {
        long start = 0;
        long stop = 100000;
        String gzipPath = "C:\\Users\\D060249\\Downloads\\babelnet-3.6-RDFNT\\part3_CC_BY_NC_SA_30_URI.nt.gz";

        try {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(gzipPath));
            BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
            String readLine;
            long lineNumber = 0;
            while ((readLine = br.readLine()) != null) {
                if(lineNumber >= start){
                    System.out.println(readLine);
                }
                lineNumber++;
                if(lineNumber > stop){
                    break;
                }
            } // end of while loop

            System.out.println("done");

        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
