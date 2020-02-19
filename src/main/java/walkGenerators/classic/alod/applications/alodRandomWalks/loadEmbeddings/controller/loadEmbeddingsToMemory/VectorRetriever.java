package walkGenerators.classic.alod.applications.alodRandomWalks.loadEmbeddings.controller.loadEmbeddingsToMemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import walkGenerators.classic.alod.applications.alodRandomWalks.generationInMemory.model.StringDoubleTuple;


import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import static walkGenerators.classic.alod.services.tools.math.MathOperations.cosineSimilarity;


/**
 * A class which provides the service of loading RDF2Vec vectors into a Java HashMap.
 */
public class VectorRetriever {

    private static Logger LOG = LoggerFactory.getLogger(VectorRetriever.class);

    /**
     * Static method which returns a HashMap with terms and associated vectors.
     * @param vectorFile File where the vectors are stored. Note that the target file is not the plain gensim output
     *                   but that it is assumed that some preprocessing already occurred (see associated python project).
     * @param vectorSize Size of the vector.
     * @return A HashMap where the key is the term and the value is the vector represented as double array.
     */
    public static HashMap<String, Double[]> retrieveVectors(File vectorFile, int vectorSize){
        HashMap<String, Double[]> vectors = new HashMap<>(850000);
        try {
            LOG.info("Loading Vector File: " + vectorFile);
            BufferedReader reader = new BufferedReader(new FileReader(vectorFile));
            String line = "";
            int readCount = 0;
            while((line = reader.readLine()) != null){
                String[] components = line.split(" ");
                String key = "";
                Double[] vector = new Double[vectorSize];
                int iterationNumber = 0;
                for(String component : components){
                    if(iterationNumber == 0){
                        key = component
                                .replace("_", " ")
                                .replace("+", " ")
                                .replace("isa:", "");
                        key = key.trim();
                    } else {
                        vector[iterationNumber-1] = Double.parseDouble(component);
                    }
                    iterationNumber++;
                } // end of loop over split components
                vectors.put(key, vector);
                readCount++;
                if(readCount % 100000 == 0){
                    LOG.info(readCount + " lines read.");
                }
            } // end of loop over lines
            LOG.info("Done Loading Vectors into Memory");
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vectors;
    }


    // (overloaded for convenience:)
    /**
     * Static method which returns a hashmap with terms and associated vectors.
     * @param vectorFilePath File where the vectors are stored. Note that the target file is not the plain gensim output
     *                       but that it is assumed that some preprocessing already occurred (see associated python project).
     * @param vectorSize Sie of the vector.
     * @return A HashMap where the key is the term and the value is the vector represented as double array.
     */
    public static HashMap<String, Double[]> retrieveVectors(String vectorFilePath, int vectorSize){
        return retrieveVectors(new File(vectorFilePath), vectorSize);
    }


    /**
     * Find the closest related concepts. Note: This method scales quadratically and can become very expensive.
     * Note: This method is just for small data sets and for testing. As all vectors are copied it is heavy on storage.
     * @param mapOfVectors Map of vectors for which the distance shall be calculated.
     * @param vectoryKey Key of the vector in question.
     * @param vectorValue Vector of the vector in question.
     * @return Sorted ArrayList with concepts and similarity score.
     */
    public static ArrayList<StringDoubleTuple> findClosestConcepts(HashMap<String, Double[]> mapOfVectors, String vectoryKey, Double[] vectorValue){
        ArrayList<StringDoubleTuple> result = new ArrayList<>();
        Iterator iterator = mapOfVectors.entrySet().iterator();
        while(iterator.hasNext()){
            HashMap.Entry<String, Double[]> otherVector = (HashMap.Entry<String, Double[]>) iterator.next();
            result.add(new StringDoubleTuple(otherVector.getKey(), cosineSimilarity(otherVector.getValue(), vectorValue)));
        }
        Collections.sort(result, Collections.reverseOrder());
        return result;
    }


    /**
     * Just for testing
     * @param args No args required.
     */
    public static void main(String[] args) {
        String pathToFile = "C:\\Users\\D060249\\OneDrive - SAP SE\\From Linux\\walks_20_2\\Embeddings\\DB2Vec_sg_200_5_5_15_2_500_java";
        int vectorSize = 200;
        int topResults = 20;

        HashMap<String, Double[]> vectors = retrieveVectors(pathToFile, vectorSize);

        String concept = "obama";
        int i = 0;
        for(StringDoubleTuple dt : findClosestConcepts(vectors, concept, vectors.get(concept))){
            System.out.println(dt.stringValue + " " + dt.doubleValue);
            i++;
            if(i == topResults){
                return;
            }
        }
    }

}

