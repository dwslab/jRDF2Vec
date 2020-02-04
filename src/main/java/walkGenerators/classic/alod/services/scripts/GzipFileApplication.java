package walkGenerators.classic.alod.services.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

/**
 * This helper method allows to gzip a file.
 *
 */
public class GzipFileApplication {

	public static void main(String[] args) {
		String filePathToRead = "D:\\Embeddings\\100_8_reverse_walks_window_3\\Classic_Reversed_SG_500_walks_100_depth_8_window_3_java";
		String filePathToWrite = "D:\\Embeddings\\100_8_reverse_walks_window_3\\Classic_Reversed_SG_500_walks_100_depth_8_window_3_java.gz";
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(filePathToRead)));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(new File(filePathToWrite)))));
			String readLine;
			while((readLine = reader.readLine()) != null) {
				writer.write(readLine + "\n");
			}
			writer.flush();
			writer.close();
			reader.close();
			System.out.println("DONE");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
