package walkGenerators.base;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NxMemoryParserTest {

    /**
     * Remark: dummyGraph.nt not parseable.
     */
    @org.junit.jupiter.api.Test
    void generateWalkForEntity(){
        NxMemoryParser parser = new NxMemoryParser(getClass().getResource("/dummyGraph_2.nt").getFile(), new DummyWalkGenerator());
        List<String> result_1 = parser.generateDuplicateFreeRandomWalksForEntity("A", 100, 8);
        System.out.println("Walks 1");
        for(String s : result_1) System.out.println(s);
        assertEquals(3, result_1.size());
        for(String s : result_1){
            assertTrue(s.equals("A P4 E P5 D") || s.equals("A P4 E P6 F") || s.equals("A P1 B P2 C P3 D"));
        }

        List<String> result_2 = parser.generateDuplicateFreeRandomWalksForEntity("Z", 3, 8);
        System.out.println("\nWalks 2");
        for(String s : result_2) System.out.println(s);
        assertEquals(3, result_2.size());

        List<String> result_3 = parser.generateDuplicateFreeRandomWalksForEntity("W", 100, 8);
        System.out.println("\nWalks 3");
        for(String s : result_3) System.out.println(s);
        assertEquals(7, result_3.size());
    }

}