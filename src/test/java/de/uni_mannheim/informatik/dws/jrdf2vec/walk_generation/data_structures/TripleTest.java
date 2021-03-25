package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TripleTest {

    @Test
    void testEquals() {
        Triple t1 = new Triple("A", "B", "C");
        Triple t2 = new Triple("C", "B", "A");
        Triple t3 = new Triple("C", "B", "A");
        assertNotEquals(t1, t2);
        assertEquals(t1, t1);
        assertEquals(t2, t2);
        assertEquals(t2, t3);
    }

    @Test
    void testHashCode() {
        Triple t1 = new Triple("A", "B", "C");
        Triple t2 = new Triple("C", "B", "A");
        assertTrue(t1.hashCode() == t2.hashCode());

    }
}