package soc.project3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class MyWordnetReasoner {
	
    private static Logger logger = LoggerFactory.getLogger(MyWordnetReasoner.class);
        
    public enum ModelType {CORE, UNIFIED};
    
//    private Map<ModelType, Relation> modelRelationMap = null;
//    private Map<ModelType, Property> modelRelationPropertyMap = null;
    private static final Map<String, Relation> resourceStringToRelationMap = new HashMap<String, Relation>() {{
    	put("causes", Relation.CAUSE);
    	put("entails", Relation.ENTAILMENT);
    	put("hyponymOf", Relation.HYPONYMY);
    	put("meronymOf", Relation.MERONYMY);
//    	put("memberMeronymOf", Relation.MERONYMY);
//    	put("partMeronymOf", Relation.MERONYMY);
//    	put("substanceMeronymOf", Relation.MERONYMY);
    }};
    
	private static final String WORDNET_CORE = "wordnet-senselabels.rdf";
	private static final String WORDNET_CAUSES = "wordnet-causes.rdf";
	private static final String WORDNET_ENTAILMENT = "wordnet-entailment.rdf";
	private static final String WORDNET_HYPONYM = "wordnet-hyponym.rdf";
	private static final String WORDNET_MERONYM_MEMBER = "wordnet-membermeronym.rdf";
	private static final String WORDNET_MERONYM_SUBSTANCE = "wordnet-substancemeronym.rdf";
	private static final String WORDNET_MERONYM_PART = "wordnet-partmeronym.rdf";
	
	public static String WNBASIC_OWL = "wnbasic.owl";
	
	protected static final String WN20SCHEMA = "http://www.w3.org/2006/03/wn/wn20/schema/";
	
	private Model coreModel = null;
	private Model unifiedModel = null;
	
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
		
		boolean validWordGroups = true;
		if(synsets1.size() == 0 ) {
			System.err.println("Invalid word-group: " + asCommaList(wordGroup1));
			validWordGroups = false;
		}
		if(synsets2.size() == 0 ) {
			System.err.println("Invalid word-group: " + asCommaList(wordGroup2));
			validWordGroups = false;
		}
		if(!validWordGroups) {
			System.exit(1);
		}

		List<Relation> relations = myReasoner.getRelations(wordGroup1, wordGroup2);
		System.out.println(asCommaList(new HashSet<Relation>(relations)));
	}

	@SuppressWarnings("serial")
	public MyWordnetReasoner() {
		coreModel = FileManager.get().loadModel(WORDNET_CORE);
		Model ontologyModel = FileManager.get().loadModel( WNBASIC_OWL );
		this.unifiedModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, ontologyModel);
		this.unifiedModel.add(coreModel, Boolean.TRUE); // include reified statements
		this.unifiedModel.add(FileManager.get().loadModel(WORDNET_ENTAILMENT), Boolean.TRUE); // include reified statements
		this.unifiedModel.add(FileManager.get().loadModel(WORDNET_CAUSES), Boolean.TRUE); // include reified statements
		this.unifiedModel.add(FileManager.get().loadModel(WORDNET_HYPONYM), Boolean.TRUE); // include reified statements
		this.unifiedModel.add(FileManager.get().loadModel( WORDNET_MERONYM_MEMBER), Boolean.TRUE); // include reified statements
		this.unifiedModel.add(FileManager.get().loadModel(WORDNET_MERONYM_SUBSTANCE), Boolean.TRUE); // include reified statements
		this.unifiedModel.add(FileManager.get().loadModel(WORDNET_MERONYM_PART), Boolean.TRUE); // include reified statements
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
	
	
	/* see http://www.w3.org/TR/sparql11-query/#propertypath-arbitrary-length
	 * see http://jena.sourceforge.net/ARQ/property_paths.html
SELECT ?relation
WHERE
  { ?synset1 wn20schema:senseLabel "learn"@en-US .
    ?synset1 wn20schema:senseLabel "acquire"@en-US .
    ?synset2 wn20schema:senseLabel "teach"@en-US .
    ?synset2 wn20schema:senseLabel "instruct"@en-US .
    ?synset1 ?relation ?synset2
  }

	 * 
	 * 
	 */
	public List<Relation> getRelations(final List<String> wordGroup1, final List<String> wordGroup2) {
		if(wordGroup1 == null || wordGroup2.size() == 0 || wordGroup2 == null || wordGroup2.size() == 0) {
			throw new IllegalArgumentException("WordGroups must have 1 or more elements.");
		}
		List<Relation> relations = new ArrayList<Relation>();
		
		StringBuilder queryBaseSb = new StringBuilder();
		queryBaseSb.append("PREFIX wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> ");
		queryBaseSb.append("SELECT ?relation ");
		queryBaseSb.append("WHERE { ");
		for(String word : wordGroup1) {
			queryBaseSb.append("	?synset1 wn20schema:senseLabel \"" + word +"\"@en-US . ");
		}
		for(String word : wordGroup2) {
			queryBaseSb.append("	?synset2 wn20schema:senseLabel \"" + word +"\"@en-US . ");
		}
		StringBuilder relationQuerySb = new StringBuilder(queryBaseSb);
		relationQuerySb.append("	?synset1 ?relation ?synset2");
		relationQuerySb.append("}");
		StringBuilder inverseRelationQuerySb = new StringBuilder(queryBaseSb);
		inverseRelationQuerySb.append("	?synset2 ?relation ?synset1");
		inverseRelationQuerySb.append("}");	
		
		Query query = QueryFactory.create(relationQuerySb.toString());
		//System.out.println(query);
		QueryExecution qe = QueryExecutionFactory.create(query, unifiedModel);
		ResultSet results = qe.execSelect();
		while(results.hasNext()) {
			Resource next = results.next().getResource("relation");
			if(next != null) {
				System.out.println(next);
				Relation relation = resourceStringToRelationMap.get(next.getLocalName());
				//System.out.println(relation);
				relations.add(relation);
			}
		}
		
		query = QueryFactory.create(inverseRelationQuerySb.toString());
		//System.out.println(query);
		qe = QueryExecutionFactory.create(query, unifiedModel);
		results = qe.execSelect();
		while(results.hasNext()) {
			Resource next = results.next().getResource("relation");
			if(next != null) {
				System.out.println(next);
				Relation relation = resourceStringToRelationMap.get(next.getLocalName()).getInverse();
				//System.out.println(relation);
				relations.add(relation);
			}
		}
		qe.close();	
		
		if(relations.size() > 0) {
			return relations;
		} else {
			return Collections.singletonList(Relation.NONE);
		}
		
	/*
		queryBaseSb.append("PREFIX  wn20instances:<http://www.w3.org/2006/03/wn/wn20/instances/> ");
		queryBaseSb.append("SELECT ?relation ");
		queryBaseSb.append("WHERE { ");
		StringBuilder relationQuerySb = new StringBuilder(queryBaseSb);
		StringBuilder inverseRelationQuerySb = new StringBuilder(queryBaseSb);
		relationQuerySb.append("	?synset1 ?relation ?synset2");
		inverseRelationQuerySb.append("	?synset2 ?relation ?synset1");
		StringBuilder filterSb = new StringBuilder();
		filterSb.append("	FILTER(?synset1 = ");
		String delimiter = "";
		for(Resource synset : synsets1) {
			filterSb.append(delimiter);
			filterSb.append("wn20instances:" + synset.getLocalName());			
			delimiter = " || ";
		}
		filterSb.append("  && ?synset2 = ");
		delimiter = "";
		for(Resource synset : synsets2) {
			filterSb.append(delimiter);
			filterSb.append("wn20instances:" + synset.getLocalName());			
			delimiter = " || ";
		}

		relationQuerySb.append(filterSb).append(") }");
		inverseRelationQuerySb.append(filterSb).append(") }");
*/
	}
		
	protected static String asCommaList(final Collection collection) {
		StringBuilder sb = new StringBuilder();
		String delimiter = "";
		for(Object o : collection) {
			if(o == null) {
				sb.append(delimiter)
				.append("NULL");
			} else {
				sb.append(delimiter)
				.append(o.toString());
				delimiter = ",";
			}
		}
		return sb.toString();
	}

	protected Model getModel(ModelType modelType) {
		switch(modelType) {
			case CORE:
				return this.coreModel;
			case UNIFIED:
				return this.unifiedModel;
			default:
				return null;		
		}
	}
	
	
	public long getTriplesCount() {
		// see http://code.google.com/p/void-impl/wiki/SPARQLQueriesForStatistics
		String queryString = "SELECT (COUNT(*) AS ?numberTriples) { ?s ?p ?o  }";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, unifiedModel);
		ResultSet results = qe.execSelect(); // ( ?numberTriples = 782810 )
		long n = results.next().getLiteral("numberTriples").getLong();
		qe.close();	
		return n;
	}
	
	public long getEntitesCount() {
		String queryString = "SELECT COUNT(distinct ?s) AS ?numberEntities { ?s a []  }";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, unifiedModel);
		ResultSet results = qe.execSelect();
		long n = results.next().getLiteral("numberEntities").getLong();
		qe.close();	
		return n;
	}
	
}
