package walkGenerators.base;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import walkGenerators.dataStructure.TripleDataSetMemory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


/**
 * Parser built with the <a href="https://github.com/nxparser">nxparser framework</a>.
 */
public class NxMemoryParser extends MemoryParser {

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NxMemoryParser.class);


    /**
     * Constructor
     * @param walkGenerator Walk Generator to be used.
     */
    public NxMemoryParser(WalkGenerator walkGenerator){
        this.specificWalkGenerator = walkGenerator;
        data = new TripleDataSetMemory();
    }

    /**
     * Constructor
     * @param nTripleFilePath File to be parsed.
     * @param specificWalkGenerator Walk Generator to be used.
     */
    public NxMemoryParser(String nTripleFilePath, WalkGenerator specificWalkGenerator){
        this(new File(nTripleFilePath), specificWalkGenerator);
    }

    /**
     * Constructor
     * @param nTripleFile File to be parsed.
     * @param specificWalkGenerator Walk Generator to be used.
     */
    public NxMemoryParser(File nTripleFile, WalkGenerator specificWalkGenerator){
        this(specificWalkGenerator);
        readNtriples(nTripleFile);
    }


    /**
     * Read n-triples from the given file.
     * @param fileToReadFrom File from which will be read.
     */
    public void readNtriples(File fileToReadFrom){
        if (!fileToReadFrom.exists()) {
            LOGGER.error("File does not exist. Cannot parse.");
            return;
        }
        if(fileToReadFrom.getName().endsWith(".nt") || fileToReadFrom.getName().endsWith(".ttl")){
            NxParser parser = new NxParser();
            try {
                parser.parse(new FileInputStream(fileToReadFrom));

                String subject, predicate, object;
                for (Node[] nx : parser) {
                    if(nx[2].toString().startsWith("\"")) continue;
                    subject = specificWalkGenerator.shortenUri(removeTags(nx[0].toString()));
                    predicate = specificWalkGenerator.shortenUri(removeTags(nx[1].toString()));
                    object = specificWalkGenerator.shortenUri(removeTags(nx[2].toString()));
                    data.add(subject, predicate, object);
                }
            } catch (FileNotFoundException fnfe){
                LOGGER.error("Could not find file " + fileToReadFrom.getAbsolutePath(), fnfe);
            }
        }


    }



}
