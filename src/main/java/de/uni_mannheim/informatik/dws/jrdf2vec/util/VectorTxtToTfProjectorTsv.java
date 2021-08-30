package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Converts a vectors.txt file to two tsv files that can be used for visualization via the
 * <a href="http://projector.tensorflow.org/">tensorflow projector</a>.
 */
public class VectorTxtToTfProjectorTsv {


    private static final Logger LOGGER = LoggerFactory.getLogger(VectorTxtToTfProjectorTsv.class);

    /**
     * Write the vectors.tsv and metadata.tsv file given a vectors.txt file.
     * The files to be written are determined using
     * {@link VectorTxtToTfProjectorTsv#getDerivedVectorsFile(File)} and
     * {@link VectorTxtToTfProjectorTsv#getDerivedMetadataFile(File)} respectively.
     * @param vectorTxtFile The vectors.txt file as written by jRDF2vec.
     */
    public static void convert (File vectorTxtFile){
        if(vectorTxtFile == null || !vectorTxtFile.exists() || vectorTxtFile.isDirectory()){
            LOGGER.error("The provided vectorTxtFile does not exist or is a directory.\n" +
                    "ABORTING program.");
            return;
        }
        convert(vectorTxtFile, getDerivedVectorsFile(vectorTxtFile), getDerivedMetadataFile(vectorTxtFile));
    }

    public static File getDerivedVectorsFile(File vectorTxtFile){
        return new File(vectorTxtFile.getParentFile(),
                vectorTxtFile.getName().substring(0, vectorTxtFile.getName().length() - 4) +
                        "_vectors.tsv"
        );
    }

    public static File getDerivedMetadataFile(File vectorTxtFile){
        return new File(vectorTxtFile.getParentFile(),
                vectorTxtFile.getName().substring(0, vectorTxtFile.getName().length() - 4) +
                        "_metadata.tsv"
        );
    }

    /**
     * Converts a vector.txt file to a file that can be used for the
     * <a href="http://projector.tensorflow.org/">tensorflow projector</a>.
     * @param vectorTxtFile The vectors.txt file. Will only work for GloVe-style format, not for .w2v format.
     * @param vectorFileToWrite The vector file that shall be written.
     * @param metadataFileToWrite The metadata file that shall be written.
     */
    public static void convert(File vectorTxtFile, File vectorFileToWrite, File metadataFileToWrite){
        if(vectorTxtFile == null || vectorFileToWrite == null || metadataFileToWrite == null){
            LOGGER.error("One of the three provided files (vectorTxtFile, vectorFileToWrite, metadataFileToWrite)\n" +
                    "is null. ABORTING program.");
            return;
        }
        if(!vectorTxtFile.exists() || vectorTxtFile.isDirectory()){
            LOGGER.error("The provided vectorTxtFile does not exist or is a directory.\n" +
                    "ABORTING program.");
            return;
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(vectorTxtFile),
                        StandardCharsets.UTF_8));
                BufferedWriter vectorWriter =
                        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(vectorFileToWrite),
                                StandardCharsets.UTF_8));
                BufferedWriter metadataWriter =
                        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metadataFileToWrite),
                                StandardCharsets.UTF_8));
                ) {

                //metadataWriter.write("Labels\n");
                String line;
                while((line = reader.readLine()) != null){
                    String[] tokens = line.split(" ");
                    metadataWriter.write(tokens[0] + "\n");
                    StringBuilder sb = new StringBuilder();
                    boolean isFirst = true;
                    for(int i = 1; i < tokens.length; i++){
                        if(isFirst){
                            isFirst = false;
                        } else {
                            sb.append("\t");
                        }
                        sb.append(tokens[i]);
                    }
                    sb.append("\n");
                    vectorWriter.write(sb.toString());
                }
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found exception occurred. ABORTING program.", e);
        } catch (IOException e) {
            LOGGER.error("IOException occurred. ABORTING program.", e);
        }
    }

}
