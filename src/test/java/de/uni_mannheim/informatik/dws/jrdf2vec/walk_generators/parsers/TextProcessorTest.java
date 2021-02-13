package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.parsers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextProcessorTest {

    @Test
    void apply(){
       TextProcessor tp = new TextProcessor();
       assertEquals("hello world", tp.apply("\"Hello World!\"@de ."));
       assertEquals("hello world", tp.apply("\" Hello World!\". "));
       assertEquals("151", tp.apply("\"1.51\"^^<something> . "));
    }

}