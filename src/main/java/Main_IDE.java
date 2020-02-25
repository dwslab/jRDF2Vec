import walkGenerators.light.DBpedia.DBpediaLightEntitySelector;
import walkGenerators.light.DBpedia.DBpediaWalkGeneratorLight;
import walkGenerators.light.LightEntitySelector;
import walkGenerators.light.WalkGeneratorLight;

/**
 * Can be started in CLI using {@code -main} option.
 */
public class Main_IDE {

    public static void main(String[] args) {


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

        String pathTotripleFile = "/work/jportisc/dbpedia_rdf/unzipped_nt";

        // AAUP
        String pathToEntitesFile = "./entities/AAUP_DBpedia_entities.txt";
        String fileToBeWritten = "./dbpedia_aaup/walks/walks_aaup.txt.gz";
        DBpediaWalkGeneratorLight generatorLight = new DBpediaWalkGeneratorLight(pathTotripleFile, pathToEntitesFile);
        generatorLight.entitySelector = new LightEntitySelector(pathToEntitesFile);
        generatorLight.generateRandomMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, fileToBeWritten);

        // Forbes
        fileToBeWritten = "./dbpedia_forbes/walks/walks_forbes.txt.gz";
        pathToEntitesFile = "./entities/FORBES_DBpedia_entities.txt";
        generatorLight = new DBpediaWalkGeneratorLight(pathTotripleFile, pathToEntitesFile);
        generatorLight.entitySelector = new LightEntitySelector(pathToEntitesFile);
        generatorLight.generateRandomMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, fileToBeWritten);

        // METACRITIC Albums
        fileToBeWritten = "./dbpedia_metacritic_albums/walks/walks_albums.txt.gz";
        pathToEntitesFile = "./entities/METACRITIC_ALBUMS_DBpedia_entities.txt";
        generatorLight = new DBpediaWalkGeneratorLight(pathTotripleFile, pathToEntitesFile);
        generatorLight.entitySelector = new LightEntitySelector(pathToEntitesFile);
        generatorLight.generateRandomMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, fileToBeWritten);

        // METACRITIC Movies
        fileToBeWritten = "./dbpedia_metacritic_movies/walks/walks_movies.txt.gz";
        pathToEntitesFile = "./entities/METACRITIC_MOVIES_DBpedia_entities.txt";
        generatorLight = new DBpediaWalkGeneratorLight(pathTotripleFile, pathToEntitesFile);
        generatorLight.entitySelector = new LightEntitySelector(pathToEntitesFile);
        generatorLight.generateRandomMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, fileToBeWritten);

        // CITIES
        fileToBeWritten = "./dbpedia_cities/walks/walks_cities.txt.gz";
        pathToEntitesFile = "./entities/CITIES_DBpedia_entities.txt";
        generatorLight = new DBpediaWalkGeneratorLight(pathTotripleFile, pathToEntitesFile);
        generatorLight.entitySelector = new LightEntitySelector(pathToEntitesFile);
        generatorLight.generateRandomMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, fileToBeWritten);

    }

}
