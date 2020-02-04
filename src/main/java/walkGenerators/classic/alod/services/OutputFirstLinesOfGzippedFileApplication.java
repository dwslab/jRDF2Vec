package walkGenerators.classic.alod.services;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * This application outputs the first lines of a gzipped file.
 */
public class OutputFirstLinesOfGzippedFileApplication {
    public static void main(String[] args) {
    	
    	// define the path to your file
    	String filePath = "";
    	
    	//-------------------------------------------------------------------------
    	// No changes below this point
    	//-------------------------------------------------------------------------
    	
        try {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(filePath));
            BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
            String readLine;
            long lineNumber = 0;

            while ((readLine = br.readLine()) != null) {
                lineNumber++;
                System.out.println(readLine);
                if(lineNumber > 10000){
                    break;
                }
            } // end of while loop
            br.close();
            System.out.println("done");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
