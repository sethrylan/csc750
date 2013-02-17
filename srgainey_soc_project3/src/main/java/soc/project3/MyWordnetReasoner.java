package soc.project3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class MyWordnetReasoner {
	
    private static Logger logger = LoggerFactory.getLogger(MyWordnetReasoner.class);
        
    public enum ModelType {CORE, CAUSES, ENTAILMENT, HYPONYM, MERONYM_MEMBER, MERONYM_SUBSTANCE, MERONYM_PART};
    
    private Map<ModelType, Relation> modelRelationMap = null;
    private Map<ModelType, Property> modelRelationPropertyMap = null;
//    private static final Map<String, Relation> resourceStringToRelationMap = new HashMap<String, Relation>() {{
//    	put("causes", Relation.CAUSE);
//    	put("entails", Relation.ENTAILMENT);
//    	put("hyponymOf", Relation.HYPONYMY);
//    	put("memberMeronymOf", Relation.MERONYMY);
//    	put("partMeronymOf", Relation.MERONYMY);
//    	put("substanceMeronymOf", Relation.MERONYMY);
//    }};
    
	private static final String WORDNET_CORE = "wordnet-senselabels.rdf";
	private static final String WORDNET_CAUSES = "wordnet-causes.rdf";
	private static final String WORDNET_ENTAILMENT = "wordnet-entailment.rdf";
	private static final String WORDNET_HYPONYM = "wordnet-hyponym.rdf";
	private static final String WORDNET_MERONYM_MEMBER = "wordnet-membermeronym.rdf";
	private static final String WORDNET_MERONYM_SUBSTANCE = "wordnet-substancemeronym.rdf";
	private static final String WORDNET_MERONYM_PART = "wordnet-partmeronym.rdf";
	
	private static final String WN20SCHEMA = "http://www.w3.org/2006/03/wn/wn20/schema/";
	
	private Model coreModel = null;
	private Model causesModel = null;
	private Model entailmentModel = null;
	private Model hyponymModel = null;
	private Model meronymMemberModel = null;
	private Model meronymSubstanceModel = null;
	private Model meronymPartModel = null;
	
//	private Model unifiedModel = null;
	
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
	public static void main(String args[]) {
		if (args.length != 2) {
			System.out.println("Usage: " + MyWordnetReasoner.class.getName() + " \"word-group-1\" \"word-group-2\"");
			System.out.println("\tE.g., " + MyWordnetReasoner.class.getName() + " \"teach, instruct\" \"learn, acquire\"");
			System.exit(1);
		}
		
		List<String> wordGroup1 = Arrays.asList(args[0].split("\\s*,\\s*"));
		List<String> wordGroup2 = Arrays.asList(args[1].split("\\s*,\\s*"));
		
		MyWordnetReasoner myReasoner = new MyWordnetReasoner();
		List<Resource> synsets1, synsets2 = null;
		synsets1 = myReasoner.getSynsets(wordGroup1);
		synsets2 = myReasoner.getSynsets(wordGroup2);
		
		if(synsets1.size() == 0  || synsets2.size() == 0) {
			System.err.println("Invalid word-group");
			System.exit(1);
		}
		
		List<Relation> relations = myReasoner.getRelations(synsets1, synsets2);
		System.out.println(asCommaList(relations));
	}

	@SuppressWarnings("serial")
	public MyWordnetReasoner() {
		// create an empty model and read from file
		coreModel = FileManager.get().loadModel(WORDNET_CORE);
		entailmentModel = FileManager.get().loadModel(WORDNET_ENTAILMENT);
		causesModel = FileManager.get().loadModel(WORDNET_CAUSES);
		hyponymModel = FileManager.get().loadModel(WORDNET_HYPONYM);
		meronymMemberModel = FileManager.get().loadModel( WORDNET_MERONYM_MEMBER);	
		meronymSubstanceModel = FileManager.get().loadModel(WORDNET_MERONYM_SUBSTANCE);	
		meronymPartModel = FileManager.get().loadModel(WORDNET_MERONYM_PART);	

		this.modelRelationMap = new HashMap<ModelType, Relation>() {{
	    	put(ModelType.CAUSES, Relation.CAUSE);
	    	put(ModelType.ENTAILMENT, Relation.ENTAILMENT);
	    	put(ModelType.HYPONYM, Relation.HYPONYMY);
	    	put(ModelType.MERONYM_MEMBER, Relation.MERONYMY);
	    	put(ModelType.MERONYM_SUBSTANCE, Relation.MERONYMY);
	    	put(ModelType.MERONYM_PART, Relation.MERONYMY);
	    }};
		
		this.modelRelationPropertyMap = new HashMap<ModelType, Property>() {{
	    	put(ModelType.CAUSES, causesModel.getProperty(WN20SCHEMA + "causes"));
	    	put(ModelType.ENTAILMENT, entailmentModel.getProperty(WN20SCHEMA + "entails"));
	    	put(ModelType.HYPONYM, hyponymModel.getProperty(WN20SCHEMA + "hyponymOf"));
	    	put(ModelType.MERONYM_MEMBER, meronymMemberModel.getProperty(WN20SCHEMA + "memberMeronymOf"));
	    	put(ModelType.MERONYM_SUBSTANCE, meronymSubstanceModel.getProperty(WN20SCHEMA + "substanceMeronymOf"));
	    	put(ModelType.MERONYM_PART, meronymPartModel.getProperty(WN20SCHEMA + "partMeronymOf"));
	    }};
	    
	    
//		this.unifiedModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF );
//		this.unifiedModel.add(coreModel, Boolean.TRUE); // include reified statements
//		this.unifiedModel.add(entailmentModel, Boolean.TRUE); // include reified statements
//		this.unifiedModel.add(causesModel, Boolean.TRUE); // include reified statements
//		this.unifiedModel.add(hyponymModel, Boolean.TRUE); // include reified statements
//		this.unifiedModel.add(meronymMemberModel, Boolean.TRUE); // include reified statements
//		this.unifiedModel.add(meronymPartModel, Boolean.TRUE); // include reified statements
//		this.unifiedModel.add(meronymSubstanceModel, Boolean.TRUE); // include reified statements

	    
	}
	
	/**
	 * Returns a List of the synsets containing all words in the word group.
	 * From wordnet.princeton.edu: "Synonyms--words that denote the same concept and are interchangeable in many contexts--are grouped into unordered sets (synsets)." 
	 * @param wordGroup	list of words
	 * @return list of synsets containing all words in the word-group; list of size 0 if no such synset exists
	 */
	protected List<Resource> getSynsets(final List<String> wordGroup) {
		if(wordGroup == null || wordGroup.size() == 0) {
			throw new IllegalArgumentException("WordGroup must have 1 or more elements.");
		} else {			
			List<Resource> synsets = new ArrayList<Resource>();
			StringBuilder querySb = new StringBuilder();
			querySb.append("PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> ");
			querySb.append("	SELECT  ?synset ");
			querySb.append("	WHERE   { ");
			for(String word : wordGroup) {
				querySb.append("		?synset wn20schema:senseLabel \"" + word +"\"@en-US . ");
			}
			querySb.append("	}");
			
			Query query = QueryFactory.create(querySb.toString());
			QueryExecution qe = QueryExecutionFactory.create(query, coreModel);
			ResultSet results = qe.execSelect();
			while(results.hasNext()) {
				synsets.add(results.next().getResource("synset"));
			}
			qe.close();	
			return synsets;			
		}
	}

	
	
	public List<Relation> getRelations(final List<Resource> synsetList1, final List<Resource> synsetList2) {
		if(synsetList1 == null || synsetList2 == null ) {
			throw new IllegalArgumentException("Synsets must not be null.");
		}
		List<Relation> relations = new ArrayList<Relation>();
		
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
//    	System.out.println("s1=" + asCommaList(synsetList1));
//    	System.out.println("s2=" + asCommaList(synsetList2));
    	
		for(ModelType modelType : modelRelationMap.keySet()) {
			Model model = this.getModel(modelType);
			for(Resource synset1 : synsetList1) {
		    	StmtIterator statements = model.listStatements(synset1, modelRelationPropertyMap.get(modelType), (RDFNode)null);
				while(statements.hasNext()) {
					Statement statement = statements.nextStatement();
					logger.debug(statement.toString());
					if(statement.getObject().isResource() && synsetList2.contains(statement.getObject().asResource())) {
						relations.add(this.modelRelationMap.get(modelType));
					} 
				}
			}
			
			for(Resource synset2 : synsetList2) {
		    	StmtIterator statements = model.listStatements(synset2, modelRelationPropertyMap.get(modelType), (RDFNode)null);
				while(statements.hasNext()) {
					Statement statement = statements.nextStatement();
					logger.debug(statement.toString());
					if(statement.getObject().isResource() && synsetList1.contains(statement.getObject().asResource())) {
						relations.add(this.modelRelationMap.get(modelType).getInverse());
					} 
				}
			}
		}	
		if(relations.size() > 0) {
			return relations;
		} else {
			return Collections.singletonList(Relation.NONE);
		}
	}
		
	
	protected static String asCommaList(final List list) {
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
			case MERONYM_PART:
				return this.meronymPartModel;
			default:
				return null;		
		}
	}
}
