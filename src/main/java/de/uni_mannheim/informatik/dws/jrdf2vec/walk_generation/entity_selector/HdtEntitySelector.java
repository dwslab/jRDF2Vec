package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector;

import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.dictionary.Dictionary;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.*;


import java.io.BufferedWriter;
import java.io.FileWriter;

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

    public HdtEntitySelector(HDT hdt) throws IOException {
        hdtDataSet = hdt;    
    }
    
    //V3
    @Override
    public Set<String> getEntities() {
        HashSet<String> result = new HashSet<>();
        try {
            Dictionary dict = hdtDataSet.getDictionary();
            long numShared = dict.getNshared();
            long numSubjects = dict.getNsubjects();
            long numObjects = dict.getNobjects();
    
            // Add shared entities 
            for (long i = 1; i <= numShared; i++) {
                String entity = dict.idToString(i, TripleComponentRole.SUBJECT).toString();
                result.add(entity);
            }
    
            // Add exclusive subjects
            for (long i = numShared + 1; i <= numSubjects; i++) {
                String subject = dict.idToString(i, TripleComponentRole.SUBJECT).toString();
                result.add(subject);
            }
    
            // Add exclusive objects
            for (long i = 1; i <= numObjects; i++) {
                String object = dict.idToString(i, TripleComponentRole.OBJECT).toString();
                result.add(object);
            }
            
            return result;
        } catch (Exception e) {
            LOGGER.error("Could not get HDT entities using indices. Returning null.", e);
            e.printStackTrace();
            return null;
        }
    }

}
