package walkGenerators.classic.alod.applications.alodRandomWalks.generationSPARQL;

import walkGenerators.classic.alod.applications.alodRandomWalks.generationSPARQL.controller.AlodRandomWalkGenerator;


/**
 * File generation using Apache Jena.
 */
@Deprecated
public class AlodRandomWalkApplication {

    /**
     * Main Method.
     * @param args
     */
    public static void main(String[] args) {

        //------------------------------------------------------------------------------------------
        // Set your parameters below.
        //------------------------------------------------------------------------------------------

        final boolean useTDB = true;
        String pathToRepo = "/home/D060249/WebIsALOD/tdb_dataset_3/";
        final int numberOfThreads = 40;

        final int numberOfWalks = 150;
        final int depth = 3;
        final String outputFileName = "myWalks.gz";


        //------------------------------------------------------------------------------------------
        // Do not change the code below.
        //------------------------------------------------------------------------------------------

        System.out.println("STARTING PROCESS");
        AlodRandomWalkGenerator walkGenerator = new AlodRandomWalkGenerator(numberOfWalks,depth, outputFileName);
        walkGenerator.setUseTDB(useTDB);

        /*
        HashSet<String> entities = new HashSet<>();
        entities.add("http://webisa.webdatacommons.org/concept/_office_");
        entities.add("http://webisa.webdatacommons.org/concept/_greenland_");
        walkGenerator.setEntities(entities);
        */

        long startTime = System.currentTimeMillis();
        walkGenerator.generateWalks(pathToRepo, numberOfThreads, -1);
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        System.out.println("PROCESS COMPLETED");

    }
}
