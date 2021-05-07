package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector;

import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Selects HDT entities.
 */
public class HdtEntitySelector implements EntitySelector {


    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HdtEntitySelector.class);

    /**
     * The data set to be used by the parser.
     */
    HDT hdtDataSet;

    /**
     * Constructor
     *
     * @param hdtFilePath Path to the HDT file.
     * @throws IOException IOException
     */
    public HdtEntitySelector(String hdtFilePath) throws IOException {
        try {
            hdtDataSet = HDTManager.loadHDT(hdtFilePath);
        } catch (IOException e) {
            LOGGER.error("Failed to load HDT file: " + hdtFilePath + "\nProgramm will fail.", e);
            throw e;
        }
    }

    @Override
    public Set<String> getEntities() {
        HashSet<String> result = new HashSet<>();
        IteratorTripleString iterator;
        try {
            iterator = hdtDataSet.search("", "", "");
            TripleString ts;
            // TODO: We currently miss objects in this selector (can lead to vocab loss in some cases)
            while (iterator.hasNext()) {
                ts = iterator.next();
                result.add(ts.getSubject().toString());
            }
            return result;
        } catch (NotFoundException e) {
            LOGGER.error("Could not get HDT subjects. Returning null.");
            e.printStackTrace();
            return null;
        }
    }
}
