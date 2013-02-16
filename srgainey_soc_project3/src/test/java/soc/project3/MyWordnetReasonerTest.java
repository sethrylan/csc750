package soc.project3;

import java.util.ArrayList;
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

import com.hp.hpl.jena.rdf.model.Model;

@SuppressWarnings({"serial", "unused"})
public class MyWordnetReasonerTest extends TestCase {

	private Logger logger = LoggerFactory.getLogger(MyWordnetReasonerTest.class);
	    
	private static List<String> wordGroupA1 = new ArrayList<String>() {{
		add("call");
		add("ring");
	}};
	private static List<String> wordGroupA2 = new ArrayList<String>() {{
		add("dial");
	}};
	private static Pair<List<String>,List<String>> wordGroupPairA = new Pair<List<String>,List<String>>(wordGroupA1, wordGroupA2);
    
    private static List<String> wordGroupB1 = new ArrayList<String>() {{
		add("warp");
	}};
	private static List<String> wordGroupB2 = new ArrayList<String>() {{
		add("fabric");
		add("textile");
	}};
	private static Pair<List<String>,List<String>> wordGroupPairB = new Pair<List<String>,List<String>>(wordGroupB1, wordGroupB2);

    private static List<String> wordGroupC1 = new ArrayList<String>() {{
		add("relation");
	}};
	private static List<String> wordGroupC2 = new ArrayList<String>() {{
		add("abstraction");
	}};
	private static Pair<List<String>,List<String>> wordGroupPairC = new Pair<List<String>,List<String>>(wordGroupC1, wordGroupC2);

    private static List<String> wordGroupD1 = new ArrayList<String>() {{
		add("teach");
		add("instruct");
	}};
	private static List<String> wordGroupD2 = new ArrayList<String>() {{
		add("learn");
		add("acquire");
	}};
	private static Pair<List<String>,List<String>> wordGroupPairD = new Pair<List<String>,List<String>>(wordGroupD1, wordGroupD2);

    private static List<String> wordGroupZ1 = new ArrayList<String>() {{
		add("fungible");
		add("papistic");
	}};
	private static List<String> wordGroupZ2 = new ArrayList<String>() {{
		add("bananas");
		add("marigold");
	}};
	private static Pair<List<String>,List<String>> wordGroupPairZ = new Pair<List<String>,List<String>>(wordGroupD1, wordGroupD2);
    
    private Map<List<String>, Boolean> wordGroupsToSynsetMap = new HashMap<List<String>, Boolean>() {{
    	put(wordGroupA1, Boolean.TRUE);
    	put(wordGroupA2, Boolean.TRUE);
    	put(wordGroupB1, Boolean.TRUE);
    	put(wordGroupB2, Boolean.TRUE);
    	put(wordGroupC1, Boolean.TRUE);
    	put(wordGroupC2, Boolean.TRUE);
    	put(wordGroupD1, Boolean.TRUE);
    	put(wordGroupD2, Boolean.TRUE);
    	put(wordGroupZ1, Boolean.FALSE);
    	put(wordGroupZ2, Boolean.FALSE);
    }};
    
    private static Map<Pair<List<String>, List<String>>, Relation> wordGroupsToRelationMap = new HashMap<Pair<List<String>, List<String>>, Relation>() {{
    	put(wordGroupPairA, Relation.ENTAILMENT);
    	put(wordGroupPairB, Relation.MERONYMY);
    	put(wordGroupPairC, Relation.HYPONYMY);
    	put(wordGroupPairD, Relation.CAUSE);
    	put(wordGroupPairZ, Relation.NONE);    	
    }};
    
    private MyWordnetReasoner myReason = new MyWordnetReasoner();
    
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
    public void testModel() {
		Model model = myReason.getModel();
		Assert.assertNotNull(model);
		Assert.assertTrue("There should be over 9,000 triples.", model.size() > 9000L);
	}

    
    @Test
    public void testIsValidSynset() {
    	for(Entry<List<String>, Boolean> entry : wordGroupsToSynsetMap.entrySet()) {
        	Assert.assertSame(MyWordnetReasoner.asCommaList(entry.getKey()) + " should be a synset.", entry.getValue(), myReason.isValidSynset(entry.getKey()));
    	}
    }

    @Test
    public void testGetRelation() {
    	for(Entry<Pair<List<String>, List<String>>, Relation> entry : wordGroupsToRelationMap.entrySet()) {
    		Assert.assertEquals(entry.getValue(), myReason.getRelation(entry.getKey().getfirst(), entry.getKey().getsecond()));
    	}
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
    
	
}
