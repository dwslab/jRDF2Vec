package walkGenerators.alod.services.scripts;

import walkGenerators.alod.services.scripts.model.StringString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * This Helper class allows to perform simple set operations on mapping files.
 *
 */
public class AlignmentResultSetServicesApplication{

	public static void main(String[] args) {

		String filePath1 = "./output/large_bio_cbow_200_result_5_best_threshold.rdf";
		String filePath2 = "./output/large_bio_cbow_200_result_6_best_threshold.rdf";
		AlignmentResultSetServicesApplication service = new AlignmentResultSetServicesApplication(filePath1, filePath2);

		System.out.println(service.isAlignment1oneToOne());
		System.out.println(service.isAlignment2oneToOne());
		
		System.out.println("NOT IN 1");
		service.printNotInSet1();
		System.out.println("\n");
		System.out.println("NOT IN 2");
		service.printNotInSet2();
		
	}
	
	
	File file1;
	File file2;
	HashSet<StringString> set1;
	HashSet<StringString> set2;
	
	/**
	 * Constructor
	 * @param path1 Path to first rdf file.
	 * @param path2 Path to second rdf file.
	 */
	public AlignmentResultSetServicesApplication(String path1, String path2) {
		this.file1 = new File(path1);
		this.file2 = new File(path2);
		if(!file1.exists() || !file2.exists()) {
			System.out.println("One of the files cannot be found. ABORT.");
		}
		set1 = getMappings(file1);
		set2 = getMappings(file2);
	}
	
	
	/**
	 * Print the IRI pairs that are not in set1 but in set2.
	 */
	public void printNotInSet1() {
		HashSet<StringString> tempSet = (HashSet) set2.clone();
		tempSet.removeAll(set1);
		tempSet.forEach(x -> System.out.println(x.string1 + "   " + x.string2));
	}
	
	
	/**
	 * Will return true if Alignment 1 ia a 1:1 alignment.
	 * @return
	 */
	public boolean isAlignment1oneToOne() {
		boolean result = true;
		HashSet<String> entities1 = new HashSet();
		HashSet<String> entities2 = new HashSet();
		for(StringString mapping : set1) {
			if(entities1.contains(mapping.string1)){
				result = false;
				System.out.println("Already mapped Entitiy 1: " + mapping.string1);
			} else {
				entities1.add(mapping.string1);
			}
			if(entities2.contains(mapping.string2)){
				result = false;
				System.out.println("Already mapped Entitiy 2: " + mapping.string2);
			} else {
				entities1.add(mapping.string2);
			}
		}
		return result;
	}
	
	
	/**
	 * Will return true if Alignment 2 ia a 1:1 alignment.
	 * @return
	 */
	public boolean isAlignment2oneToOne() {
		boolean result = true;
		HashSet<String> entities1 = new HashSet();
		HashSet<String> entities2 = new HashSet();
		for(StringString mapping : set2) {
			if(entities1.contains(mapping.string1)){
				result = false;
				System.out.println("Already mapped Entitiy 1: " + mapping.string1);
			} else {
				entities1.add(mapping.string1);
			}
			if(entities2.contains(mapping.string2)){
				result = false;
				System.out.println("Already mapped Entitiy 2: " + mapping.string2);
			} else {
				entities1.add(mapping.string2);
			}
		}
		return result;
	}
	
	
	/**
	 * Print the IRI pairs that are not in set2 but in set1.
	 */
	public void printNotInSet2() {
		HashSet<StringString> tempSet = (HashSet) set1.clone();
		tempSet.removeAll(set2);
		tempSet.forEach(x -> System.out.println(x.string1 + "   " + x.string2));
	}
	
	
	/**
	 * Load mapping IRI IDs into set.
	 * @param file File to read from. It is assumed that the file follows the alignment API.
	 * @return HashSet with IRI pairs that were mapped.
	 */
	private HashSet<StringString> getMappings(File file){
		HashSet<StringString> result = new HashSet();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			Pattern pattern = Pattern.compile("(?<=rdf:resource=').*(?='\\/>)"); // (?<=rdf:resource=').*(?='\/>)
			String readLine;
			Matcher matcher;
			String e1 = null;
			String e2 = null;
			while((readLine = reader.readLine()) != null) {
				if(readLine.contains("<entity1")) {
					matcher = pattern.matcher(readLine);
					matcher.find();
					e1 = matcher.group();
				} else if(readLine.contains("<entity2")) {
					matcher = pattern.matcher(readLine);
					matcher.find();
					e2 = matcher.group();
					if(e1 != null && e2 != null) {
						result.add(new StringString(e1, e2));
						e1 = null;
						e2 = null;
					} else {
						System.out.println("ERROR");
					}
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	
}
