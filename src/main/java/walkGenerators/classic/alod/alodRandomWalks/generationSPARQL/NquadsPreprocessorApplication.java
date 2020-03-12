package walkGenerators.classic.alod.alodRandomWalks.generationSPARQL;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NquadsPreprocessorApplication {

    public static void main(String[] args) {
        try {
            File readFile = new File(args[0]);
            File writeFile = new File(args[1]);
            if(!readFile.exists()){
                System.out.println("READ FILE (arg 0) does not exist.");
                return;
            }
            BufferedReader br = new BufferedReader(new FileReader(readFile));
            BufferedWriter bw = new BufferedWriter(new FileWriter(writeFile));
            Pattern pattern = Pattern.compile("<.[^>]*>");
            String s;
            while((s = br.readLine()) != null){
                String newS = "";
                Matcher matcher = pattern.matcher(s);
                int iteration = 0;
                int lastIndex = 0;
                boolean hasFound = false;
                while(matcher.find()){
                    if(iteration == 0) {
                        newS = s.substring(0, matcher.start());
                        hasFound = true;
                        newS = newS + s.substring(matcher.start(), matcher.end()).replace(" ", "_");
                        iteration++;
                        lastIndex = matcher.end();
                    } else {
                        newS = newS
                                + s.substring(lastIndex, matcher.start())
                                + s.substring(matcher.start(), matcher.end()).replace(" ", "_");
                        iteration++;
                        lastIndex = matcher.end();
                    }
                }
                if(hasFound){
                    newS = newS + s.substring(lastIndex, s.length());
                } else {
                    newS = s;
                }
                bw.write(newS + "\n");
            }
            bw.flush();
            bw.close();
            br.close();
            System.out.println("DONE");
        } catch (FileNotFoundException e) {
            System.out.println("FAILED");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("FAILED");
            e.printStackTrace();
        }
    }
}
