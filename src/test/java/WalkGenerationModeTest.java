import org.junit.jupiter.api.Test;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationMode;

import static org.junit.jupiter.api.Assertions.*;

class WalkGenerationModeTest {


    @Test
    void getOptions() {
        String result = WalkGenerationMode.getOptions();
        assertNotNull(result);
        assertFalse(result.endsWith(" "));
        assertFalse(result.endsWith("|"));
        //System.out.println(result);
    }

    @Test
    void assertImplementationOfGetModeFromString(){
        for(WalkGenerationMode mode : WalkGenerationMode.values()){
            assertNotNull(WalkGenerationMode.getModeFromString(mode.toString()), "Failure for " + mode.toString());
        }
    }
}