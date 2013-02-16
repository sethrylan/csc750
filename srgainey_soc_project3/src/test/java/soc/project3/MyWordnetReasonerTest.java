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

import static soc.project3.MyWordnetReasoner.*;

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

    private static List<String> wordGroupE1 = new ArrayList<String>() {{
		add("do");
	}};
	private static List<String> wordGroupE2 = new ArrayList<String>() {{
		add("make");
	}};
	private static Pair<List<String>,List<String>> wordGroupPairE = new Pair<List<String>,List<String>>(wordGroupE1, wordGroupE2);
	
    private static List<String> wordGroupZ1 = new ArrayList<String>() {{
		add("fungible");
		add("papistic");
	}};
	private static List<String> wordGroupZ2 = new ArrayList<String>() {{
		add("bananas");
		add("marigold");
	}};
	private static Pair<List<String>,List<String>> wordGroupPairZ = new Pair<List<String>,List<String>>(wordGroupZ1, wordGroupZ2);
    
    private Map<List<String>, Boolean> wordGroupsToSynsetMap = new HashMap<List<String>, Boolean>() {{
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
    
    private static Map<Pair<List<String>, List<String>>, List<Relation>> wordGroupsToRelationsMap = new HashMap<Pair<List<String>, List<String>>, List<Relation>>() {{
    	put(wordGroupPairA, Collections.singletonList(Relation.ENTAILMENT));
    	put(wordGroupPairB, Collections.singletonList(Relation.MERONYMY));
    	put(wordGroupPairC, Collections.singletonList(Relation.HYPONYMY));
    	put(wordGroupPairD, Collections.singletonList(Relation.CAUSE));
    	put(wordGroupPairE, new ArrayList<Relation>() {{ add(Relation.HYPONYMY); add(Relation.HYPONYMY); }});
    	put(wordGroupPairZ, Collections.singletonList(Relation.NONE));    	
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
    		Boolean synsetsExist = myReason.getSynset(entry.getKey()).size() > 0;
        	Assert.assertSame(asCommaList(entry.getKey()) + " should have at least one synset.", expected, synsetsExist);
    	}
    }

    @Test
    public void testGetRelation() {
    	for(Entry<Pair<List<String>, List<String>>, List<Relation>> entry : wordGroupsToRelationsMap.entrySet()) {
    		List<Relation> expected = entry.getValue();
    		List<Relation> actualRelations = myReason.getRelations( myReason.getSynset(entry.getKey().getfirst()), myReason.getSynset(entry.getKey().getsecond()));
    		String message = asCommaList(entry.getKey().getfirst()) + " should " + asCommaList(entry.getValue()) + " " + asCommaList(entry.getKey().getsecond());
    		Assert.assertEquals(message, expected, actualRelations);
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