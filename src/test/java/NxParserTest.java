import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.rdfxml.RdfXmlParser;
import org.semanticweb.yars.turtle.TurtleParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class NxParserTest {


    @Test
    public void testNxParserBehavior() {
        testExecutionNx(loadFile("pizza.owl.nt").getAbsolutePath());
    }

    @Test
    public void testXmlParserBehavior(){
        testExecutionXml(loadFile("pizza.owl.xml").getAbsolutePath());
    }

    @Test
    public void testTtlParserBehavior(){
        testExecutionTtl(loadFile("pizza.ttl").getAbsolutePath());
    }

    /**
     * For repeated NX tests.
     * @param filePath The file path to the file to be tested.
     */
    public static void testExecutionTtl(String filePath){
        try {
            TurtleParser nxp = new TurtleParser();
            nxp.parse(new FileReader(filePath), new URI(("http://example.com")));

            for (Node[] nx : nxp) {
                assertNotNull(nx[0]);
                assertNotNull(nx[1]);
                assertNotNull(nx[2]);

                assertFalse(nx[0].toString().startsWith("\""));
                assertFalse(nx[1].toString().startsWith("\""));

                // nx[2] may start with a string
                //if(nx[2].toString().startsWith("\"")) System.out.println(nx[2].toString());
            }
        } catch (Exception e) {
            fail("Exception was thrown.");
        }
    }

    /**
     * For repeated NX tests.
     * @param filePath The file path to the file to be tested.
     */
    public static void testExecutionXml(String filePath){
        try {
            RdfXmlParser nxp = new RdfXmlParser();
            nxp.parse(new FileInputStream(new File(filePath)), "http://example.com");

            for (Node[] nx : nxp) {
                assertNotNull(nx[0]);
                assertNotNull(nx[1]);
                assertNotNull(nx[2]);

                assertFalse(nx[0].toString().startsWith("\""));
                assertFalse(nx[1].toString().startsWith("\""));

                // nx[2] may start with a string
                //if(nx[2].toString().startsWith("\"")) System.out.println(nx[2].toString());
            }
        } catch (Exception e) {
            fail("Exception was thrown.");
        }
    }

    /**
     * For repeated NX tests.
     * @param filePath The file path to the file to be tested.
     */
    public static void testExecutionNx(String filePath){
        try {
            NxParser nxp = new NxParser();
            nxp.parse(new FileInputStream(new File(filePath)));

            for (Node[] nx : nxp) {
                assertNotNull(nx[0]);
                assertNotNull(nx[1]);
                assertNotNull(nx[2]);

                assertFalse(nx[0].toString().startsWith("\""));
                assertFalse(nx[1].toString().startsWith("\""));

                // nx[2] may start with a string
                //if(nx[2].toString().startsWith("\"")) System.out.println(nx[2].toString());
            }
        } catch (Exception e) {
            fail("Exception was thrown.");
        }
    }

    /**
     * Helper function to load files in class path that contain spaces.
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private File loadFile(String fileName){
        try {
            File result =  FileUtils.toFile(this.getClass().getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception){
            exception.printStackTrace();
            fail("Could not load file.");
            return null;
        }
    }
}
