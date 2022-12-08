package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.Triple;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.NtMemoryWalkGenerator;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

/**
 * Work in Progress.
 * {@link UniqueURIsToFile#uniqueURIsToFile(String, String)} and {@link UniqueURIsToFile#uniqueURIsToFileNt(String, String)}
 * are identical.
 */
public class UniqueURIsToFile {


    public static void uniqueURIsToFile(String fileToReadPath, String fileToWritePath) throws Exception {
        OntModel myOntology = Util.readOntology(new File(fileToReadPath));
        File fileToWrite = new File(fileToWritePath);

        Set<String> uris = new HashSet<>();
        StmtIterator iterator = myOntology.listStatements();
        while(iterator.hasNext()){
            Statement s = iterator.next();
            if(!s.getObject().isLiteral()) {
                uris.add(s.getSubject().getURI());
                uris.add(s.getPredicate().getURI());
                uris.add(s.getObject().asResource().getURI());
            }
        }

        try (
                OutputStreamWriter osw = new OutputStreamWriter(Files.newOutputStream(fileToWrite.toPath()), StandardCharsets.UTF_8);
                BufferedWriter bw = new BufferedWriter(osw);
        ) {
            for (String uri : uris) {
                bw.write(uri + "\n");
            }
        }
    }

    /**
     * Use {@link UniqueURIsToFile#uniqueURIsToFile(String, String)} instead (works also on non-NT files and is
     * equivalent in terms of its results).
     * @param fileToReadPath The file to read from.
     * @param fileToWritePath The file to be written to.
     * @throws Exception Any exception.
     */
    @Deprecated
    public static void uniqueURIsToFileNt(String fileToReadPath, String fileToWritePath) throws Exception {

        File fileToWrite = new File(fileToWritePath);
        NtMemoryWalkGenerator ntw = new NtMemoryWalkGenerator(new File(fileToReadPath));
        Set<String> uris = new HashSet<>(ntw.getData().getAllObjectTriples().size());
        for (Triple triple : ntw.getData().getAllObjectTriples()) {
            uris.add(triple.subject);
            uris.add(triple.predicate);
            uris.add(triple.object);
        }

        try (
                OutputStreamWriter osw = new OutputStreamWriter(Files.newOutputStream(fileToWrite.toPath()), StandardCharsets.UTF_8);
                BufferedWriter bw = new BufferedWriter(osw);
        ) {
            for (String uri : uris) {
                bw.write(uri + "\n");
            }
        }
    }

}
