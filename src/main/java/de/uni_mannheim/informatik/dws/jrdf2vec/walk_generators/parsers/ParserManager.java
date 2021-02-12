package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.parsers;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.EntitySelector;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.HdtEntitySelector;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.MemoryEntitySelector;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.OntModelEntitySelector;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.riot.Lang;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.readOntology;

/**
 * This class provides management (utility) functions for different parsers, for example when it comes to
 * deciding which parser to use for which file.
 */
public class ParserManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(ParserManager.class);

    /**
     * Given a triple file, this method determines the appropriate parser and entity selector.
     * @param tripleFile The triple file path of the file to be processed.
     * @return Pair with parser and entity selector.
     */
    public static Pair<IParser, EntitySelector> parseSingleFile(String tripleFile){
        return parseSingleFile(new File(tripleFile));
    }

    /**
     * Given a triple file, this method determines the appropriate parser and entity selector.
     * @param tripleFile The triple file to be processed.
     * @return Pair with parser and entity selector.
     */
    public static Pair<IParser, EntitySelector> parseSingleFile(File tripleFile){
        IParser parser = null;
        EntitySelector entitySelector = null;
        try {
            String pathToTripleFile = tripleFile.getAbsolutePath();
            String fileName = tripleFile.getName();
            if (fileName.toLowerCase().endsWith(".nt") | fileName.toLowerCase().endsWith(".nq")) {
                if(fileName.toLowerCase().endsWith(".nq")){
                    LOGGER.info("NQ File detected: Please note that the graph information will be skipped.");
                }
                try {
                    LOGGER.info("Using NxParser.");
                    parser = new NxMemoryParser(pathToTripleFile);
                    entitySelector = new MemoryEntitySelector(((NxMemoryParser) parser).getData());
                } catch (Exception e) {
                    LOGGER.error("There was a problem using the default NxParser. Retry with slower NtParser.");
                    parser = new NtMemoryParser(pathToTripleFile);
                    entitySelector = new MemoryEntitySelector(((NtMemoryParser) parser).getData());
                }
                if (((MemoryParser) parser).getDataSize() == 0L) {
                    LOGGER.error("There was a problem using the default NxParser. Retry with slower NtParser.");
                    parser = new NtMemoryParser(pathToTripleFile);
                    entitySelector = new MemoryEntitySelector(((NtMemoryParser) parser).getData());
                }
            } else if (fileName.toLowerCase().endsWith(".ttl")) {
                OntModel model = readOntology(pathToTripleFile, Lang.TTL);
                entitySelector = new OntModelEntitySelector(model);
                File newResourceFile = new File(tripleFile.getParent(), fileName.substring(0, fileName.length() - 3) + "nt");
                NtMemoryParser.saveAsNt(model, newResourceFile);
                parser = new NtMemoryParser(newResourceFile);
            } else if (fileName.toLowerCase().endsWith(".xml")) {
                OntModel model = readOntology(pathToTripleFile, Lang.RDFXML);
                entitySelector = new OntModelEntitySelector(model);
                File newResourceFile = new File(tripleFile.getParent(), fileName.substring(0, fileName.length() - 3) + "nt");
                //this.parser = new JenaOntModelMemoryParser(this.model, this);
                NtMemoryParser.saveAsNt(model, newResourceFile);
                parser = new NtMemoryParser(newResourceFile);
            } else if (fileName.toLowerCase().endsWith(".hdt") || fileName.toLowerCase().endsWith(".hdt.index.v1-1")) {
                LOGGER.info("HDT file detected. Using HDT parser.");
                try {
                    parser = new HdtParser(pathToTripleFile);
                    entitySelector = new HdtEntitySelector(pathToTripleFile);
                } catch (IOException ioe) {
                    LOGGER.error("Propagated HDT Initializer Exception", ioe);
                }
            }
            LOGGER.info("Model read into memory.");
        } catch (MalformedURLException mue) {
            LOGGER.error("Path seems to be invalid. Generator not functional.", mue);
        }
        return new Pair<>(parser, entitySelector);
    }

}
