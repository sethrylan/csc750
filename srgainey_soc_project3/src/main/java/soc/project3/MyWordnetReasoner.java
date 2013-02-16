package soc.project3;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
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
			
		
		MyWordnetReasoner myReasoner = new MyWordnetReasoner();
		
		for(List<String> wordGroup : wordGroups) {
			boolean validWordGroups = true;
			if(!myReasoner.isValidSynset(wordGroup)) {
				logger.error("Invalid word-group");
				validWordGroups = false;
			}
			if(!validWordGroups) {
				System.exit(1);
			}
		}
		
		System.out.println(myReasoner.getRelation(wordGroups));
	}

	public MyWordnetReasoner() {
		// create an empty model
		model = ModelFactory.createDefaultModel();

		// use the FileManager to find the input file
		FileManager.get().readModel(model, WORDNET_CORE);
		//model.write(System.out);	// write it to standard out
	}
	
	@SuppressWarnings("serial")
	public Relation getRelation(final List<String> wordGroup1, final List<String> wordGroup2) {
		List<List<String>> wordGroups = new ArrayList<List<String>>() {{
			add(wordGroup1);
			add(wordGroup2);
		}};
		return this.getRelation(wordGroups);
	}
	
	public Relation getRelation(List<List<String>> wordGroups) {
		for(List<String> wordGroup: wordGroups) {
			if(wordGroup == null | wordGroup.size() == 0) {
				throw new IllegalArgumentException("WordGroups must have at least one word each.");
			}
		}
		
		
		
		
		return Relation.NONE;
	}

	
	/**
	 * Returns true if all words in the word-group are senseLabels of the same synset.
	 * From wordnet.princeton.edu: "Synonyms--words that denote the same concept and are interchangeable in many contexts--are grouped into unordered sets (synsets)." 
	 * @param wordGroup	list of words
	 * @return true if all words in the word-group are senseLabels of the same synset
	 */
	protected Boolean isValidSynset(List<String> wordGroup) {
		if(wordGroup.size() == 0) {
			throw new IllegalArgumentException("WordGroup must have 1 or more elements.");
		} else {			
			final String firstWord = wordGroup.get(0);

			Property senseLabelProperty = model.getProperty("http://www.w3.org/2006/03/wn/wn20/schema/senseLabel");

			/*
			 * Statements are formed [Subject, Predicate, Object]
			 * e.g., [http://www.w3.org/2006/03/wn/wn20/instances/synset-teach-verb-1, http://www.w3.org/2006/03/wn/wn20/schema/senseLabel, "teach"@en-US]
			 */
	        StmtIterator iter = model.listStatements(
	        	new  SimpleSelector(null, senseLabelProperty, (RDFNode)null) {
                    @Override
                    public boolean selects(Statement s) {
                            return s.getString().equals(firstWord);
                    }
	            }
        	);
	        
            while (iter.hasNext()) {
            	Statement statement = iter.nextStatement();
            	logger.debug("  " + statement.getObject().asLiteral().getString() + "  found in  " + statement.getSubject());
            	StmtIterator subPropIter = statement.getSubject().listProperties(senseLabelProperty);
            	List<String> synsetWords = new ArrayList<String>();
            	while (subPropIter.hasNext()) {
            		Statement propertyStatement = subPropIter.nextStatement();
                	logger.debug("  		" + propertyStatement.getObject().asLiteral().getString() + "  found in  " + propertyStatement.getSubject());
            		synsetWords.add(propertyStatement.getObject().asLiteral().getString());
				}
            	if(synsetWords.containsAll(wordGroup)) {
            		return Boolean.TRUE;
            	}
            }
			return Boolean.FALSE;			
		}
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
	
	protected Model getModel() {
		return this.model;
	}
		
	
}
