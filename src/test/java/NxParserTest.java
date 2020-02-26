import org.junit.jupiter.api.Test;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.rdfxml.RdfXmlParser;
import org.semanticweb.yars.turtle.TurtleParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class NxParserTest {

    @Test
    public void testNxParserBehavior() {
        testExecutionNx(NxParserTest.class.getClassLoader().getResource("pizza.owl.nt").getPath());
    }

    @Test
    public void testXmlParserBehavior(){
        testExecutionXml(NxParserTest.class.getClassLoader().getResource("pizza.owl.xml").getPath());
    }

    @Test
    public void testTtlParserBehavior(){
        testExecutionTtl(NxParserTest.class.getClassLoader().getResource("pizza.ttl").getPath());
    }


    /**
     * For repeated NX tests.
     * @param filePath The file path to the file to be tested.
     */
    public static void testExecutionTtl(String filePath){
        try {
            TurtleParser nxp = new TurtleParser();
            nxp.parse(new FileReader(new File(filePath)), new URI(("http://example.com")));

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
     * For experiments...
     * @param args Not required.
     * @throws Exception For manual experiments, no handling required here.
     */
    public static void main(String[] args) throws Exception {
        String filePath = "/src/test/resources/pizza.owl.nt‚";
        filePath = NxParserTest.class.getClassLoader().getResource("pizza.owl.nt").getPath();
        NxParser nxp = new NxParser();
        nxp.parse(new FileInputStream(new File(filePath)));

        for (Node[] nx : nxp) {
            // prints the subject, eg. <http://example.org/>
            System.out.println(nx[0]);
            System.out.println(nx[1]);
            System.out.println(nx[2]);
        }
    }

}
