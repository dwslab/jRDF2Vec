package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.parsers;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.data_structures.TripleDataSetMemory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.function.UnaryOperator;


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
     */
    public NxMemoryParser(){
        data = new TripleDataSetMemory();
        uriShortenerFunction = new UnaryOperator<String>() {
            @Override
            public String apply(String s) {
                return s;
            }
        };
    }

    /**
     * Constructor
     * @param nTripleFilePath File to be parsed.
     */
    public NxMemoryParser(String nTripleFilePath, UnaryOperator<String> uriShortenerFunction){
        this(new File(nTripleFilePath), uriShortenerFunction);
    }

    /**
     * Constructor
     * @param nTripleFilePath File to be parsed.
     */
    public NxMemoryParser(String nTripleFilePath){
        this(new File(nTripleFilePath));
    }

    /**
     * Constructor
     * @param nTripleFile File to be parsed.
     */
    public NxMemoryParser(File nTripleFile, UnaryOperator<String> uriShortenerFunction){
        this.uriShortenerFunction = uriShortenerFunction;
        readNtriples(nTripleFile);
    }

    /**
     * Constructor
     * @param nTripleFile File to be parsed.
     */
    public NxMemoryParser(File nTripleFile){
        this();
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
        if(fileToReadFrom.getName().endsWith(".nt") || fileToReadFrom.getName().endsWith(".ttl") || fileToReadFrom.getName().endsWith(".nq")){
            NxParser parser = new NxParser();
            try {
                parser.parse(new FileInputStream(fileToReadFrom));

                String subject, predicate, object;
                for (Node[] nx : parser) {
                    if(nx[2].toString().startsWith("\"")) continue;
                    subject = uriShortenerFunction.apply(removeTags(nx[0].toString()));
                    predicate = uriShortenerFunction.apply(removeTags(nx[1].toString()));
                    object = uriShortenerFunction.apply(removeTags(nx[2].toString()));
                    data.add(subject, predicate, object);
                }
            } catch (FileNotFoundException fnfe){
                LOGGER.error("Could not find file " + fileToReadFrom.getAbsolutePath(), fnfe);
            }
        }
    }

}
