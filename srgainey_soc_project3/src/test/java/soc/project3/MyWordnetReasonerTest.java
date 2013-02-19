package soc.project3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soc.project3.MyWordnetReasoner.ModelType;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import static soc.project3.MyWordnetReasoner.*;

@SuppressWarnings({"serial", "unused"})
public class MyWordnetReasonerTest extends TestCase {

	private Logger logger = LoggerFactory.getLogger(MyWordnetReasonerTest.class);
	
	private static double NS_TO_MS = 1000000.0;
	    
	private static final List<String> wordGroupA1 = new ArrayList<String>() {{
		add("call");
		add("ring");
	}};
	private static final List<String> wordGroupA2 = new ArrayList<String>() {{
		add("dial");
	}};
	private static final Pair<List<String>,List<String>> wordGroupPairA = new Pair<List<String>,List<String>>(wordGroupA1, wordGroupA2);
	private static final Pair<List<String>,List<String>> wordGroupPairA_I = new Pair<List<String>,List<String>>(wordGroupA2, wordGroupA1);
	
    private static final List<String> wordGroupB1 = new ArrayList<String>() {{
		add("warp");
	}};
	private static final List<String> wordGroupB2 = new ArrayList<String>() {{
		add("fabric");
		add("textile");
	}};
	private static final Pair<List<String>,List<String>> wordGroupPairB = new Pair<List<String>,List<String>>(wordGroupB1, wordGroupB2);
	private static final Pair<List<String>,List<String>> wordGroupPairB_I = new Pair<List<String>,List<String>>(wordGroupB2, wordGroupB1);

	private static final List<String> wordGroupC1 = new ArrayList<String>() {{
		add("relation");
	}};
	private static final List<String> wordGroupC2 = new ArrayList<String>() {{
		add("abstraction");
	}};
	private static final Pair<List<String>,List<String>> wordGroupPairC = new Pair<List<String>,List<String>>(wordGroupC1, wordGroupC2);
	private static final Pair<List<String>,List<String>> wordGroupPairC_I = new Pair<List<String>,List<String>>(wordGroupC2, wordGroupC1);

    private static final List<String> wordGroupD1 = new ArrayList<String>() {{
		add("teach");
		add("instruct");
	}};
	private static final List<String> wordGroupD2 = new ArrayList<String>() {{
		add("learn");
		add("acquire");
	}};
	private static final Pair<List<String>,List<String>> wordGroupPairD = new Pair<List<String>,List<String>>(wordGroupD1, wordGroupD2);
	private static final Pair<List<String>,List<String>> wordGroupPairD_I = new Pair<List<String>,List<String>>(wordGroupD2, wordGroupD1);

    private static final List<String> wordGroupE1 = new ArrayList<String>() {{
		add("do");
	}};
	private static final List<String> wordGroupE2 = new ArrayList<String>() {{
		add("make");
	}};
	private static final Pair<List<String>,List<String>> wordGroupPairE = new Pair<List<String>,List<String>>(wordGroupE1, wordGroupE2);
	private static final Pair<List<String>,List<String>> wordGroupPairE_I = new Pair<List<String>,List<String>>(wordGroupE2, wordGroupE1);

    private static final List<String> wordGroupF1 = new ArrayList<String>() {{
		add("breathe in");
		add("inhale");
		add("inspire");
	}};
	private static final List<String> wordGroupF2 = new ArrayList<String>() {{
		add("breathe");
		add("respire");
		add("suspire");
		add("take a breath");
	}};
	private static final Pair<List<String>,List<String>> wordGroupPairF = new Pair<List<String>,List<String>>(wordGroupF1, wordGroupF2);
	private static final Pair<List<String>,List<String>> wordGroupPairF_I = new Pair<List<String>,List<String>>(wordGroupF2, wordGroupF1);
	
    private static final List<String> wordGroupZ1 = new ArrayList<String>() {{
		add("fungible");
		add("papistic");
	}};
	private static final List<String> wordGroupZ2 = new ArrayList<String>() {{
		add("bananas");
		add("marigold");
	}};
	private static final Pair<List<String>,List<String>> wordGroupPairZ = new Pair<List<String>,List<String>>(wordGroupZ1, wordGroupZ2);
	private static final Pair<List<String>,List<String>> wordGroupPairZ_I = new Pair<List<String>,List<String>>(wordGroupZ2, wordGroupZ1);

    private static final Map<List<String>, Boolean> wordGroupsToSynsetMap = new HashMap<List<String>, Boolean>() {{
    	put(wordGroupA1, Boolean.TRUE);
    	put(wordGroupA2, Boolean.TRUE);
    	put(wordGroupB1, Boolean.TRUE);
    	put(wordGroupB2, Boolean.TRUE);
    	put(wordGroupC1, Boolean.TRUE);
    	put(wordGroupC2, Boolean.TRUE);
    	put(wordGroupD1, Boolean.TRUE);
    	put(wordGroupD2, Boolean.TRUE);
    	put(wordGroupE1, Boolean.TRUE);
    	put(wordGroupE2, Boolean.TRUE);
    	put(wordGroupF1, Boolean.TRUE);
    	put(wordGroupF2, Boolean.TRUE);
    	put(wordGroupZ1, Boolean.FALSE);
    	put(wordGroupZ2, Boolean.FALSE);
    }};
    
    private static final Map<Pair<List<String>, List<String>>, Set<Relation>> wordGroupsToRelationsMap = new HashMap<Pair<List<String>, List<String>>, Set<Relation>>() {{
    	put(wordGroupPairA, Collections.singleton(Relation.ENTAILMENT));
    	put(wordGroupPairA_I, Collections.singleton(Relation.ENTAILMENT_I));
    	put(wordGroupPairB, Collections.singleton(Relation.MERONYMY));
    	put(wordGroupPairB_I, Collections.singleton(Relation.MERONYMY_I));
    	put(wordGroupPairC, Collections.singleton(Relation.HYPONYMY));
    	put(wordGroupPairC_I, Collections.singleton(Relation.HYPONYMY_I));
    	put(wordGroupPairD, Collections.singleton(Relation.CAUSE));
    	put(wordGroupPairD_I, Collections.singleton(Relation.CAUSE_I));
    	put(wordGroupPairE, new HashSet<Relation>() 
    					{{  add(Relation.HYPONYMY);
    						add(Relation.HYPONYMY);
    						add(Relation.HYPONYMY_I);
    						add(Relation.HYPONYMY_I);
    						add(Relation.HYPONYMY_I);
    						add(Relation.HYPONYMY_I);
    					}});
    	put(wordGroupPairE_I, new HashSet<Relation>() 
				{{  add(Relation.HYPONYMY);
					add(Relation.HYPONYMY);
					add(Relation.HYPONYMY);
					add(Relation.HYPONYMY);
					add(Relation.HYPONYMY_I);
					add(Relation.HYPONYMY_I);
				}});
    	
    	put(wordGroupPairF, new HashSet<Relation>() {{  
    			add(Relation.HYPONYMY);
				add(Relation.ENTAILMENT_I);
			}});
    	put(wordGroupPairF_I, new HashSet<Relation>() {{
    			add(Relation.HYPONYMY_I);
				add(Relation.ENTAILMENT);
			}});

    	put(wordGroupPairZ, Collections.singleton(Relation.NONE));
    	put(wordGroupPairZ_I, Collections.singleton(Relation.NONE));    	
    }};
    
    
    private static final List<String> wordGroupAA1 = new ArrayList<String>() {{
		add("social relation");
	}};
	private static final List<String> wordGroupAA2 = new ArrayList<String>() {{
		add("abstraction");
	}};
	private static final Pair<List<String>,List<String>> wordGroupPairAA = new Pair<List<String>,List<String>>(wordGroupAA1, wordGroupAA2);
	private static final Pair<List<String>,List<String>> wordGroupPairAA_I = new Pair<List<String>,List<String>>(wordGroupAA2, wordGroupAA1);
    
	private static final Map<Pair<List<String>, List<String>>, Set<Relation>> positiveClosureWordGroupsToRelationsMap = new HashMap<Pair<List<String>, List<String>>, Set<Relation>>() {{
    	put(wordGroupPairAA, Collections.singleton(Relation.HYPONYMY));
    	put(wordGroupPairAA_I, Collections.singleton(Relation.HYPONYMY_I));
    }};
    
    
	String showMultipleRelations =
			"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
			+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
			+ "SELECT ?synset1 ?relation1 ?relation2 ?synset2 "
					+ "WHERE { "
					+ "      ?synset1 ?relation1 ?synset2 .  "  
					+ "      ?synset2 ?relation2 ?synset1 . "  
					+ " FILTER ( ?relation1 IN ( wn20schema:causes, wn20schema:hyponymOf, wn20schema:entails, wn20schema:meronymOf)  "
					+ "       && ?relation2 IN ( wn20schema:causes, wn20schema:hyponymOf, wn20schema:entails, wn20schema:meronymOf) "
					+ "       && ?relation1 != ?relation2"
					+ ") "
					+ "} ";

    
	/**
	 * Shows properties
		| <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>    |
		| <http://www.w3.org/2000/01/rdf-schema#domain>        |
		| <http://www.w3.org/2000/01/rdf-schema#range>         |
		| <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> |
		| <http://www.w3.org/2000/01/rdf-schema#subClassOf>    |
	 */
	String showPropertiesQuery = 
			"SELECT DISTINCT ?property "
			+ "			WHERE { "
			+ "			  ?s ?property ?o . "
			+ "			} "
			+ "			LIMIT 5 ";

	String synsetByName =
            "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
			+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
			+ "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " 
            + "SELECT  DISTINCT ?synset "
            + "WHERE {"
            + "   ?synset ?p ?o    .  " 
			+ "   FILTER ( ?synset = wn20instances:synset-tank-noun-1) "
            + "      } LIMIT 1000";

	// returns every synset
	String queryString =
            "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
            + "PREFIX wn:<http://www.webkb.org/theKB_terms.rdf/wn#> "
            + "SELECT ?s ?o "
            + "WHERE {"
            + "  ?s    rdf:type   ?o  . "
            + "      } LIMIT 1000";

	// see http://stackoverflow.com/questions/2930246/exploratory-sparql-queries
	String showClasses = 
			"SELECT DISTINCT ?class " +
			"WHERE { ?s a ?class . } " +
			"LIMIT 25 " +
			"OFFSET 0";	
	
	String showProperties = 
			"SELECT DISTINCT ?property " +
			"WHERE { ?s ?property ?o .} " +
			"LIMIT 5";
	
    private static MyWordnetReasoner testReasoner = new MyWordnetReasoner();
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
	@Test
    public void testGetModel() {
		for(ModelType modelType : ModelType.values()) {
			Model model = testReasoner.getModel(modelType);
			Assert.assertNotNull(model);
			if(modelType == ModelType.CORE) {
				Assert.assertTrue("There should be over 9,000 triples.", model.size() > 9000L);
			}
		}
	}

    @Test
    public void testIsValidSynset() {
    	for(Entry<List<String>, Boolean> entry : wordGroupsToSynsetMap.entrySet()) {
    		Boolean expected = entry.getValue();
    		Boolean synsetsExist = testReasoner.getSynsets(entry.getKey()).size() > 0;
        	Assert.assertSame(asCommaList(entry.getKey()) + " should have at least one synset.", expected, synsetsExist);
    	}
    }

    @Test
    public void testGetRelations() {
    	for(Entry<Pair<List<String>, List<String>>, Set<Relation>> entry : wordGroupsToRelationsMap.entrySet()) {
    		Set<Relation> expectedRelations = entry.getValue();
    		Set<Relation> actualRelations = testReasoner.getRelations( entry.getKey().getfirst(), entry.getKey().getsecond(), false);
    		String message = asCommaList(entry.getKey().getfirst()) + " should be " + asCommaList(expectedRelations) + " " + asCommaList(entry.getKey().getsecond()) + "; not " + asCommaList(actualRelations);
    		Assert.assertEquals(expectedRelations, actualRelations);
    	}
    }
    
    @Test
    public void testGetRelationsPositiveClosure() {
    	for(Entry<Pair<List<String>, List<String>>, Set<Relation>> entry : positiveClosureWordGroupsToRelationsMap.entrySet()) {
    		Set<Relation> expectedRelations = entry.getValue();
    		Set<Relation> actualRelations = testReasoner.getRelations( entry.getKey().getfirst(), entry.getKey().getsecond(), true);
    		String message = asCommaList(entry.getKey().getfirst()) + " should be " + asCommaList(expectedRelations) + " " + asCommaList(entry.getKey().getsecond()) + "; not " + asCommaList(actualRelations);
    		Assert.assertEquals(expectedRelations, actualRelations);
    	}
    }


    /**
     * Test new implementation of getRelations against older implementation
     */
    @Test
    public void testGetRelationsEquivalent() {
    	for(Entry<Pair<List<String>, List<String>>, Set<Relation>> entry : wordGroupsToRelationsMap.entrySet()) {
    		List<String> wordGroup1 = entry.getKey().getfirst();
    		List<String> wordGroup2 = entry.getKey().getsecond();
    		Set<Resource> synsets1 = testReasoner.getSynsets(wordGroup1);
    		Set<Resource> synsets2 = testReasoner.getSynsets(wordGroup2);
    		Set<Relation> expectedRelations = entry.getValue();
    		
    		long startTime = System.nanoTime();
    		Set<Relation> oldRelations = getRelations(wordGroup1, wordGroup2);
    		long endTime = System.nanoTime();
    		//System.out.println("older version of getRelations took\t" + (endTime - startTime)/NS_TO_MS + "ms");

    		startTime = System.nanoTime();
    		Set<Relation> newRelations = testReasoner.getRelations(synsets1, synsets2, false);
    		endTime = System.nanoTime();
    		//System.out.println("newer version of getRelations took\t" + (endTime - startTime)/NS_TO_MS + "ms");

    		String message = MyWordnetReasoner.asCommaList(oldRelations) + " is not " + MyWordnetReasoner.asCommaList(newRelations);
    		assertEquals(oldRelations, newRelations);
    	}    	
    }
        
    /**
     * Test new implementation of getSynsets against older implementation
     */
    @Test
    public void testSynsetEquivalent() {
    	for(List<String> wordGroup : wordGroupsToSynsetMap.keySet()) {
    		long startTime = System.nanoTime();
    		Set<Resource> synsets = testReasoner.getSynsets(wordGroup);
    		long endTime = System.nanoTime();
    		//System.out.println("Current version of getSysnset took\t" + (endTime - startTime)/NS_TO_MS + "ms");

    		startTime = System.nanoTime();
    		Set<Resource> oldVersionSynsets = getSynsets(wordGroup);
    		endTime = System.nanoTime();
    		//System.out.println("Older version of getSysnset took\t" + (endTime - startTime)/NS_TO_MS + "ms");

    		String message = MyWordnetReasoner.asCommaList(synsets) + " is not " + MyWordnetReasoner.asCommaList(oldVersionSynsets);
    		assertEquals(message, oldVersionSynsets, synsets);
    	}
    }
    
    @Test
    public void testTotalTriples() {
    	Assert.assertTrue("There should be over 9,000 triples.", testReasoner.getTriplesCount() > 9000);
    }

    
    private static class Pair<K, V> {
		private final K first;
		private final V second;
		public Pair(K first, V second) {
			this.first = first;
			this.second = second;
		}
		public K getfirst() {
			return first;
		}
		public V getsecond() {
			return second;
		}
    }
    
    
	/* Produces a query in the form:
	 * SELECT ?relation
	 * WHERE
	 * { ?synset1 wn20schema:senseLabel "learn"@en-US .
	 *   ?synset1 wn20schema:senseLabel "acquire"@en-US .
	 *   ?synset2 wn20schema:senseLabel "teach"@en-US .
	 *   ?synset2 wn20schema:senseLabel "instruct"@en-US .
	 *   ?synset1 ?relation ?synset2
	 * }
	 */
	private final Set<Relation> getRelations(final List<String> wordGroup1, final List<String> wordGroup2) {
		if(wordGroup1 == null || wordGroup2.size() == 0 || wordGroup2 == null || wordGroup2.size() == 0) {
			throw new IllegalArgumentException("WordGroups must have 1 or more elements.");
		}
		Set<Relation> relations = new HashSet<Relation>();
		
		StringBuilder queryBaseSb = new StringBuilder();
		queryBaseSb.append("PREFIX wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> ");
		queryBaseSb.append("SELECT ?relation ");
		queryBaseSb.append("WHERE { ");
		queryBaseSb.append(" FILTER ( ?relation IN (wn20schema:causes,  wn20schema:entails,  wn20schema:hyponymOf, wn20schema:meronymOf)) . ");
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
		QueryExecution qe = QueryExecutionFactory.create(query, testReasoner.getModel(ModelType.UNIFIED));
		ResultSet results = qe.execSelect();
		while(results.hasNext()) {
			Resource next = results.next().getResource("relation");
			if(next != null) {
				Relation relation = resourceStringToRelationMap.get(next.getLocalName());
				relations.add(relation);
			}
		}
		
		query = QueryFactory.create(inverseRelationQuerySb.toString());
		qe = QueryExecutionFactory.create(query, testReasoner.getModel(ModelType.UNIFIED));
		results = qe.execSelect();
		while(results.hasNext()) {
			Resource next = results.next().getResource("relation");
			if(next != null) {
				Relation inverseRelation = resourceStringToRelationMap.get(next.getLocalName()).getInverse();
				relations.add(inverseRelation);
			}
		}
		qe.close();	
		
		if(relations.size() > 0) {
			return relations;
		} else {
			return Collections.singleton(Relation.NONE);
		}
	}

    /**
     * Queries for synsets using a StatementIterator
     * @param wordGroup group of words to find synsets for
     * @return any and all synsets for the words in wordGroup
     */
	private final Set<Resource> getSynsets(List<String> wordGroup) {
		if(wordGroup == null || wordGroup.size() == 0) {
			throw new IllegalArgumentException("WordGroup must have 1 or more elements.");
		} else {			
			Set<Resource> synsets = new HashSet<Resource>();
			final String firstWord = wordGroup.get(0);
			Property senseLabelProperty = testReasoner.getModel(ModelType.UNIFIED).getProperty(WN20SCHEMA_URI + "senseLabel");
			//Statements are formed [Subject, Predicate, Object]
			//e.g., [http://www.w3.org/2006/03/wn/wn20/instances/synset-teach-verb-1, http://www.w3.org/2006/03/wn/wn20/schema/senseLabel, "teach"@en-US]
	        StmtIterator statements = testReasoner.getModel(ModelType.UNIFIED).listStatements(
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

	
}
