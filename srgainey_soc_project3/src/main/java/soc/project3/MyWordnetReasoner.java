package soc.project3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    public enum ModelType {CORE, CAUSES, ENTAILMENT, HYPONYM, MERONYM_MEMBER, MERONYM_SUBSTANCE};
    
    private Map<ModelType, Relation> modelRelationMap = null;
    private Map<ModelType, Property> modelRelationPropertyMap = null;
    
	private static final String WORDNET_CORE = "wordnet-senselabels.rdf";
	private static final String WORDNET_CAUSES = "wordnet-causes.rdf";
	private static final String WORDNET_ENTAILMENT = "wordnet-entailment.rdf";
	private static final String WORDNET_HYPONYM = "wordnet-hyponym.rdf";
	private static final String WORDNET_MERONYM_MEMBER = "wordnet-membermeronym.rdf";
	private static final String WORDNET_MERONYM_SUBSTANCE = "wordnet-substancemeronym.rdf";

	private static final String WN20SCHEMA = "http://www.w3.org/2006/03/wn/wn20/schema/";
	
	private Model coreModel = null;
	private Model causesModel = null;
	private Model entailmentModel = null;
	private Model hyponymModel = null;
	private Model meronymMemberModel = null;
	private Model meronymSubstanceModel = null;

	
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
		List<Resource> synsets1, synsets2 = null;
		synsets1 = myReasoner.getSynset(wordGroup1);
		synsets2 = myReasoner.getSynset(wordGroup2);
		
		if(synsets1.size() == 0  || synsets2.size() == 0) {
			logger.error("Invalid word-group");
			System.exit(1);
		}
				
		System.out.println(myReasoner.getRelation(synsets1, synsets2));
	}

	@SuppressWarnings("serial")
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

		meronymMemberModel = ModelFactory.createDefaultModel();
		FileManager.get().readModel(meronymMemberModel, WORDNET_MERONYM_MEMBER);	
		
		meronymSubstanceModel = ModelFactory.createDefaultModel();
		FileManager.get().readModel(meronymSubstanceModel, WORDNET_MERONYM_SUBSTANCE);	

		this.modelRelationMap = new HashMap<ModelType, Relation>() {{
	    	put(ModelType.CAUSES, Relation.CAUSE);
	    	put(ModelType.ENTAILMENT, Relation.ENTAILMENT);
	    	put(ModelType.HYPONYM, Relation.HYPONYMY);
	    	put(ModelType.MERONYM_MEMBER, Relation.MERONYMY);
	    	put(ModelType.MERONYM_SUBSTANCE, Relation.MERONYMY);
	    }};

		
		this.modelRelationPropertyMap = new HashMap<ModelType, Property>() {{
	    	put(ModelType.CAUSES, causesModel.getProperty(WN20SCHEMA + "causes"));
	    	put(ModelType.ENTAILMENT, entailmentModel.getProperty(WN20SCHEMA + "entails"));
	    	put(ModelType.HYPONYM, hyponymModel.getProperty(WN20SCHEMA + "hyponymOf"));
	    	put(ModelType.MERONYM_MEMBER, meronymMemberModel.getProperty(WN20SCHEMA + "memberMeronymOf"));
	    	put(ModelType.MERONYM_SUBSTANCE, meronymSubstanceModel.getProperty(WN20SCHEMA + "substanceMeronymOf"));

	    }};
	}
		
	public Relation getRelation(final List<Resource> synsetList1, final List<Resource> synsetList2) {
		if(synsetList1 == null || synsetList2 == null ) {
			throw new IllegalArgumentException("Synsets must not be null.");
		}
		
		
//		List<String> words1 = new ArrayList<String>() {{
////			add("teach");
////			add("instruct");
//			add("develop");
//			add("make grow");
//		}};
//		Resource s1 = this.getSynset(words1);
//		
//		List<String> words2 = new ArrayList<String>() {{
//			add("learn");
//			add("acquire");
//		}};
//		Resource s2 = this.getSynset(words2);
//    	System.out.println("s1=" + s1);
//    	System.out.println("s2=" + s2);
//    	System.out.println(causesModel.listObjectsOfProperty(causesModel.getProperty(WN20SCHEMA + "causes")).toList().size());    	
//    	System.out.println("s1bag=" + causesModel.getBag(s1));
//    	System.out.println("s2bag=" + causesModel.getBag(s2));
//    	System.out.println("s1alt=" + causesModel.getAlt(s1));
//    	System.out.println("s2alt=" + causesModel.getAlt(s2));
//    	System.out.println("s1causesprop=" + causesModel.getProperty(s1, causesModel.getProperty(WN20SCHEMA + "causes")));
//    	
//    	StmtIterator statements = causesModel.listStatements(s1, causesModel.getProperty(WN20SCHEMA + "causes"), (RDFNode)null);
//    	while(statements.hasNext()) {
//        	System.out.println("    " + statements.nextStatement());
//    	}
//    	
//    	System.out.println("s2causesprop=" + causesModel.getProperty(s2, causesModel.getProperty(WN20SCHEMA + "causes")));
    	System.out.println("s1=" + asCommaList(synsetList1));
    	System.out.println("s2=" + asCommaList(synsetList2));
    	
		for(ModelType modelType : modelRelationMap.keySet()) {
			Model model = this.getModel(modelType);
			for(Resource synset1 : synsetList1) {
		    	StmtIterator statements = model.listStatements(synset1, modelRelationPropertyMap.get(modelType), (RDFNode)null);
				while(statements.hasNext()) {
					Statement statement = statements.nextStatement();
					System.out.println(statement);
					if(statement.getObject().isResource() && synsetList2.contains(statement.getObject().asResource())) {
							return this.modelRelationMap.get(modelType);
					} 
				}
			}
		}	
		return Relation.NONE;
	}
	

	/**
	 * Returns a List of the synsets containing all words in the word group.
	 * From wordnet.princeton.edu: "Synonyms--words that denote the same concept and are interchangeable in many contexts--are grouped into unordered sets (synsets)." 
	 * @param wordGroup	list of words
	 * @return list of synsets containing all words in the word-group; list of size 0 if no such synset exists
	 */
	protected List<Resource> getSynset(List<String> wordGroup) {
		if(wordGroup == null || wordGroup.size() == 0) {
			throw new IllegalArgumentException("WordGroup must have 1 or more elements.");
		} else {			
			List<Resource> synsets = new ArrayList<Resource>();
			final String firstWord = wordGroup.get(0);
			Property senseLabelProperty = coreModel.getProperty(WN20SCHEMA + "senseLabel");
			/*
			 * Statements are formed [Subject, Predicate, Object]
			 * e.g., [http://www.w3.org/2006/03/wn/wn20/instances/synset-teach-verb-1, http://www.w3.org/2006/03/wn/wn20/schema/senseLabel, "teach"@en-US]
			 */
	        StmtIterator statements = coreModel.listStatements(
	        	new  SimpleSelector(null, senseLabelProperty, (RDFNode)null) {
                    @Override
                    public boolean selects(Statement s) {
                            return s.getString().equals(firstWord);
                    }
	            }
        	);
	        
            while (statements.hasNext()) {
            	Statement statement = statements.nextStatement();
            	logger.debug("  " + statement.getObject().asLiteral().getString() + "  found in  " + statement.getSubject());
            	StmtIterator subjectProperties = statement.getSubject().listProperties(senseLabelProperty);
            	List<String> synsetWords = new ArrayList<String>();
            	while (subjectProperties.hasNext()) {
            		Statement propertyStatement = subjectProperties.nextStatement();
                	logger.debug("  		" + propertyStatement.getObject().asLiteral().getString() + "  found in  " + propertyStatement.getSubject());
            		synsetWords.add(propertyStatement.getObject().asLiteral().getString());
				}
            	if(synsetWords.containsAll(wordGroup)) {
            		synsets.add(statement.getSubject());
            	}
            }
			return synsets;			
		}
	}
	
	protected static String asCommaList(List list) {
		StringBuilder sb = new StringBuilder();
		String delimiter = "";
		for(Object o : list) {
			sb.append(delimiter)
			.append(o.toString());
			delimiter = ",";
		}
		return sb.toString();
	}


	protected Model getModel(ModelType modelType) {
		switch(modelType) {
			case CORE:
				return this.coreModel;
			case CAUSES:
				return this.causesModel;
			case ENTAILMENT:
				return this.entailmentModel;
			case HYPONYM:
				return this.hyponymModel;
			case MERONYM_MEMBER:
				return this.meronymMemberModel;
			case MERONYM_SUBSTANCE:
				return this.meronymSubstanceModel;
			default:
				return null;		
		}
	}
}
