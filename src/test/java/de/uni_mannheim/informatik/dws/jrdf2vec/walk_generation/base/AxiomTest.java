package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AxiomTest {


    @Test
    void instanceOfAxiom(){
        AClass aClass = new AClass();
        BClass bClass = new BClass();
        assertTrue(aClass instanceof IAxiomInterface);
        assertTrue(bClass instanceof IAxiomInterface);
    }

}
