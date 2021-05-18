package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.light.dbpedia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DBpediaLightEntitySelectorTest {


    @Test
    public void getRedirectUrl() {
        // DBpedia changes so we add multiple expected concepts for various versions
        String url = DBpediaLightEntitySelector.getRedirectUrl("http://dbpedia.org/resource/Hebei_Iron_and_Steel");
        assertTrue(url.equals("http://dbpedia.org/resource/Hesteel_Group") ||
                url.equals("http://dbpedia.org/resource/Hebei_Iron_and_Steel"));
        assertEquals("http://dbpedia.org/resource/Accor", DBpediaLightEntitySelector.getRedirectUrl("http://dbpedia.org/resource/Accor"));
    }
}