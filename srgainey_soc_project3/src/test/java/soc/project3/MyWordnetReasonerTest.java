package soc.project3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soc.project3.MyWordnetReasoner.ModelType;

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
    	put(wordGroupZ1, Boolean.FALSE);
    	put(wordGroupZ2, Boolean.FALSE);
    }};
    
    private static final Map<Pair<List<String>, List<String>>, List<Relation>> wordGroupsToRelationsMap = new HashMap<Pair<List<String>, List<String>>, List<Relation>>() {{
    	put(wordGroupPairA, Collections.singletonList(Relation.ENTAILMENT));
    	put(wordGroupPairA_I, Collections.singletonList(Relation.ENTAILMENT_I));
    	put(wordGroupPairB, Collections.singletonList(Relation.MERONYMY));
    	put(wordGroupPairB_I, Collections.singletonList(Relation.MERONYMY_I));
    	put(wordGroupPairC, Collections.singletonList(Relation.HYPONYMY));
    	put(wordGroupPairC_I, Collections.singletonList(Relation.HYPONYMY_I));
    	put(wordGroupPairD, Collections.singletonList(Relation.CAUSE));
    	put(wordGroupPairD_I, Collections.singletonList(Relation.CAUSE_I));
    	put(wordGroupPairE, new ArrayList<Relation>() 
    					{{  add(Relation.HYPONYMY);
    						add(Relation.HYPONYMY);
    						add(Relation.HYPONYMY_I);
    						add(Relation.HYPONYMY_I);
    						add(Relation.HYPONYMY_I);
    						add(Relation.HYPONYMY_I);
    					}});
    	put(wordGroupPairE_I, new ArrayList<Relation>() 
				{{  add(Relation.HYPONYMY);
					add(Relation.HYPONYMY);
					add(Relation.HYPONYMY);
					add(Relation.HYPONYMY);
					add(Relation.HYPONYMY_I);
					add(Relation.HYPONYMY_I);
				}});
    	put(wordGroupPairZ, Collections.singletonList(Relation.NONE));
    	put(wordGroupPairZ_I, Collections.singletonList(Relation.NONE));    	
    }};
    
    private static MyWordnetReasoner myReason = new MyWordnetReasoner();
    
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
			Model model = myReason.getModel(modelType);
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
    		Boolean synsetsExist = myReason.getSynsets(entry.getKey()).size() > 0;
        	Assert.assertSame(asCommaList(entry.getKey()) + " should have at least one synset.", expected, synsetsExist);
    	}
    }

    @Test
    public void testGetRelation() {
    	for(Entry<Pair<List<String>, List<String>>, List<Relation>> entry : wordGroupsToRelationsMap.entrySet()) {
    		List<Relation> expected = entry.getValue();
    		List<Relation> actualRelations = myReason.getRelations( myReason.getSynsets(entry.getKey().getfirst()), myReason.getSynsets(entry.getKey().getsecond()));
    		String message = asCommaList(entry.getKey().getfirst()) + " should " + asCommaList(entry.getValue()) + " " + asCommaList(entry.getKey().getsecond());
    		Assert.assertEquals(message, expected, actualRelations);
    	}
    }
    /*
    @Test
    public void testSynsetEquivalentsInSparql() {
    	for(List<String> wordGroup : wordGroupsToSynsetMap.keySet()) {
    		long startTime = System.currentTimeMillis();
    		List<Resource> synsets = myReason.getSynsets(wordGroup);
    		long endTime = System.currentTimeMillis();
    		System.out.println("getSysnset took " + (endTime - startTime) + "ms");

    		startTime = System.currentTimeMillis();
    		List<Resource> sparqlSynsets = myReason.getSynsetsSparql(wordGroup);
    		endTime = System.currentTimeMillis();
    		System.out.println("getSparqlSysnset took " + (endTime - startTime) + "ms");

    		String message = MyWordnetReasoner.asCommaList(synsets) + " is not " + MyWordnetReasoner.asCommaList(sparqlSynsets);
    		assertTrue(message, synsets.containsAll(sparqlSynsets));
    		assertTrue(message, sparqlSynsets.containsAll(synsets));
    	}
    }
    */
    
    /***** Older get synset for comparison purposes; SPARQL query runs at ~1% of original time.
     * 
	protected List<Resource> getSynset(List<String> wordGroup) {
		if(wordGroup == null || wordGroup.size() == 0) {
			throw new IllegalArgumentException("WordGroup must have 1 or more elements.");
		} else {			
			List<Resource> synsets = new ArrayList<Resource>();
			final String firstWord = wordGroup.get(0);
			Property senseLabelProperty = coreModel.getProperty(WN20SCHEMA + "senseLabel");
			//Statements are formed [Subject, Predicate, Object]
			//e.g., [http://www.w3.org/2006/03/wn/wn20/instances/synset-teach-verb-1, http://www.w3.org/2006/03/wn/wn20/schema/senseLabel, "teach"@en-US]
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
     */

    
    
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
    
	
}
