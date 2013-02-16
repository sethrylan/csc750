package soc.project3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class MyWordnetReasoner {
	
    private static Logger logger = LoggerFactory.getLogger(MyWordnetReasoner.class);
    
    public enum ModelType {CORE, CAUSES, ENTAILMENT, HYPONYM, MERONYM};
    
	private static final String WORDNET_CORE = "wordnet-senselabels.rdf";
	private static final String WORDNET_CAUSES = "wordnet-causes.rdf";
	private static final String WORDNET_ENTAILMENT = "wordnet-entailment.rdf";
	private static final String WORDNET_HYPONYM = "wordnet-hyponym.rdf";
	private static final String WORDNET_MERONYM = "wordnet-membermeronym.rdf";
	
	private Model coreModel = null;
	private Model causesModel = null;
	private Model entailmentModel = null;
	private Model hyponymModel = null;
	private Model meronymModel = null;

	
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
		
		MyWordnetReasoner myReasoner = new MyWordnetReasoner();
		Resource synset1, synset2 = null;
		synset1 = myReasoner.getSynset(wordGroup1);
		synset2 = myReasoner.getSynset(wordGroup2);
		
		if(synset1 == null || synset2 == null) {
			logger.error("Invalid word-group");
			System.exit(1);
		}
				
		System.out.println(myReasoner.getRelation(synset1, synset2));
	}

	public MyWordnetReasoner() {
		// create an empty model and read from file
		coreModel = ModelFactory.createDefaultModel();
		FileManager.get().readModel(coreModel, WORDNET_CORE);

		entailmentModel = ModelFactory.createDefaultModel();
		FileManager.get().readModel(entailmentModel, WORDNET_ENTAILMENT);

		causesModel = ModelFactory.createDefaultModel();
		FileManager.get().readModel(causesModel, WORDNET_CAUSES);
		
		hyponymModel = ModelFactory.createDefaultModel();
		FileManager.get().readModel(hyponymModel, WORDNET_HYPONYM);

		meronymModel = ModelFactory.createDefaultModel();
		FileManager.get().readModel(meronymModel, WORDNET_MERONYM);	
	}
	
//	public Relation getRelation(List<List<String>> wordGroups) {
//		if(wordGroups == null || wordGroups.size() != 2) {
//			throw new IllegalArgumentException("There must be two wordgroups.");
//		}
//		return this.getRelation(wordGroups.get(0), wordGroups.get(1));
//	}
	
	public Relation getRelation(Resource synset1, Resource synset2) {
		if(synset1 == null || synset2 == null  ) {
			throw new IllegalArgumentException("Synsets must not be null.");
		}
				
//
//        StmtIterator iter = coreModel.listStatements(
//	        	new  SimpleSelector(null, senseLabelProperty, (RDFNode)null) {
//                    @Override
//                    public boolean selects(Statement s) {
//                            return s.getString().equals(firstWord);
//                    }
//	            }
//        	);
//	        
//            while (iter.hasNext()) {
//            	Statement statement = iter.nextStatement();
//            	logger.debug("  " + statement.getObject().asLiteral().getString() + "  found in  " + statement.getSubject());
//            	StmtIterator subPropIter = statement.getSubject().listProperties(senseLabelProperty);
//            	List<String> synsetWords = new ArrayList<String>();
//            	while (subPropIter.hasNext()) {
//            		Statement propertyStatement = subPropIter.nextStatement();
//                	logger.debug("  		" + propertyStatement.getObject().asLiteral().getString() + "  found in  " + propertyStatement.getSubject());
//            		synsetWords.add(propertyStatement.getObject().asLiteral().getString());
//				}
//            	if(synsetWords.containsAll(wordGroup)) {
//            		return Boolean.TRUE;
//            	}
//            }

		
		
		
		return Relation.NONE;
	}

	
	/**
	 * Returns synset containing all words in the word group.
	 * From wordnet.princeton.edu: "Synonyms--words that denote the same concept and are interchangeable in many contexts--are grouped into unordered sets (synsets)." 
	 * @param wordGroup	list of words
	 * @return synset containing all words in the word-group; null if no such synset exists
	 */
	protected Resource getSynset(List<String> wordGroup) {
		if(wordGroup == null || wordGroup.size() == 0) {
			throw new IllegalArgumentException("WordGroup must have 1 or more elements.");
		} else {			
			final String firstWord = wordGroup.get(0);

			Property senseLabelProperty = coreModel.getProperty("http://www.w3.org/2006/03/wn/wn20/schema/senseLabel");

			/*
			 * Statements are formed [Subject, Predicate, Object]
			 * e.g., [http://www.w3.org/2006/03/wn/wn20/instances/synset-teach-verb-1, http://www.w3.org/2006/03/wn/wn20/schema/senseLabel, "teach"@en-US]
			 */
	        StmtIterator iter = coreModel.listStatements(
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
            		return statement.getSubject();
            	}
            }
			return null;			
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
	
	protected Model getModel(ModelType modelType) {
		switch(modelType) {
			case CORE:
				return this.coreModel;
			case CAUSES:
				return this.hyponymModel;
			case ENTAILMENT:
				return this.entailmentModel;
			case HYPONYM:
				return this.hyponymModel;
			case MERONYM:
				return this.meronymModel;
			default:
				return null;		
		}
	}
	
		
	
}
