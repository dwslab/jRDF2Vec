package walkGenerators.alod.services.scripts;

import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * In some cases, such as an unexpected program abort, the MapDB file can remain locked. This program helps to unlock the file.
 */
public class UnlockMapDBfileApplication {

	public static void main(String[] args) {
		DB db = DBMaker.fileDB("./output/databases/CBOW_200_CLASSIC_REV_100_8").fileLockDisable().make();
	}
}
