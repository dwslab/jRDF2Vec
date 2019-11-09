package walkGenerators;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BabelNetWalkGeneratorTest {

    @Test
    void shortenUri_2() {
        assertEquals("lemon:hello", BabelNetWalkGenerator.shortenUri_static("http://www.lemon-model.net/lemon#hello"));
        assertEquals("bn:hello_world", BabelNetWalkGenerator.shortenUri_static("http://babelnet.org/rdf/hello_world"));
        assertEquals("dc:europa", BabelNetWalkGenerator.shortenUri_static("http://purl.org/dc/europa"));
        assertEquals("rdf:european_union", BabelNetWalkGenerator.shortenUri_static("http://www.w3.org/1999/02/22-rdf-syntax-ns#european_union"));
        assertEquals("rdf:european_union", BabelNetWalkGenerator.shortenUri_static("http://www.w3.org/1999/02/22-rdf-syntax-ns#european_union"));
        assertEquals("http://www.example.com/my-super-example", BabelNetWalkGenerator.shortenUri_static("http://www.example.com/my-super-example"));
        assertEquals("hello", BabelNetWalkGenerator.shortenUri_static("hello"));
        assertEquals("bn:Laura_Fredducci_FR/s04721248n", BabelNetWalkGenerator.shortenUri_static("http://babelnet.org/rdf/Laura_Fredducci_FR/s04721248n"));
        assertEquals("bn:s11117647n\n", BabelNetWalkGenerator.shortenUri_static("http://babelnet.org/rdf/s11117647n\n"));

    }
}