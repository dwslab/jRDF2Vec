package de.uni_mannheim.informatik.dws.jrdf2vec.walkGenerators.base;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MemoryParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryParserTest.class);

    @Test
    void randomDrawFromList() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList(new String[]{"A", "B", "C"}));
        int aCount = 0;
        int bCount = 0;
        int cCount = 0;

        for (int i = 0; i < 1000; i++) {
            String drawValue = MemoryParser.<String>randomDrawFromList(list);
            switch (drawValue) {
                case "A":
                    aCount++;
                    break;
                case "B":
                    bCount++;
                    break;
                case "C":
                    cCount++;
                    break;
                default:
                    fail("Invalid value: " + drawValue);
            }
        }
        assertTrue(aCount > 0, "A was never drawn.");
        assertTrue(bCount > 0, "B was never drawn.");
        assertTrue(cCount > 0, "C was never drawn.");
        LOGGER.info("A : B : C  :   " + aCount + " : " + bCount + " : " + cCount);
    }

}