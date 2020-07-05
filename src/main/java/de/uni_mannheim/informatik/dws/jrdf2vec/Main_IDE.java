package de.uni_mannheim.informatik.dws.jrdf2vec;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.DummyWalkGenerator;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.NxMemoryParser;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.WalkGeneratorDefault;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.light.dbpedia.DBpediaWalkGeneratorLight;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.light.LightEntitySelector;

/**
 * Can be started in CLI using {@code -main} option.
 * Put the code here that you want to run.
 */
public class Main_IDE {

    public static void main(String[] args) {
        walkGenerationForWikidata();
    }


    public static void checkNxParser(){
        String filePath = "/work/jportisc/wikidata_2020_02_11/wikidata_truthy_2020_02_11.nt";
        NxMemoryParser parser = new NxMemoryParser(filePath,new DummyWalkGenerator());
        System.out.println("done");
    }

    public static void walkGenerationForWikidata(){
        // run on dws-07
        String pathToTripleFile = "/work/jportisc/wikidata_2020_02_11/wikidata_truthy_2020_02_11.nt";
        String fileToWrite = "./wikidata_walks_200_4/wikidata_200_4_.gz";
        int threads = 35;
        WalkGeneratorDefault walkGeneratorDefault = new WalkGeneratorDefault(pathToTripleFile);
        walkGeneratorDefault.generateRandomWalksDuplicateFree(threads, 200, 4, fileToWrite);
    }

    public static void walkGenerationForLightWikidata(){
        // TODO
    }


    public static void walkGenerationForLightDBpedia(){
        //BabelNetWalkGenerator walkGenerator = new BabelNetWalkGenerator();


        //WordNetWalkGenerator walkGenerator = new WordNetWalkGenerator("C:\\Users\\D060249\\Documents\\WordNet\\rdf\\wordnet.nt");
        //walkGenerator.generateRandomWalksDuplicateFree(80, 500, 8, "./walks/wordnet_500_4_df.gz");

        //DbnaryWalkGenerator walkGenerator = new DbnaryWalkGenerator("./dbnary_eng.nt");
        //walkGenerator.generateRandomWalksDuplicateFree(80, 500, 4, "./walks/dbnary_500_4_pages_df/dbnary_500_4_pages_df.gz");

        //WalkGeneratorLight generatorLight = new WalkGeneratorLight("./src/test/resources/swdf-2012-11-28.hdt", "./entities/FORBES_DBpedia_entities.txt");
        //generatorLight.generateRandomMidWalks(4, 100, 4);

        int numberOfThreads = 29;
        int numberOfWalksPerEntity = 500;
        int depth = 4;

        String pathToTripleFile = "/work/jportisc/dbpedia_rdf/unzipped_nt";

        // AAUP
        String pathToEntitesFile = "./entities/AAUP_DBpedia_entities.txt";
        String fileToBeWritten = "./dbpedia_aaup/walks/walks_aaup.txt.gz";
        DBpediaWalkGeneratorLight generatorLight = new DBpediaWalkGeneratorLight(pathToTripleFile, pathToEntitesFile);
        generatorLight.entitySelector = new LightEntitySelector(pathToEntitesFile);
        generatorLight.generateRandomMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, fileToBeWritten);

        // Forbes
        fileToBeWritten = "./dbpedia_forbes/walks/walks_forbes.txt.gz";
        pathToEntitesFile = "./entities/FORBES_DBpedia_entities.txt";
        generatorLight = new DBpediaWalkGeneratorLight(pathToTripleFile, pathToEntitesFile);
        generatorLight.entitySelector = new LightEntitySelector(pathToEntitesFile);
        generatorLight.generateRandomMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, fileToBeWritten);

        // METACRITIC Albums
        fileToBeWritten = "./dbpedia_metacritic_albums/walks/walks_albums.txt.gz";
        pathToEntitesFile = "./entities/METACRITIC_ALBUMS_DBpedia_entities.txt";
        generatorLight = new DBpediaWalkGeneratorLight(pathToTripleFile, pathToEntitesFile);
        generatorLight.entitySelector = new LightEntitySelector(pathToEntitesFile);
        generatorLight.generateRandomMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, fileToBeWritten);

        // METACRITIC Movies
        fileToBeWritten = "./dbpedia_metacritic_movies/walks/walks_movies.txt.gz";
        pathToEntitesFile = "./entities/METACRITIC_MOVIES_DBpedia_entities.txt";
        generatorLight = new DBpediaWalkGeneratorLight(pathToTripleFile, pathToEntitesFile);
        generatorLight.entitySelector = new LightEntitySelector(pathToEntitesFile);
        generatorLight.generateRandomMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, fileToBeWritten);

        // CITIES
        fileToBeWritten = "./dbpedia_cities/walks/walks_cities.txt.gz";
        pathToEntitesFile = "./entities/CITIES_DBpedia_entities.txt";
        generatorLight = new DBpediaWalkGeneratorLight(pathToTripleFile, pathToEntitesFile);
        generatorLight.entitySelector = new LightEntitySelector(pathToEntitesFile);
        generatorLight.generateRandomMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, fileToBeWritten);
    }


}
