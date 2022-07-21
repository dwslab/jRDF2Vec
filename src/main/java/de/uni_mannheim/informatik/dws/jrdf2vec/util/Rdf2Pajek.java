package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple class for converting any RDF file to a <a href="https://gephi.org/users/supported-graph-formats/pajek-net-format/">PajektNet</a> file.
 */
public class Rdf2Pajek {


    private static final Logger LOGGER = LoggerFactory.getLogger(Rdf2Pajek.class);

    static int vertexId;
    static Map<String, Integer> verticesMap;
    static Set<Pair<Integer, Integer>> links;

    public static void convert(File graphFile, File fileToWrite){
        // initialize global variables
        vertexId = 0;
        verticesMap = new HashMap<>();
        links = new HashSet<>();

        try {
            OntModel ontModel = Util.readOntology(graphFile);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite), StandardCharsets.UTF_8));

            for(Statement statement : ontModel.listStatements().toList()) {
                if (statement.getObject().isLiteral()) {
                    // we only want entity-entity relationships
                    continue;
                }
                int sourceId = addVertex(statement.getSubject());
                int targetId = addVertex(statement.getObject());
                links.add(new Pair(sourceId,targetId));
            }

            writer.write("*Vertices " + verticesMap.size() + "\n");
            for(Map.Entry<String, Integer> entry : verticesMap.entrySet()){
                writer.write(entry.getValue() + " \"" + entry.getKey() +  "\"\n");
            }
            writer.write("*arcs");
            for(Pair<Integer, Integer> entry : links){
                writer.write(entry.getValue0() + " " + entry.getValue1() + "\n");
            }
            writer.close();
        } catch (MalformedURLException e) {
            LOGGER.error("Could not load the ont model.", e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe){
            LOGGER.error("Could not write the result file.");
        }
    }

    private static int addVertex(RDFNode rdfNode){
        String vertexIdString;
        if(rdfNode.isAnon()){
            vertexIdString = rdfNode.asResource().getId().toString();
        } else {
            vertexIdString = rdfNode.asResource().getURI();
        }
        if(verticesMap.containsKey(vertexIdString)){
            return verticesMap.get(vertexIdString);
        } else {
            verticesMap.put(vertexIdString, ++vertexId);
            return vertexId;
        }
    }

}
