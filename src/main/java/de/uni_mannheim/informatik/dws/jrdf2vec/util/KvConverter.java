package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class KvConverter {


    private static final Logger LOGGER = LoggerFactory.getLogger(KvConverter.class);

    public static void convert(File txtOrw2vFile, File fileToWrite){
        File w2vFile = null;
        if(txtOrw2vFile.getName().endsWith(".w2v")){
            LOGGER.info("Recognized w2v format. Converting to kv...");
            w2vFile = txtOrw2vFile;
        } else if(txtOrw2vFile.getName().endsWith(".txt")) {
            LOGGER.info("Recognized txt format. Will convert to w2v and then to kv.");
            w2vFile = new File(fileToWrite.getParentFile(), txtOrw2vFile.getName().substring(0,
                    (int) (txtOrw2vFile.getName().length()) - 4) + ".w2v");
            VectorTxtToW2v.convert(txtOrw2vFile, w2vFile);
        } else {
            LOGGER.error("Neither .txt nor .w2v file provided (make sure you use correct file endings). ABORTING " +
                    "program.");
            return;
        }
        Gensim.getInstance().convertW2vToKv(w2vFile.getAbsolutePath(), fileToWrite.getAbsolutePath());
    }

}
