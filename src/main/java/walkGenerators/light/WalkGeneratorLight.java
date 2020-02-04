package walkGenerators.light;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import walkGenerators.classic.WalkGeneratorDefault;

import java.io.File;
import java.util.HashSet;

/**
 * Default Walk Generator for RDF2Vec Light.
 */
public class WalkGeneratorLight extends WalkGeneratorDefault {

    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(WalkGeneratorLight.class);

    public WalkGeneratorLight(File tripleFile, HashSet<String> entitiesToProcess) {
        super(tripleFile);
        super.entitySelector = new LightEntitySelector(entitiesToProcess);
    }

    public WalkGeneratorLight(String pathToTripleFile, HashSet<String> entitiesToProcess) {
        this(new File(pathToTripleFile), entitiesToProcess);
    }


//
//    /**
//     * Central OntModel
//     */
//    private OntModel model;
//
//    public WalkGeneratorLight(File tripleFile, HashSet<String> entitiesToProcess) {
//        this.entitiesToProcess = entitiesToProcess;
//
//        // identical to WalkGeneratorClassic (candidate for modularization)
//        String pathToTripleFile = tripleFile.getAbsolutePath();
//        if(tripleFile.isDirectory()){
//            LOGGER.error("You specified a directory, but a file needs to be specified as resource file. ABORT.");
//            return;
//        }
//        if(!tripleFile.exists()){
//            LOGGER.error("The resource file you specified does not exist. ABORT.");
//            return;
//        }
//        try {
//            String fileName = tripleFile.getName();
//            if(fileName.toLowerCase().endsWith(".nt")) {
//                this.model = readOntology(pathToTripleFile, "NT");
//                this.parser = new NtParser(pathToTripleFile, this);
//            } else if(fileName.toLowerCase().endsWith(".ttl")) {
//                this.model = readOntology(pathToTripleFile, "TTL");
//                File newResourceFile = new File(tripleFile.getParent(), fileName.substring(0, fileName.length()-3) + "nt");
//                NtParser.saveAsNt(this.model, newResourceFile);
//                this.parser = new NtParser(newResourceFile, this);
//            } else if (fileName.toLowerCase().endsWith(".xml")) {
//                this.model = readOntology(pathToTripleFile, "RDFXML");
//                File newResourceFile = new File(tripleFile.getParent(), fileName.substring(0, fileName.length()-3) + "nt");
//                NtParser.saveAsNt(this.model, newResourceFile);
//                this.parser = new NtParser(newResourceFile, this);
//            }
//            LOGGER.info("Model read into memory.");
//        } catch (MalformedURLException mue) {
//            LOGGER.error("Path seems to be invalid. Generator not functional.", mue);
//        }
//    }
//
//    public WalkGeneratorLight(String pathToTripleFile, HashSet<String> entitiesToProcess) {
//    }
//
//
//    @Override
//    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
//
//    }
//
//    @Override
//    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
//
//    }
//
//    @Override
//    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
//
//    }
//
//    @Override
//    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
//
//    }
//
//    /**
//     * No URI shortening for default walk generators.
//     * @param uri The uri to be transformed.
//     * @return Input = Output
//     */
//    @Override
//    public String shortenUri(String uri) {
//        return uri;
//    }


}
