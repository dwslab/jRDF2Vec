package scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class BabelNetStatistics {

    public static void main(String[] args) {
        System.out.println("CONDITION PATTERN LINES: " + totalNumberOfBabelnetLines("C:\\Users\\D060249\\Downloads\\babelnet-3.6-RDFNT"));
    }


    public static long totalNumberOfBabelnetLines(String directoryPath){
        long result = 0;
        File directory = new File(directoryPath);
        if(!directory.isDirectory()){
            System.out.println("NOT A DIRECTORY - ABORT.");
            return result;
        }
        Pattern datatypePropertyPattern = Pattern.compile("\".*\"");
        Pattern glossPattern = Pattern.compile("_Gloss[0-9]"); // _Gloss[0-9]

        for(File file : directory.listFiles()){
            if(file.getName().endsWith(".gz")) {
                System.out.println("Processing file: " + file.getName());
                result += NumberOfLinesOfFile.numberOfLinesGivenCondition(file, x -> {
                    if (x.startsWith("#")) return true;
                    if(x.contains("http://purl.org/dc/terms/license")) return true;
                    if(x.contains("http://purl.org/dc/elements/1.1/source")) return true;
                    Matcher matcher = datatypePropertyPattern.matcher(x);
                    if(matcher.find()) return true;
                    Matcher glossMatcher = glossPattern.matcher(x);
                    if(glossMatcher.find()) return true;
                    return false;
                });
            }
        }
        return result;
    }

}
