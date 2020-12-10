package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.data_structures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TripleDataSetMemoryTest {

    @Test
    void add() {
        TripleDataSetMemory ds = new TripleDataSetMemory();
        ds.add("A", "B", "C");
        assertTrue(ds.getTriplesInvolvingObject("C").get(0).subject.equals("A"));
        assertTrue(ds.getTriplesInvolvingPredicate("B").get(0).subject.equals("A"));
        assertTrue(ds.getTriplesInvolvingSubject("A").get(0).object.equals("C"));

        // reference checks
        Triple t1 = new Triple("A", "B", "C");
        Triple t2 = new Triple("A", "B", "C");
        assertFalse(t1 == t2);

        Triple a = ds.getTriplesInvolvingSubject("A").get(0);
        Triple b = ds.getTriplesInvolvingPredicate("B").get(0);
        Triple c = ds.getTriplesInvolvingObject("C").get(0);
        assertTrue((a == b) && (b == c) );

        assertEquals(1, ds.getSize());
    }

    @Test
    void addAll(){
        TripleDataSetMemory ds = new TripleDataSetMemory();
        ds.add("A", "B", "C");
        ds.add("D", "E", "F");

        TripleDataSetMemory ds2 = new TripleDataSetMemory();
        ds2.add("A", "B", "C");
        ds2.add("D", "E", "G");

        ds.addAll(ds2);
        assertEquals(1, ds.getTriplesInvolvingSubject("A").size());
        assertEquals("B", ds.getTriplesInvolvingSubject("A").get(0).predicate);
        assertEquals("C", ds.getTriplesInvolvingSubject("A").get(0).object);

        assertEquals(2, ds.getTriplesInvolvingSubject("D").size());
        assertEquals("E", ds.getTriplesInvolvingSubject("D").get(0).predicate);
        assertTrue(ds.getTriplesInvolvingSubject("D").get(0).object == "F" || ds.getTriplesInvolvingSubject("D").get(0).object == "G");
        assertTrue(ds.getTriplesInvolvingSubject("D").get(1).object == "F" || ds.getTriplesInvolvingSubject("D").get(1).object == "G");
        assertFalse(ds.getTriplesInvolvingSubject("D").get(0).object.equals(ds.getTriplesInvolvingSubject("D").get(1).object));
    }
}