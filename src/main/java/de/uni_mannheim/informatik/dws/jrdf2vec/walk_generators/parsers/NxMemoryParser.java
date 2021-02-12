package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.parsers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.data_structures.TripleDataSetMemory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    public NxMemoryParser() {
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
     *
     * @param nTripleFilePath File to be parsed.
     */
    public NxMemoryParser(String nTripleFilePath, UnaryOperator<String> uriShortenerFunction) {
        this(new File(nTripleFilePath), uriShortenerFunction);
    }

    /**
     * Constructor
     *
     * @param nTripleFilePath File to be parsed.
     */
    public NxMemoryParser(String nTripleFilePath) {
        this(new File(nTripleFilePath));
    }

    /**
     * Constructor
     *
     * @param nTripleFile File to be parsed.
     */
    public NxMemoryParser(File nTripleFile, UnaryOperator<String> uriShortenerFunction) {
        this.uriShortenerFunction = uriShortenerFunction;
        readNtriples(nTripleFile);
    }

    /**
     * Constructor
     *
     * @param nTripleFile File to be parsed.
     */
    public NxMemoryParser(File nTripleFile) {
        this();
        readNtriples(nTripleFile);
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
                parser.parse(new FileInputStream(fileToReadFrom));
                String subject, predicate, object;
                for (Node[] nx : parser) {

                    if(isIncludeDatatypeProperties() && nx[2].toString().startsWith("\"")){
                        // the current triple is a datatype triple


                    } else if (nx[2].toString().startsWith("\"")) continue;

                    subject = uriShortenerFunction.apply(removeTags(nx[0].toString()));
                    predicate = uriShortenerFunction.apply(removeTags(nx[1].toString()));
                    object = uriShortenerFunction.apply(removeTags(nx[2].toString()));
                    data.add(subject, predicate, object);
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
