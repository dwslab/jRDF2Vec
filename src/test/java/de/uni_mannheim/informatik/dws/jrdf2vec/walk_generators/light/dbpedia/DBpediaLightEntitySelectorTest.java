package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.light.dbpedia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DBpediaLightEntitySelectorTest {

    @Test
    public void getRedirectUrl() {
        assertEquals("http://dbpedia.org/resource/Hesteel_Group", DBpediaLightEntitySelector.getRedirectUrl("http://dbpedia.org/resource/Hebei_Iron_and_Steel"));
        assertEquals("http://dbpedia.org/resource/Nielsen_Holdings", DBpediaLightEntitySelector.getRedirectUrl("http://dbpedia.org/resource/Nielsen_N.V."));
        assertEquals("http://dbpedia.org/resource/Accor", DBpediaLightEntitySelector.getRedirectUrl("http://dbpedia.org/resource/Accor"));
    }

}