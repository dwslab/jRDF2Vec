package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MemoryWalkGeneratorTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryWalkGeneratorTest.class);

    @Test
    void getNumberOfAppearances(){
        List<String> myArray = new LinkedList<>(Arrays.asList("A", "B", "C", "A"));
        assertEquals(2, MemoryWalkGenerator.getNumberOfAppearances("A", myArray));
        assertEquals(1, MemoryWalkGenerator.getNumberOfAppearances("B", myArray));
        assertEquals(0, MemoryWalkGenerator.getNumberOfAppearances("D", myArray));
    }

    @Test
    void getRandomNumberBetweenOneAndX(){
        int count0 = 0;
        int count1 = 0;
        int count2 = 0;
        for(int i=0; i < 100; i++){
            int result = MemoryWalkGenerator.getRandomNumberBetweenZeroAndX(3);
            switch (result){
                case 0:
                    count0++;
                    break;
                case 1:
                    count1++;
                    break;
                case 2:
                    count2++;
                    break;
                default:
                    fail("Number not allowed: " + result);
            }
        }
        // for debugging:
        // System.out.println("Count 0: " + count0 + "\nCount 1: " + count1 + "\nCount 2: " + count2);
        assertEquals(100, count0 + count1 + count2);
    }

    @Test
    void randomDrawFromSet(){
        Set<String> set = new HashSet<>(Arrays.asList("A", "B", "C"));
        int aCount = 0;
        int bCount = 0;
        int cCount = 0;

        for (int i = 0; i < 1000; i++) {
            String drawValue = MemoryWalkGenerator.<String>randomDrawFromSet(set);
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

    @Test
    void randomDrawFromList() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("A", "B", "C"));
        int aCount = 0;
        int bCount = 0;
        int cCount = 0;

        for (int i = 0; i < 1000; i++) {
            String drawValue = MemoryWalkGenerator.<String>randomDrawFromList(list);
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