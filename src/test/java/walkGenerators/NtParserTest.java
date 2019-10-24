package walkGenerators;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NtParserTest {

    @org.junit.jupiter.api.Test
    void removeTags() {
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", NtParser.removeTags("<http://www.w3.org/ns/lemon/ontolex#LexicalEntry>"));
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", NtParser.removeTags("http://www.w3.org/ns/lemon/ontolex#LexicalEntry>"));
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", NtParser.removeTags("http://www.w3.org/ns/lemon/ontolex#LexicalEntry"));
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", NtParser.removeTags("<http://www.w3.org/ns/lemon/ontolex#LexicalEntry"));
    }


    public class DummyWalkGenerator extends WalkGenerator {

        @Override
        public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
            // do nothing
        }

        @Override
        public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
            // do nothing
        }

        @Override
        public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
            // do nothing
        }

        @Override
        public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
            // do nothing
        }

        @Override
        public String shortenUri(String uri) {
            return uri;
        }
    }


    @org.junit.jupiter.api.Test
    void generateWalkForEntity(){
        NtParser parser = new NtParser(getClass().getResource("/dummyGraph.nt").getFile(), new DummyWalkGenerator());
        List<String> result_1 = parser.generateWalksForEntity("A", 100, 8);
        System.out.println("Walks 1");
        for(String s : result_1) System.out.println(s);
        assertEquals(3, result_1.size());
        for(String s : result_1){
            assertTrue(s.equals("A P4 E P5 D") || s.equals("A P4 E P6 F") || s.equals("A P1 B P2 C P3 D"));
        }

        List<String> result_2 = parser.generateWalksForEntity("Z", 3, 8);
        System.out.println("\nWalks 2");
        for(String s : result_2) System.out.println(s);
        assertEquals(3, result_2.size());

        List<String> result_3 = parser.generateWalksForEntity("W", 100, 8);
        System.out.println("\nWalks 3");
        for(String s : result_3) System.out.println(s);
        assertEquals(7, result_3.size());
    }



}