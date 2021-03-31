package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.TripleDataSetMemory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;


/**
 * Parser built with the <a href="https://github.com/nxparser">nxparser framework</a>.
 */
public class NxMemoryWalkGenerator extends MemoryWalkGenerator {


    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NxMemoryWalkGenerator.class);

    /**
     * The default number of lines to check.
     * If the first X lines cannot be parsed, the parsing process will be cancelled.
     */
    public static final int DEFAULT_CHECK_LINES = 5;

    /**
     * The number of lines to check.
     * If the first X lines cannot be parsed, the parsing process will be cancelled.
     */
    private int linesToCheck = DEFAULT_CHECK_LINES;

    /**
     * Constructor
     */
    public NxMemoryWalkGenerator() {
        data = new TripleDataSetMemory();
        uriShortenerFunction = s -> s;
    }

    /**
     * Constructor
     *
     * @param nTripleFilePath File to be parsed.
     * @param uriShortenerFunction The URI shortener function which maps from String to String.
     */
    public NxMemoryWalkGenerator(String nTripleFilePath, UnaryOperator<String> uriShortenerFunction) {
        this(new File(nTripleFilePath), uriShortenerFunction);
    }

    /**
     * Constructor
     *
     * @param nTripleFilePath File to be parsed.
     */
    public NxMemoryWalkGenerator(String nTripleFilePath) {
        this(new File(nTripleFilePath), false);
    }

    /**
     * Constructor
     *
     * @param nTripleFilePath File to be parsed.
     * @param isParseDatatypeTriples True if datatype triples shall also be parsed.
     */
    public NxMemoryWalkGenerator(String nTripleFilePath, boolean isParseDatatypeTriples) {
        this(new File(nTripleFilePath), isParseDatatypeTriples);
    }

    /**
     * Constructor
     *
     * @param nTripleFile File to be parsed.
     * @param uriShortenerFunction The URI shortener function which maps from String to String.
     */
    public NxMemoryWalkGenerator(File nTripleFile, UnaryOperator<String> uriShortenerFunction) {
        this.uriShortenerFunction = uriShortenerFunction;
        readNtriples(nTripleFile);
    }

    /**
     * Constructor
     * @param nTripleFile The N-TRIPLES file.
     */
    public NxMemoryWalkGenerator(File nTripleFile){
        this(nTripleFile, false);
    }

    /**
     * Constructor
     *
     * @param nTripleFile File to be parsed.
     * @param isParseDatatypeTriples True if datatype properties shall also be parsed.
     */
    public NxMemoryWalkGenerator(File nTripleFile, boolean isParseDatatypeTriples) {
        this();
        this.setParseDatatypeProperties(isParseDatatypeTriples);
        readNtriples(nTripleFile);
    }

    /**
     * Read n-triples from the given file.
     *
     * @param filePathToReadFrom File from which will be read.
     */
    public void readNTriples(String filePathToReadFrom){
        readNtriples(new File(filePathToReadFrom));
    }

    /**
     * Read n-triples from the given file.
     *
     * @param fileToReadFrom File from which will be read.
     */
    public void readNtriples(File fileToReadFrom) {
        if (!fileToReadFrom.exists()) {
            LOGGER.error("File does not exist. Cannot parse.");
            return;
        }
        if (!sanityCheck(fileToReadFrom)) {
            LOGGER.error("File cannot be parsed by NxParser.");
            return;
        }
        if (fileToReadFrom.getName().endsWith(".nt") || fileToReadFrom.getName().endsWith(".ttl") || fileToReadFrom.getName().endsWith(".nq")) {
            NxParser parser = new NxParser();
            try {
                parser.parse(new InputStreamReader(new FileInputStream(fileToReadFrom), StandardCharsets.UTF_8));
                String subject, predicate, object;
                for (Node[] nx : parser) {

                    if(isParseDatatypeProperties && nx[2].toString().startsWith("\"")){
                        // the current triple is a datatype triple
                        subject = uriShortenerFunction.apply(removeTags(nx[0].toString()));
                        predicate = uriShortenerFunction.apply(removeTags(nx[1].toString()));
                        object = getTextProcessingFunction().apply(nx[2].toString());
                        data.addDatatypeTriple(subject, predicate, object);
                        continue;
                    } else if (nx[2].toString().startsWith("\"")) continue;

                    subject = uriShortenerFunction.apply(removeTags(nx[0].toString()));
                    predicate = uriShortenerFunction.apply(removeTags(nx[1].toString()));
                    object = uriShortenerFunction.apply(removeTags(nx[2].toString()));
                    data.addObjectTriple(subject, predicate, object);
                }
            } catch (FileNotFoundException fnfe) {
                LOGGER.error("Could not find file " + fileToReadFrom.getAbsolutePath(), fnfe);
            }
        }
    }

    /**
     * Check if the file can be parsed by only parsing the first few lines.
     * If this fails, the method will return false.
     *
     * @param fileToCheck The file that shall be checked.
     * @return False if file is not ok for this parser else true.
     */
    private boolean sanityCheck(File fileToCheck) {
        List<String> lines = new ArrayList<>();
        try {
            LineIterator lineIterator = FileUtils.lineIterator(fileToCheck, "UTF-8");
            int i = 0;
            while (i < this.linesToCheck && lineIterator.hasNext()) {
                lines.add(lineIterator.nextLine());
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        NxParser parser = new NxParser();
        parser.parse(lines);
        int size = 0;
        for (Node[] nx : parser) {
            size++;
        }
        if (size == 0) {
            return false;
        } else return true;
    }

    public int getLinesToCheck() {
        return linesToCheck;
    }

    public void setLinesToCheck(int linesToCheck) {
        if (linesToCheck < 0) {
            LOGGER.error("linesToCheck must be > 0. Using default: " + DEFAULT_CHECK_LINES);
            this.linesToCheck = DEFAULT_CHECK_LINES;
        } else {
            this.linesToCheck = linesToCheck;
        }
    }
}
