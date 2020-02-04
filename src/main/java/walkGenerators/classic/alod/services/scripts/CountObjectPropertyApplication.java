package walkGenerators.classic.alod.services.scripts;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Given a gzipped RDF file the number of skos:broader relations is counted.
 *
 */
public class CountObjectPropertyApplication {

    public static void main(String[] args) {
    	// define your filepath to the gzipped file
        final String readFrom = "";

        // no changes below here
        
        File nquadXLfile = new File(readFrom);
        try {
            GZIPInputStream gzipInput = new GZIPInputStream(new FileInputStream(nquadXLfile));
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInput));
            String readLine;
            long lineCounter = 0;
            long broaderMatches = 0;
            while((readLine = reader.readLine()) != null){
                if(readLine.contains("skos/core#broader>")){
                   broaderMatches++;
                }
                lineCounter++;
                if(lineCounter % 1000000 == 0){
                    System.out.println(lineCounter);
                }
            }
            reader.close();
            System.out.println("\n\n\n");
            System.out.println("Broader: " + broaderMatches);
            System.out.println("All Lines: " + lineCounter);
        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
}
