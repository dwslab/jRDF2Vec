import de.uni_mannheim.informatik.dws.jrdf2vec.Main;
import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import java.io.File;

public class MultiTtlTest {


    private static final String WALK_DIR = "./multi-ttl_walk-dir";

    @Test
    void testMultiTtlCase(){
        // TODO this feature is not yet supported
        //File graphDirectory = Util.loadFile("multi_ttl");
        //assertNotNull(graphDirectory);
        //assertTrue(graphDirectory.exists());
        //Main.main(new String[]{"-graph", graphDirectory.getAbsolutePath(), "-walkDir", WALK_DIR});
    }

}
