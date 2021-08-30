package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.getDimensionalityFromVectorTextFile;
import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.getNumberOfNonBlancLines;

/**
 * Converts a vector.txt file as written by this framework (in the format of GloVe vectors) to the classic word2vec
 * format where the first line contains the number of elements and the dimension.
 *
 * The w2v file will be in UTF_8.
 */
public class VectorTxtToW2v {


    private static final Logger LOGGER = LoggerFactory.getLogger(VectorTxtToW2v.class);

    public static void convert(File vectorFile, File fileToWrite) {
        if(vectorFile == null || fileToWrite == null){
            LOGGER.error("Parameters 'vectorFile' and 'fileToWrite' must never be null. ABORTING program.");
            return;
        }
        if(!vectorFile.exists()){
            LOGGER.error("The provided vector file does not exist. ABORTING program.");
            return;
        }
        if(vectorFile.isDirectory()){
            LOGGER.error("The provided vector file is a directory, a file is expected. ABORTING program.");
            return;
        }

        try(
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(vectorFile),
                StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite),
                        StandardCharsets.UTF_8))
        ){
            String line;
            boolean isFirstLine = true;
            int lines = getNumberOfNonBlancLines(vectorFile);
            int dimension = getDimensionalityFromVectorTextFile(vectorFile);
            while((line = reader.readLine()) != null){
                if(isFirstLine){
                    writer.write(lines + " " + dimension + "\n");
                    isFirstLine = false;
                }
                writer.write(line + "\n");
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("FileNotFoundException. Could not complete program.", e);
        } catch (IOException e) {
            LOGGER.error("IOException occurred. Could not complete program.", e);
        }

    }

}
