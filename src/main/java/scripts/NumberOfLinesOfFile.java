package scripts;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Simple helper application.
 */
public class NumberOfLinesOfFile {

    public static void main(String[] args) {
        String filePath = "./cache/babelnet_entities_en.txt";
        try {
            File file = new File(filePath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String readLine;
            long lineNumber = 0;

            while ((readLine = br.readLine()) != null) {
                lineNumber++;
            } // end of while loop

            System.out.println(lineNumber + " lines.");
            br.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Obtain the numbers of a gzipped file if it were unzipped.
     * @param file File to be used.
     * @return Number of lines as long.
     */
    public static long numberOfLinesOfGzippedFile(File file){
        long result = 0;
        try {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
            BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
            while ((br.readLine()) != null) {
                result++;
            } // end of while loop
            br.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Obtain the numbers of a gzipped file if it were unzipped.
     * @param file File to be used.
     * @return Number of lines containing datatype properties as long.
     */
    public static long numberOfLinesOfDatatypeProperties(File file) {
        long result = 0;
        try {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
            BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
            String readLine = null;
            Pattern pattern = Pattern.compile("\".*\"");
            nextLine:
            while ((readLine = br.readLine()) != null) {
                if (readLine.trim().startsWith("#")) continue nextLine; // just a comment line
                if (readLine.trim().equals("")) continue nextLine; // empty line
                Matcher matcher = pattern.matcher(readLine);
                if (matcher.find()) {
                    result++;
                    continue nextLine;
                }
            }
            br.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Obtain the numbers of a gzipped file if it were unzipped.
     * @param file File to be used.
     * @param condition Condition a line must fulfill in order to be counted.
     * @return Number of lines that fulfill condition.
     */
    public static long numberOfLinesGivenCondition(File file, IsearchCondition condition) {
        long result = 0;
        try {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
            BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
            String readLine = null;
            nextLine:
            while ((readLine = br.readLine()) != null) {
                if (condition.isHit(readLine)) result++;
                else continue nextLine;
            }
            br.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

}
