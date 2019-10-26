package walkGenerators.alod.services.scripts;

import java.io.*;

/**
 * This script outputs the first view lines of a file. This can be useful when a very large file cannot be opened in
 * a text editor.
 */
public class OutputFirstLinesOfFileApplication {


    public static void main(String[] args) {
        final String fileName = "./output/classic_all_concepts.txt";
        final int lines = 1000;

        //--------------------------------------------------------------------------------------------
        // Do not change code below
        //--------------------------------------------------------------------------------------------

        System.out.println("START\n\n");
        File f = new File(fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String readLine;
            int linesRead = 0;
            while((readLine = br.readLine()) != null){
                System.out.println(readLine);
                linesRead++;
                if(linesRead == lines){
                    break;
                }
            }
            br.close();
        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        System.out.println("DONE\n\n");
    }
}
