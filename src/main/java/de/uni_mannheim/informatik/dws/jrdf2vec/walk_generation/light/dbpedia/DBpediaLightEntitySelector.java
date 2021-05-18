package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.light.dbpedia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.light.LightEntitySelector;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class DBpediaLightEntitySelector extends LightEntitySelector {


    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DBpediaLightEntitySelector.class);

    public DBpediaLightEntitySelector(String pathToEntityFile){
        super(pathToEntityFile);
    }

    public DBpediaLightEntitySelector(File entityFile) {
        super(entityFile);
    }

    public DBpediaLightEntitySelector(Set<String> entitiesToProcess) {
        super(entitiesToProcess);
    }

    /**
     * Reads the entities in the specified file into a HashSet.
     *
     * @param entityFile The file to be read from. The file must be UTF-8 encoded.
     * @return A HashSet of entities.
     */
    public static Set<String> readEntitiesFromFile(File entityFile) {
        Set<String> result = new HashSet<>();
        if(!entityFile.exists()){
            LOGGER.error("The specified entity file does not exist: " + entityFile.getName() + "\nProgram will fail.");
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(entityFile),
                    StandardCharsets.UTF_8));
            String readLine = "";
            while((readLine = reader.readLine()) != null){
                result.add(readLine);
                String alternativeUri = getRedirectUrl(readLine);
                if(!alternativeUri.equals(readLine)){
                    LOGGER.info("Alternative URI for " + readLine + " found: " + alternativeUri + "\n" +
                            "URI will be added to set of entities.");
                    result.add(alternativeUri);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read file.", e);
        }
        return result;
    }

    /**
     * Obtain potential redirection target URL.
     * @param originalUrl The original URL.
     * @return The URL to which the original URL redirects to - otherwise the original URL will be returned. Note that
     * '/page/' will be replaced with '/resource/' in the returned URL to allow for walk generation.
     */
    public static String getRedirectUrl(String originalUrl){
        try {
            URLConnection con = new URL(originalUrl).openConnection();
            LOGGER.debug("original url: " + con.getURL());
            con.connect();
            LOGGER.debug("connected url: " + con.getURL());
            InputStream is = con.getInputStream();
            String redirectedURL = con.getURL().toString();
            LOGGER.debug("redirected url: " + redirectedURL);
            redirectedURL = redirectedURL.replace("/page/", "/resource/");
            is.close();
            return redirectedURL;
        } catch (IOException ioe){
            LOGGER.warn("Problem with finding redirect URL for " + originalUrl);
            return originalUrl;
        }
    }

    @Override
    public Set<String> getEntities() {
        if(entitiesToProcess == null) {
            entitiesToProcess = readEntitiesFromFile(this.entityFile);
        }
        return this.entitiesToProcess;
    }
}
