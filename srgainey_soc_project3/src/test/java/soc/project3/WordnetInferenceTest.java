package soc.project3;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class WordnetInferenceTest extends TestCase {

	private MyJenaInference inferenceEngine = null;
	
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    	inferenceEngine = new MyJenaInference();
    }

    @After
    public void tearDown() {
    }
    
    @Test
    public void testInfer() {
//    	WordnetInference.checkWordUri("learn", "teach");
    	inferenceEngine.infer();
    	
    }

}
