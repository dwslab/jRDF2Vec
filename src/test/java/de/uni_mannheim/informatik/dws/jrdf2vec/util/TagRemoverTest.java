package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TagRemoverTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(TagRemoverTest.class);

    @Test
    void removeTagsWriteNewFile(){
        File tagTxtFile = UtilTest.loadFile("txtVectorFileTags.txt");
        File fileToWrite = new File("./txtVectorFileNoTags.txt");
        fileToWrite.deleteOnExit();
        TagRemover.removeTagsWriteNewFile(tagTxtFile, fileToWrite);

        assertTrue(fileToWrite.exists());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileToWrite),
                StandardCharsets.UTF_8))){

            String line;
            int i = 0;
            while((line = reader.readLine()) != null){
                String[] tokens = line.split(" ");

                if(i == 0){
                    assertEquals("Freude",tokens[0]);
                    assertEquals("1.0", tokens[1]);
                    assertEquals("-5", tokens[2]);
                    assertEquals("3", tokens[3]);
                }
                if(i == 1){
                    assertEquals("schöner",tokens[0]);
                    assertEquals("5.0", tokens[1]);
                    assertEquals("-3.4", tokens[2]);
                    assertEquals("5.3", tokens[3]);
                }
                i++;
            }
            assertTrue(i > 2);
        } catch (IOException ioe){
            LOGGER.error("An exception occurred. Test will fail.", ioe);
            fail(ioe);
        }
        Util.deleteFile(fileToWrite);
    }

    @Test
    void removeTagsFromVectorLine(){
        assertEquals("concept 1 2 3\n", TagRemover.removeTagsFromVectorLine("<concept> 1 2 3"));;
        assertEquals("concept 1 2 3\n", TagRemover.removeTagsFromVectorLine("<concept> 1 2 3\n"));;
        assertEquals("concept 1 2 3\n", TagRemover.removeTagsFromVectorLine("concept 1 2 3\n"));;
        assertEquals("\n", TagRemover.removeTagsFromVectorLine(""));
        assertEquals("hello\n", TagRemover.removeTagsFromVectorLine("hello"));
    }

}