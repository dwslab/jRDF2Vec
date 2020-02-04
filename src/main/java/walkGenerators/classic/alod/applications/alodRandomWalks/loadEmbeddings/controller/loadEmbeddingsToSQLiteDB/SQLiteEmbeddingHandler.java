package walkGenerators.classic.alod.applications.alodRandomWalks.loadEmbeddings.controller.loadEmbeddingsToSQLiteDB;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import walkGenerators.classic.alod.applications.alodRandomWalks.loadEmbeddings.model.EmbeddingHandler;

import java.io.*;
import java.sql.*;
import java.util.HashMap;


/**
 * Handler for large SQL embeddings.
 * The handler can load embedding files into an SQLite database and retrieve individual embeddings later on.
 * This is implemented for cases in which there is not enough RAM available to load all embeddings into memory.
 */
public class SQLiteEmbeddingHandler implements EmbeddingHandler {


    private static Logger LOG = LoggerFactory.getLogger(SQLiteEmbeddingHandler.class);

    private String databaseDirectory = "./output/databases/"; // using Windows default, setter available
    private Connection connection = null;
    private int dimension = - 1;
    HashMap<String, Double[]> buffer = new HashMap<>();
    private String databaseFileName = "";
    private PreparedStatement preparedSelectStatement;
    private PreparedStatement preparedInsertStatement;


    /**
     * Initialize with dimension and database.
      * @param databaseFileName Name for the database file to be read or to be written.
     */
    public SQLiteEmbeddingHandler(String databaseFileName){
        setDatabaseFileName(databaseFileName);
    }

    /**
     * Create required tables if they do not exist yet.
     */
    private void initializeDatabase(){
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS embeddings (\n"
                + "	term String PRIMARY KEY,\n"
                + "	vector String NOT NULL\n"
                + ");";
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * Load a file into the database.
     * @param filePath Path to file.
     */
    public void loadFile(String filePath){

        // invalidate old buffer
        buffer = new HashMap<>();

        File vectorFile = new File(filePath);
        if(!vectorFile.exists()){
            LOG.error("The filePath specified does not exist. Nothing is loaded into the table.");
            return;
        }

        try {
            LOG.info("Loading Vector File: " + vectorFile);
            BufferedReader reader = new BufferedReader(new FileReader(vectorFile));
            String line = "";
            int readCount = 0;
            while ((line = reader.readLine()) != null) {
                String[] components = line.split(" ");
                String key = "";

                key = components[0]
                        .replace("_", " ")
                        .replace("+", " ")
                        .replace("isa:", "");
                key = key.trim();

                writeEmbeddingEntry(key, line.substring(components[0].length() + 1, line.length()));
                readCount++;
                if(readCount % 10000 == 0){
                    LOG.info(readCount + " lines read.");
                }
            } // end of loop over split components
            LOG.info("Done Loading Vectors into DB.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Will insert an entry into the database.
     * @param key
     * @param value
     */
    private void writeEmbeddingEntry(String key, String value){
        try{
            preparedInsertStatement.setString(1, key);
            preparedInsertStatement.setString(2, value);
            preparedInsertStatement.executeUpdate();
        } catch (SQLException e) {
            LOG.error("INSERT failed.");
            LOG.error("ERROR writing key: \"" + key + "\"");
            System.out.println(e.getMessage());
        }
    }


    /**
     * Obtain the embedding vector.
     * @param term The term for which the vector is to be retrieved.
     * @return The vector as double array.
     */
    public Double[] getVector(String term){
        if(buffer.containsKey(term)){
            return buffer.get(term);
        }
        if(dimension < 1){
            dimension = getDimension();
            if(dimension < 1){
                // still no result
                return null;
            }
        }
        Double result[] = new Double[dimension];
        try {
            preparedSelectStatement.setString(1, term);
            ResultSet resultSet  = preparedSelectStatement.executeQuery();
            boolean found = false;
            // loop through the result set
            while (resultSet.next()) {
                found = true;
                String vectorString[] = resultSet.getString("vector").trim().split(" ");
                for(int i = 0; i < vectorString.length; i++){
                    result[i] = Double.parseDouble(vectorString[i]);
                }
            }
            if(!found){
                result = null;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        buffer.put(term, result);
        return result;
    }
    
    
	@Override
	public double[] getVectorPrimitive(String term) {
		Double[] objectArray = getVector(term);
		if(objectArray != null) {
			return ArrayUtils.toPrimitive(objectArray);
		} else {
			return null;
		}
	}


    /**
     * Check whether there is an embedding for the specified concept.
     * @param term The concept for which existence is to be checked.
     * @return True if there is a concept, else false,
     */
    public boolean hasVector(String term){
        if(getVector(term) == null){
            return false;
        } else {
            return true;
        }
    }


    /**
     * Connect to a database. If the database does not exist it will be created.
     */
    public void connect() {
        String url = "jdbc:sqlite:" + databaseDirectory + this.databaseFileName;
        Connection connectionAttempt = null;
        boolean newDatabase = true;
        File dbFile = new File(databaseDirectory + "/" + this.databaseFileName);

        if(dbFile.exists()){
            newDatabase = false;
        }

        try {
            connectionAttempt = DriverManager.getConnection(url);
            if (connectionAttempt != null) {
                connection = connectionAttempt;
                if(newDatabase){
                    // initialize if not yet existent
                    initializeDatabase();
                    LOG.info("New empty database created.");
                }
                LOG.info("Connection established.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // prepare statement once (performance optimization)
        try {
            String sqlSelectString = "SELECT vector FROM embeddings WHERE term = ?";
            preparedSelectStatement = connection.prepareStatement(sqlSelectString);
            String sqlInsertString = "INSERT INTO embeddings(term,vector) VALUES(?,?)";
            preparedInsertStatement = connection.prepareStatement(sqlInsertString);
        } catch (SQLException ioe){
            LOG.error("Could not prepare statement. The handler is not functional.");
            ioe.printStackTrace();
        }
    }


    /**
     * Close the connection to the database if open.
     */
    public void close(){
        if(connection != null) {
            try {
                connection.close();
                LOG.info("Connection closed.");
                return;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            LOG.info("No open connection. Nothing to close.se");
        }
    }


    /**
     * This method derives the embedding dimension given a SQLite database.
     * @return The dimension as int.
     */
    public int getDimension(){
        if(dimension < 1){
            // derive dimension
            int result = calculateDimension();
            if(result > 0){
                this.dimension = result;
                LOG.info("Dimension " + dimension + " inferred.");
                return result;
            } else {
                return  -1;
            }
        } else {
            return dimension;
        }
    }


    /**
     * Read the dimension from the data base.
     * @return Dimension. -1 if there is no dimension.
     */
    private int calculateDimension(){
        if(connection == null){
            LOG.error("Not connected.");
            return -1;
        } else {
            try {
                Statement s = connection.createStatement();
                ResultSet resultSet = s.executeQuery("SELECT vector FROM embeddings ORDER BY ROWID ASC LIMIT 1;");
                if (resultSet == null) {
                    LOG.error("Database empty.");
                    return -1;
                }
                if(resultSet.next()) {
                    String resultString = resultSet.getString("vector");
                    int result = resultString.trim().split(" ").length;
                    return result;
                } else {
                    LOG.error("Cannot get dimension. There seems to be no data.");
                    return -1;
                }

            } catch (SQLException sqle){
                LOG.error("Cannot get dimension.");
                sqle.printStackTrace();
                return -1;
            }

        }
    }




    //-----------------------------------------------------------------------------------------------
    // Only Getters and Setters below this Point
    //-----------------------------------------------------------------------------------------------

    public String getDatabaseDirectory() {
        return databaseDirectory;
    }

    public void setDatabaseDirectory(String databaseDirectory) {
        File f = new File(databaseDirectory);
        if(f.isDirectory()) {
            this.databaseDirectory = databaseDirectory;
        } else {
            LOG.error("Specified directory does not exist or is no directory.");
            LOG.error("The directory is not changed. Current directory: " + getDatabaseDirectory());
        }
    }

    public String getDatabaseFileName() {
        return databaseFileName;
    }

    public void setDatabaseFileName(String databaseFileName) {
        if(!databaseFileName.endsWith(".db")){
            databaseFileName = databaseFileName + ".db";
        }
        this.databaseFileName = databaseFileName;
        File databaseFile = new File("./output/databases/" + databaseFileName);
        if(!databaseFile.exists()){
            LOG.info("Database does not exist yet. You need to load data first. The Embedding Hanlder is operational but there is no data.");
        }
    }



}
