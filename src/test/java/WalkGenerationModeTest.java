import org.junit.jupiter.api.Test;
import walkGenerators.base.WalkGenerationMode;

import static org.junit.jupiter.api.Assertions.*;

class WalkGenerationModeTest {

    @Test
    void getOptions() {
        String result = WalkGenerationMode.getOptions();
        assertNotNull(result);
        assertFalse(result.endsWith(" "));
        assertFalse(result.endsWith("|"));
        System.out.println(result);
    }
}