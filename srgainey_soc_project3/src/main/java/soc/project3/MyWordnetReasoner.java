package soc.project3;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

/**
 * A class detecting synsets (en.wikipedia.org/wiki/Synonym_ring) inferring relationships in the WordNet dataset using the 
 * properties senseLabel, hyponymOf, meronymOf, causes, entails and their reflections (i.e., 'caused 
 * by' to 'causes', 'holonym of' to 'hyponymOf').
 * 
 * @author srgainey
 */
public class MyWordnetReasoner {
	
    private static Logger logger = LoggerFactory.getLogger(MyWordnetReasoner.class);
        
    public enum ModelType {CORE, UNIFIED};
    
    @SuppressWarnings("serial")
	protected static final Map<String, Relation> resourceStringToRelationMap = new HashMap<String, Relation>() {{
    	put("causes", Relation.CAUSE);
    	put("entails", Relation.ENTAILMENT);
    	put("hyponymOf", Relation.HYPONYMY);
    	put("meronymOf", Relation.MERONYMY);  // from wnbasic ontology, includes  memberMeronymOf, partMeronymOf and substanceMeronymOf
    }};
    
	private static final String WORDNET_CORE = "wordnet-senselabels.rdf";
	private static final String WORDNET_CAUSES = "wordnet-causes.rdf";
	private static final String WORDNET_ENTAILMENT = "wordnet-entailment.rdf";
	private static final String WORDNET_HYPONYM = "wordnet-hyponym.rdf";
	private static final String WORDNET_MERONYM_MEMBER = "wordnet-membermeronym.rdf";
	private static final String WORDNET_MERONYM_SUBSTANCE = "wordnet-substancemeronym.rdf";
	private static final String WORDNET_MERONYM_PART = "wordnet-partmeronym.rdf";
	
	public static String WNBASIC_OWL = "wnbasic.owl";
	
	protected static final String WN20SCHEMA_URI = "http://www.w3.org/2006/03/wn/wn20/schema/";
	protected static final String WN20SCHEMA = "wn20schema";
	protected static final String WN20INSTANCES_URI = "http://www.w3.org/2006/03/wn/wn20/instances/";
	protected static final String WN20INSTANCES = "wn20instances";

	private Model coreModel = null;
	private Model unifiedModel = null;
	
	/**
	 * Given two comma-delimted lists of words, if both lists represent synsets, then the program displays 
	 * all of potential relationships from the first synset to the 
	 * second synset
	 * 
	 * Example:
	 * <i>Entailment</i>: java MyWordnetReasoner "call, ring" "dial" -> "Entails"
	 * <i>Meronymy</i>: java MyWordnetReasoner "warp" "fabric, textile" -> "Meronym"
	 * <i>Hyponymy</i>: java MyWordnetReasoner "social relation" "abstraction" -> "Hyponym"
	 * <i>Cause</i>: java MyWordnetReasoner "teach, instruct" "learn, acquire" -> "Causes"
	 * If no relationship exists between the two synsets, then the program prints "Relationship unknown".
	 *
	 * @param args commandline arguments of two quoted, comma-delimited strings representing word groups
	 */
	public static void main(String args[]) {
		if (args.length != 2) {
			System.out.println("Usage: " + MyWordnetReasoner.class.getName() + " \"word-group-1\" \"word-group-2\"");
			System.out.println("\tE.g., " + MyWordnetReasoner.class.getName() + " \"teach, instruct\" \"learn, acquire\"");
			System.exit(1);
		}
		
		List<String> wordGroup1 = Arrays.asList(args[0].toLowerCase().split("\\s*,\\s*"));
		List<String> wordGroup2 = Arrays.asList(args[1].toLowerCase().split("\\s*,\\s*"));
		
		System.out.print("Loading WordNet files... ");
		MyWordnetReasoner myReasoner = new MyWordnetReasoner();
		System.out.println("\t\t\tdone");

		System.out.print("Validating synsets... ");
		Set<Resource> synsets1, synsets2 = null;
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
		System.out.println("\t\t\t\tdone");

		System.out.print("Calculating single-step relations... ");
		Set<Relation> relations = myReasoner.getRelations(synsets1, synsets2, false);
		System.out.println("\t\tdone");
		System.out.println("\t" + asCommaList(relations));
		
		System.out.print("Calculating positive-closure relations... ");
		Set<Relation> positiveClosureelations = myReasoner.getRelations(synsets1, synsets2, true);
		System.out.println("\tdone.");
		System.out.println("\t" + asCommaList(positiveClosureelations));

	}

	/**
	 * Creates an instance of the MyWordnetReasoner. This may take some time, depending on the size of the RDF files.
	 */
	public MyWordnetReasoner() {
		coreModel = FileManager.get().loadModel(WORDNET_CORE);
		Model ontologyModel = FileManager.get().loadModel( WNBASIC_OWL );
		this.unifiedModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, ontologyModel);
		this.unifiedModel.add(coreModel, Boolean.TRUE); // includes reified statements
		this.unifiedModel.add(FileManager.get().loadModel(WORDNET_ENTAILMENT), Boolean.TRUE);
		this.unifiedModel.add(FileManager.get().loadModel(WORDNET_CAUSES), Boolean.TRUE); 
		this.unifiedModel.add(FileManager.get().loadModel(WORDNET_HYPONYM), Boolean.TRUE);
		this.unifiedModel.add(FileManager.get().loadModel( WORDNET_MERONYM_MEMBER), Boolean.TRUE);
		this.unifiedModel.add(FileManager.get().loadModel(WORDNET_MERONYM_SUBSTANCE), Boolean.TRUE); 
		this.unifiedModel.add(FileManager.get().loadModel(WORDNET_MERONYM_PART), Boolean.TRUE); 
	}
	
	/**
	 * Returns the set of relations from the synsets identified by all of the words in the first group to the synsets identified by all the word in the second group.
	 * @param wordGroup1	the word group representing the synset from which to return relationships
	 * @param wordGroup2	the word group to which to return relationships
	 * @param positiveClosure	is true, the positive closure relationships (one or more steps) are returned; if false, only single-step relationships are returned
	 * @see MyWordnetReasoner#getRelations(Set, Set, boolean)
	 * @return	the set of relations from wordGroup1 to wordGroup2
	 */
	public Set<Relation> getRelations(final List<String> wordGroup1, final List<String> wordGroup2, boolean positiveClosure) {
		return this.getRelations(this.getSynsets(wordGroup1), this.getSynsets(wordGroup2), positiveClosure);
	}
	
	/**
	 * Returns the set of relations from the first synset to the second.
	 * @see <a href="http://www.w3.org/TR/sparql11-query/#propertypath-arbitrary-length">W3 SPARQL documentation on positive closure</a>
	 * @see <a href="http://jena.sourceforge.net/ARQ/property_paths.html">Jena documentation on positive closure</a>
	 * @param synsetSet1	the synset group representing the synset from which to return relationships
	 * @param synsetSet2	the synset group to which to return relationships
	 * @param positiveClosure	is true, the positive closure relationships (one or more steps) are returned; if false, only single-step relationships are returned
	 * @return
	 */
	public Set<Relation> getRelations(final Set<Resource> synsetSet1, final Set<Resource> synsetSet2, boolean positiveClosure) {
		/*
		 * Creates a SPARQL query in the form:
		 * 
		 * PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/>
		 * PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>
		 * ASK  { 
		 * 		?synset1 wn20schema:entails+ ?synset2 . 
		 * 		FILTER(?synset1 IN (wn20instances:synset-call-verb-2) && 
		 * 		?synset2 IN (wn20instances:synset-dial-verb-1, wn20instances:synset-dial-verb-2, wn20instances:synset-dial-noun-2, wn20instances:synset-dial-noun-3, wn20instances:synset-dial-noun-4, wn20instances:synset-dial-noun-1))
		 * }
		 */
		if(synsetSet1 == null || synsetSet1.size() == 0 || synsetSet2 == null || synsetSet2.size() == 0) {
			return Collections.singleton(Relation.NONE);
		}
		Set<Relation> relations = new HashSet<Relation>();
		
		StringBuilder queryBeginSb = new StringBuilder();
		queryBeginSb.append("PREFIX " + WN20SCHEMA + ": <" + WN20SCHEMA_URI + "> ");
		queryBeginSb.append("PREFIX " + WN20INSTANCES + ": <" + WN20INSTANCES_URI + "> ");
		queryBeginSb.append("ASK { ");
		queryBeginSb.append("?synset1 ");
				
		StringBuilder queryEndSb = new StringBuilder();
		queryEndSb.append(" ?synset2 . ");
		queryEndSb.append("FILTER(?synset1 IN (");
		String delimiter = "";
		for(Resource synset : synsetSet1) {
			queryEndSb.append(delimiter + " wn20instances:" + synset.getLocalName());
			delimiter = ",";
		}
		queryEndSb.append(") && ?synset2 IN (");
		delimiter = "";
		for(Resource synset : synsetSet2) {
			queryEndSb.append(delimiter + " wn20instances:" + synset.getLocalName());
			delimiter = ",";
		}
		queryEndSb.append("))}");
		
		for(Entry<String, Relation> entry : resourceStringToRelationMap.entrySet()) {
			String relationResourceString = entry.getKey();
			Relation relation = entry.getValue();
			StringBuilder querySb = new StringBuilder(queryBeginSb);
			StringBuilder reflectedQuerySb = new StringBuilder(queryBeginSb);
			querySb.append(WN20SCHEMA + ":" + relationResourceString );
			reflectedQuerySb.append("^" + WN20SCHEMA + ":" + relationResourceString );
			if(positiveClosure) {
				querySb.append("+");
				reflectedQuerySb.append("+");
			}
			querySb.append(queryEndSb);
			reflectedQuerySb.append(queryEndSb);
			
			logger.debug(querySb.toString());
			logger.debug(reflectedQuerySb.toString());
			Query query = QueryFactory.create(querySb.toString());
			QueryExecution qe = QueryExecutionFactory.create(query, unifiedModel);
			boolean relationshipExists = qe.execAsk();
			if(relationshipExists) {
				relations.add(relation);
			}
			query = QueryFactory.create(reflectedQuerySb.toString());
			qe = QueryExecutionFactory.create(query, unifiedModel);
			relationshipExists = qe.execAsk();
			if(relationshipExists) {
				relations.add(relation.getInverse());
			}
		}
						
		if(relations.size() > 0) {
			return relations;
		} else {
			return Collections.singleton(Relation.NONE);
		}
	
	}
	
	/**
	 * Returns a List of the synsets containing all words in the word group.
	 * From wordnet.princeton.edu: "Synonyms--words that denote the same concept and are interchangeable in many contexts--are grouped into unordered sets (synsets)." 
	 * @param wordGroup	list of words
	 * @return list of synsets containing all words in the word-group; list of size 0 if no such synset exists
	 */
	public Set<Resource> getSynsets(final List<String> wordGroup) {
		if(wordGroup == null || wordGroup.size() == 0) {
			throw new IllegalArgumentException("WordGroup must have 1 or more elements.");
		} else {			
			Set<Resource> synsets = new HashSet<Resource>();
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
		
	/**
	 * Returns a string of comma-delimited elements.
	 * @param collection	the collection of elements to convert to a CDL
	 * @return	a comma-delimted list of elements
	 */
	@SuppressWarnings("rawtypes")
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

	/**
	 * Returns an RDF model used by this instance
	 * @param modelType		the type of model
	 * @return	RDF model used by this instance
	 */
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
	
	/**
	 * Returns total number of triples known in the models of this instance.
	 * @see Other useful <a href="http://code.google.com/p/void-impl/wiki/SPARQLQueriesForStatistics">SPARQL utility queries</a>
	 * @return		number of known triples
	 */
	public long getTriplesCount() {
		String queryString = "SELECT (COUNT(*) AS ?numberTriples) { ?s ?p ?o  }";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, unifiedModel);
		ResultSet results = qe.execSelect(); // ( ?numberTriples = 782810 )
		long n = results.next().getLiteral("numberTriples").getLong();
		qe.close();	
		return n;
	}
	
	/**
	 * Returns the total number of entities known in the models of this instance.
	 * @return		number of known entities
	 */
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
