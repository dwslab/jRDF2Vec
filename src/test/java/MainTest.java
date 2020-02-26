import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of the command line functionality.
 */
class MainTest {

    @Test
    public void getHelp(){
        String result = Main.getHelp();
        assertNotNull(result);

        // print the help for manual inspection
        System.out.println(result);
    }

}