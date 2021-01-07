package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.parsers;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.IsearchCondition;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.WalkGenerator;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.data_structures.TripleDataSetMemory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A parser for NT files. Mainly implemented to support {@link NtMemoryParser#getRandomTripleForSubject(String)} in
 * an efficient way.
 */
public class NtMemoryParser extends MemoryParser {

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NtMemoryParser.class);

    /**
     * returns true if a triple shall be excluded.
     */
    private IsearchCondition skipCondition;


    /**
     * Indicator whether an optimized file shall be written for quick parsing later on (will be written in ./optimized/)
     */
    boolean isWriteOptimizedFile = false;

    /**
     * Default Constructor
     * @param walkGenerator The walk generator is used to derive a data set specific URI shortener (if desired).
     *                      Therefore, method {@link WalkGenerator#shortenUri(String)} has to be implemented.
     */
    public NtMemoryParser(WalkGenerator walkGenerator) {
        data = new TripleDataSetMemory();

        specificWalkGenerator = walkGenerator;
        skipCondition = new IsearchCondition() {
            Pattern pattern = Pattern.compile("\".*\"");

            @Override
            public boolean isHit(String input) {
                if (input.trim().startsWith("#")) return true; // just a comment line
                if (input.trim().equals("")) return true; // empty line
                Matcher matcher = pattern.matcher(input);
                if (matcher.find()) return true;
                return false;
            }
        };
    }


    /**
     * Constructor
     *
     * @param pathToTripleFile The nt file to be read (not zipped).
     * @param walkGenerator Walk generator to be used.
     */
    public NtMemoryParser(String pathToTripleFile, WalkGenerator walkGenerator) {
        this(walkGenerator);
        readNTriples(pathToTripleFile);
    }

    /**
     * Constructor
     *
     * @param tripleFile The nt file to be read (not zipped).
     * @param walkGenerator Walk generator to be used.
     */
    public NtMemoryParser(File tripleFile, WalkGenerator walkGenerator) {
        this(walkGenerator);
        readNTriples(tripleFile, false);
    }

    /**
     * Save an ontModel as TTL file.
     *
     * @param ontModel              Model to Write.
     * @param filePathToFileToWrite File that shall be written.
     */
    public static void saveAsNt(OntModel ontModel, String filePathToFileToWrite) {
        saveAsNt(ontModel, new File(filePathToFileToWrite));
    }

    /**
     * Save an ontModel as TTL file.
     *
     * @param ontModel    Model to Write.
     * @param fileToWrite File that shall be written.
     */
    public static void saveAsNt(OntModel ontModel, File fileToWrite) {
        try {
            ontModel.write(new FileWriter(fileToWrite), "N-Triples");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Read the given nt file into memory.
     *
     * @param pathToFile Path to the file.
     */
    public void readNTriples(String pathToFile) {
        readNTriples(pathToFile, false);
    }


    /**
     * Will load all .nt and .gz files from the given directory.
     *
     * @param pathToDirectory The directory where the n-triple files reside.
     */
    public void readNTriplesFilesFromDirectory(String pathToDirectory) {
        File directoryOfDataSets = new File(pathToDirectory);
        if (!directoryOfDataSets.isDirectory()) {
            LOGGER.error("The given pathToDirectory is no directory, aborting. (given: " + pathToDirectory + ")");
            return;
        }
        for (File file : directoryOfDataSets.listFiles()) {
            LOGGER.info("Processing file " + file.getName());
            if (file.getName().endsWith(".gz")) {
                readNTriples(file, true);
            } else if (file.getName().endsWith(".nt") || file.getName().endsWith(".ttl")) {
                readNTriples(file, false);
            } else {
                LOGGER.info("Skipping file: " + file.getName());
                continue;
            }
        }
    }


    /**
     * A new thread will be opened for each file.
     *
     * @param pathToDirectory The path to the directory in which the individual data files reside.
     * @param isWriteOptimizedFile Indicator whether an optimized file shall be written for quick parsing later on
     *                             (will be written in ./optimized/)
     */
    public void readNtTriplesFromDirectoryMultiThreaded(String pathToDirectory, boolean isWriteOptimizedFile) {
        this.readNtTriplesFromDirectoryMultiThreaded(new File(pathToDirectory), isWriteOptimizedFile);
    }




    /**
     * A new thread will be opened for each file.
     *
     * @param directoryOfDataSets The directory in which the individual data files reside.
     * @param isWriteOptimizedFile Indicator whether an optimized file shall be written for quick parsing later on
     *                             (will be written in ./optimized/)
     */
    public void readNtTriplesFromDirectoryMultiThreaded(File directoryOfDataSets, boolean isWriteOptimizedFile) {
        String pathToDirectory = directoryOfDataSets.getAbsolutePath();
        this.isWriteOptimizedFile = isWriteOptimizedFile;
        if (!directoryOfDataSets.isDirectory()) {
            LOGGER.error("The given pathToDirectory is no directory, aborting. (given: " + pathToDirectory + ")");
            return;
        }

        HashMap<String, File> optimizedFiles = new HashMap<>();
        // check for optimized files
        File optimizedDirectory = new File("./optimized");
        if (optimizedDirectory.exists() && optimizedDirectory.isDirectory()) {
            LOGGER.info("Found optimized directory. Will use it for reading.");
            for (File optimizedFile : optimizedDirectory.listFiles()) {
                optimizedFiles.put(optimizedFile.getName(), optimizedFile);
            }
        }

        ArrayList<Thread> allThreads = new ArrayList<>();
        for (File fileOriginal : directoryOfDataSets.listFiles()) {
            if (optimizedFiles.containsKey(fileOriginal.getName())) {
                LOGGER.info("Found optimized file for " + fileOriginal.getName() + ", will use that one.");
                FileReaderThread zThread = new FileReaderThread(this, optimizedFiles.get(fileOriginal.getName()), true, true);
                zThread.start();
                allThreads.add(zThread);
            } else if (fileOriginal.getName().endsWith(".gz")) {
                FileReaderThread zThread = new FileReaderThread(this, fileOriginal, true, false);
                zThread.start();
                allThreads.add(zThread);
            } else if (fileOriginal.getName().endsWith(".nt") || fileOriginal.getName().endsWith(".ttl")) {
                FileReaderThread zThread = new FileReaderThread(this, fileOriginal, false, false);
                zThread.start();
                allThreads.add(zThread);
            } else {
                LOGGER.info("Skipping file: " + fileOriginal.getName());
                continue;
            }
        }

        // wait for thread completion
        try {
            for (Thread thread : allThreads) {
                thread.join();
            }
        } catch (InterruptedException ie) {
            LOGGER.error("Problem waiting for thread...", ie);
        }
        LOGGER.info("Data read.");
    }


    /**
     * Thread that allows concurrent file parsing (used for data sets that consist of multiple, potentially zipped
     * files).
     */
    class FileReaderThread extends Thread {

        public FileReaderThread(NtMemoryParser parser, File fileToRead, boolean gzipped, boolean optimized) {
            this.fileToRead = fileToRead;
            this.parser = parser;
            this.isGzipped = gzipped;
            this.isOptimizedFile = optimized;
        }

        private boolean isOptimizedFile;
        private NtMemoryParser parser;
        private File fileToRead;
        private boolean isGzipped;

        @Override
        public void run() {
            if (!isOptimizedFile) {
                LOGGER.info("STARTED thread for file " + fileToRead.getName());
                parser.readNTriples(fileToRead, isGzipped);
            } else {
                LOGGER.info("STARTED (optimized) thread for file " + fileToRead.getName());
                parser.readNTriplesOptimized(fileToRead);
            }
            LOGGER.info("Thread for file " + fileToRead.getName() + " completed.");
        }
    }


    /**
     * Read the given nt file into memory. This method will add the data in the file to the existing {@link NtMemoryParser#data} store.
     *
     * @param pathToFile    Path to the file.
     * @param isGzippedFile Indicator whether the given file is gzipped.
     */
    public void readNTriples(String pathToFile, boolean isGzippedFile) {
        File fileToReadFrom = new File(pathToFile);
        readNTriples(fileToReadFrom, isGzippedFile);
    }


    /**
     * read form an optimized file.
     * @param fileToReadFrom Optimized file.
     */
    public void readNTriplesOptimized(File fileToReadFrom) {
        if (!fileToReadFrom.exists()) {
            LOGGER.error("File does not exist. Cannot parse.");
            return;
        }
        try {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(fileToReadFrom));
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8));

            String readLine;
            //int lineNumber = 0;
            while ((readLine = reader.readLine()) != null) {
                //lineNumber += 1;
                String[] parsed = readLine.split(" ");
                if (parsed.length != 3) {
                    LOGGER.error("Problem with line: \n" + readLine);
                } else {
                    String subject = parsed[0];
                    String predicate = parsed[1];
                    String object = parsed[2];
                    data.add(subject, predicate, object);
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("Could not initialize optimized reader for file " + fileToReadFrom.getName());
        }
    }


    /**
     * Read the given nt file into memory. This method will add the data in the file to the existing {@link NtMemoryParser#data} store.
     *
     * @param fileToReadFrom the file.
     * @param isGzippedFile  Indicator whether the given file is gzipped.
     */
    public void readNTriples(File fileToReadFrom, boolean isGzippedFile) {
        if (!fileToReadFrom.exists()) {
            LOGGER.error("File does not exist. Cannot parse.");
            return;
        }

        BufferedWriter writer = null; // the writer used to write the optimized file
        if (isWriteOptimizedFile) {
            try {
                File fileToWrite = new File("./optimized/" + fileToReadFrom.getName());
                fileToWrite.getParentFile().mkdirs();
                GZIPOutputStream gzip = new GZIPOutputStream(new FileOutputStream(fileToWrite));
                writer = new BufferedWriter(new OutputStreamWriter(gzip, StandardCharsets.UTF_8));
                LOGGER.info("Writer initialized.");
            } catch (FileNotFoundException fnfe) {
                LOGGER.error("Could not initialize gzip output stream.", fnfe);
            } catch (IOException e) {
                LOGGER.error("Problem initializing gzip output stream.", e);
            }
        }

        Pattern datatypePattern = Pattern.compile("\".*");
        try {
            BufferedReader reader;
            if (isGzippedFile) {
                GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(fileToReadFrom));
                reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileToReadFrom), StandardCharsets.UTF_8));
            }
            String readLine;
            long lineNumber = 0;
            Matcher datatypeMatcher; // only required if datatype properties shall be included
            nextLine:
            while ((readLine = reader.readLine()) != null) {
                try {
                    lineNumber++;
                    if (skipCondition.isHit(readLine)) {
                        continue nextLine;
                    }

                    // remove the dot at the end of a statement
                    readLine = readLine.replaceAll("(?<=>)*[ ]*.[ ]*$", "");


                    if (isIncludeDatatypeProperties) {
                        datatypeMatcher = datatypePattern.matcher(readLine);
                        if (datatypeMatcher.find()) {
                            String datatypeValue = datatypeMatcher.group(0);
                            String newDatatypeValue = datatypeValue.replaceAll(" ", "_");
                            readLine = readLine.replace(datatypeValue, newDatatypeValue);
                        }
                    }

                    String[] spo = readLine.split(" ");
                    if (spo.length != 3) {
                        LOGGER.error("Error in file " + fileToReadFrom.getName() + " in line " + lineNumber + " while parsing the following line:\n" + readLine + "\n Required tokens: 3\nActual tokens: " + spo.length);
                        int i = 1;
                        for (String token : spo) {
                            LOGGER.error("Token " + i++ + ": " + token);
                        }
                        LOGGER.error("Line is ignored. Parsing continues.");
                        continue nextLine;
                    }
                    String subject = specificWalkGenerator.shortenUri(removeTags(spo[0])).intern();
                    String predicate = specificWalkGenerator.shortenUri(removeTags(spo[1]).intern());
                    String object = specificWalkGenerator.shortenUri(removeTags(spo[2])).intern();

                    data.add(subject, predicate, object);

                    if (isWriteOptimizedFile) {
                        writer.write(subject + " " + predicate + " " + object + "\n");
                    }

                } catch (Exception e) {
                    // it is important that the parsing continues no matter what happens
                    LOGGER.error("A problem occurred while parsing line number " + lineNumber + " of file " + fileToReadFrom.getName(), e);
                    LOGGER.error("The problem occurred in the following line:\n" + readLine);
                }
            } // end of while loop
            LOGGER.info("File " + fileToReadFrom.getName() + " successfully read. " + data.getSize() + " subjects loaded.");
            if (isWriteOptimizedFile) {
                writer.flush();
                writer.close();
            }
            reader.close();
        } catch (Exception e) {
            LOGGER.error("Error while parsing file.", e);
        }
    }

    public IsearchCondition getSkipCondition() {
        return skipCondition;
    }

    public void setSkipCondition(IsearchCondition skipCondition) {
        this.skipCondition = skipCondition;
    }

    /**
     * This method will remove a leading less-than and a trailing greater-than sign (tags).
     *
     * @param stringToBeEdited The string that is to be edited.
     * @return String without tags.
     */
    public static String removeTags(String stringToBeEdited) {
        if (stringToBeEdited.startsWith("<")) stringToBeEdited = stringToBeEdited.substring(1);
        if (stringToBeEdited.endsWith(">"))
            stringToBeEdited = stringToBeEdited.substring(0, stringToBeEdited.length() - 1);
        return stringToBeEdited;
    }


    /**
     * Note that this function will overwrite the skip condition.
     *
     * @param includeDatatypeProperties Indicator whether data type properties shall be included in the walk generation.
     */
    public void setIncludeDatatypeProperties(boolean includeDatatypeProperties) {
        LOGGER.warn("Overwriting skip condition.");
        skipCondition = new IsearchCondition() {
            @Override
            public boolean isHit(String input) {
                if (input.trim().startsWith("#")) return true; // just a comment line
                if (input.trim().equals("")) return true; // empty line
                return false;
            }
        };
        isIncludeDatatypeProperties = includeDatatypeProperties;
    }
}