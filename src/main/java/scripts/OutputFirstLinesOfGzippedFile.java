package scripts;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * Script that will print the first lines of a gzipped file.
 */
public class OutputFirstLinesOfGzippedFile {

    public static void main(String[] args) {
        long beginLineNumber = 0;
        long endLineNumber   = 2000;

        try {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("C:\\Users\\D060249\\Downloads\\babelnet-3.6-RDFNT\\part2_CC_BY_SA_30_URI.nt.gz"));
            BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
            String readLine;
            long lineNumber = 0;
            while ((readLine = br.readLine()) != null) {
                lineNumber++;
                if(lineNumber >= beginLineNumber) {
                    System.out.println(readLine);
                }
                if(lineNumber > endLineNumber){
                    break;
                }
            } // end of while loop

            System.out.println("done");

        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
