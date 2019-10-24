package scripts;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Script that will print the first lines of a gzipped file.
 */
public class OutputFirstLinesOfFile {

    public static void main(String[] args) {
        try {
            File file = new File("./cache/babelnet_entities_en.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String readLine;
            long lineNumber = 0;

            while ((readLine = br.readLine()) != null) {
                lineNumber++;
                System.out.println(readLine);
                if(lineNumber > 100){
                    break;
                }
            } // end of while loop

            System.out.println("done");
            br.close();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
