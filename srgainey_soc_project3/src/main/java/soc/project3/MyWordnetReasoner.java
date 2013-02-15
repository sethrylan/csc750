package soc.project3;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class MyWordnetReasoner {
	
    private static Logger logger = LoggerFactory.getLogger(MyWordnetReasoner.class);
    
	private static final String WORDNET_CORE = "wordnet-senselabels.rdf";
	
	private Model model = null;
	
	/**
	 * Given two comma-delimted lists of words, if both lists represent
	 * synsets (set of synonyms in WordNet), then the program displays 
	 * all of potential relationships from the first synset to the 
	 * second synset
	 * 
	 * Example:
	 * <i>Entailment</i>: java MyWordnetReasoner "call, ring" "dial" -> "Entails"
	 * <i>Meronymy</i>: java MyWordnetReasoner "warp" "fabric, textile" -> "Meronym"
	 * <i>Hyponymy</i>: java MyWordnetReasoner "relation" "abstraction" -> "Hyponym"
	 * <i>Cause</i>: java MyWordnetReasoner "teach, instruct", "learn, acquire" -> "Causes"
	 * If no relationship exists between the two synsets, then the program prints "Relationship unknown".
	 *
	 * @param args commandline arguments of two comma-delimited strings representing word groups
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: " + MyWordnetReasoner.class.getName() + " \"word-group-1\" \"word-group-2\"");
			System.out.println("\tE.g., " + MyWordnetReasoner.class.getName() + " \"teach, instruct\" \"learn, acquire\"");
			System.exit(1);
		}
		
		List<String> wordGroup1 = Arrays.asList(args[0].split("\\s*,\\s*"));
		List<String> wordGroup2 = Arrays.asList(args[1].split("\\s*,\\s*"));

		List<List<String>> wordGroups = new ArrayList<List<String>>();
		wordGroups.add(wordGroup1);
		wordGroups.add(wordGroup2);
			
		for(List<String> wordGroup : wordGroups) {
			boolean validWordGroups = true;
			if(!isValidSynset(wordGroup)) {
				logger.error("Invalid word-group: " + asCommaList(wordGroup));
				validWordGroups = false;
			}
			if(!validWordGroups) {
				System.exit(1);
			}
		}
		
		MyWordnetReasoner myReason = new MyWordnetReasoner();
		System.out.println(myReason.getRelation(wordGroups.get(0), wordGroups.get(1)));
	}

	public MyWordnetReasoner() {
		// create an empty model
		model = ModelFactory.createDefaultModel();

		// use the FileManager to find the input file
		InputStream in = FileManager.get().open( WORDNET_CORE );
		if(in == null) {
			throw new IllegalArgumentException("File: " + WORDNET_CORE + " not found");
		}
		model.read(in, null);	// read the RDF/XML file
		//model.write(System.out);	// write it to standard out
		try {
			in.close();
		} catch (IOException e) {
			logger.error("Could not close file", e);
		}
	}
	
	public Relation getRelation(List<String> wordGroup1, List<String> wordGroup2) {
		// TODO
		return Relation.NONE;
	}

	
	/**
	 * Returns true if all words in the word-group are senseLabels of the same synset.
	 * From wordnet.princeton.edu: "Synonyms--words that denote the same concept and are interchangeable in many contexts--are grouped into unordered sets (synsets)." 
	 * @param wordGroup	list of words
	 * @return true if all words in the word-group are senseLabels of the same synset
	 */
	protected static Boolean isValidSynset(List<String> wordGroup) {
		// TODO:
		return Boolean.FALSE;
	}
	
	
	protected static String asCommaList(List<String> stringList) {
		StringBuilder sb = new StringBuilder();
		String delimiter = "";
		for(String string : stringList) {
			sb.append(delimiter)
			.append(string);
			delimiter = ",";
		}
		return sb.toString();
	}
	
}
