package soc.project3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class MyJenaInference {

	private static final String WN20SCHEMA = "http://www.w3.org/2006/03/wn/wn20/schema/";
	
	public static String WNBASIC_OWL = "wnbasic.owl";
	
	private static final String WORDNET_CORE = "wordnet-senselabels.rdf";
	private static final String WORDNET_CAUSES = "wordnet-causes.rdf";
	private static final String WORDNET_ENTAILMENT = "wordnet-entailment.rdf";
	private static final String WORDNET_HYPONYM = "wordnet-hyponym.rdf";
	private static final String WORDNET_MERONYM_MEMBER = "wordnet-membermeronym.rdf";
	private static final String WORDNET_MERONYM_SUBSTANCE = "wordnet-substancemeronym.rdf";
	private static final String WORDNET_MERONYM_PART = "wordnet-partmeronym.rdf";

	private Model unifiedModel = null;
	
	public MyJenaInference() {
		Model rdfsExample = ModelFactory.createDefaultModel();
		FileManager.get().readModel(rdfsExample, WNBASIC_OWL);	    
		InfModel inferenceModel = ModelFactory.createRDFSModel(rdfsExample);
		
	    Resource a = inferenceModel.getResource(WN20SCHEMA + "substanceMeronymOf");
//		System.out.println(a);
		StmtIterator statements = a.listProperties();
		while(statements.hasNext()) {
			Statement statement = statements.nextStatement();
//			System.out.println(statement);
		}
//		Property domainProperty = inferenceModel.getProperty("http://www.w3.org/2000/01/rdf-schema#domain");
//		System.out.println(a.getProperty(domainProperty));

		// load RDF
		Model coreModel = FileManager.get().loadModel( WORDNET_CORE );
		Model causesModel = FileManager.get().loadModel( WORDNET_CAUSES );
		Model entailmentModel = FileManager.get().loadModel( WORDNET_ENTAILMENT );
		Model hyponymModel = FileManager.get().loadModel( WORDNET_HYPONYM );
		Model meronymMemberModel = FileManager.get().loadModel( WORDNET_MERONYM_MEMBER );
		Model meronymPartModel = FileManager.get().loadModel( WORDNET_MERONYM_PART );
		Model meronymSubstanceModel = FileManager.get().loadModel( WORDNET_MERONYM_SUBSTANCE );

		// load ontology
		Model ontologyModel = FileManager.get().loadModel( WNBASIC_OWL );

		unifiedModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, ontologyModel );
		unifiedModel.add(causesModel, Boolean.TRUE); // include reified statements
		unifiedModel.add(entailmentModel, Boolean.TRUE); // include reified statements
		unifiedModel.add(coreModel, Boolean.TRUE); // include reified statements
		unifiedModel.add(hyponymModel, Boolean.TRUE); // include reified statements
		unifiedModel.add(meronymMemberModel, Boolean.TRUE); // include reified statements
		unifiedModel.add(meronymPartModel, Boolean.TRUE); // include reified statements
		unifiedModel.add(meronymSubstanceModel, Boolean.TRUE); // include reified statements
	}

	
	public void infer() {
		
		// returns every synset
		String queryString =
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX wn:<http://www.webkb.org/theKB_terms.rdf/wn#> "
                + "SELECT ?s ?o "
                + "WHERE {"
                + "  ?s    rdf:type   ?o  . "
                + "      } LIMIT 1000";
				
		String queryString2 =
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX wn:<http://www.webkb.org/theKB_terms.rdf/wn#> "
                + "SELECT ?s ?p ?o "
                + "WHERE {"
                + "  ?s    ?p   ?o  . "
                + "      } LIMIT 1000";
		
		
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

		// all synsets which synset-tank-noun-1 is a hyponym of (i.e., synset-self-propelled_vehicle-noun-1)
		String findHypernymsQuery = 
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?hyper "
						+ "WHERE {"
						+ "  wn20instances:synset-tank-noun-1 wn20schema:hyponymOf*  ?hyper . " 
						+ "}";
		
		// all synsets that wn20instances:synset-tank-noun-1 is a hypernym of (synset-panzer-noun-1)
		String findHyponymsQuery = 
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?hypo "
						+ "WHERE { ?hypo wn20schema:hyponymOf* wn20instances:synset-tank-noun-1 . }";
				
		// all synsets which synset-remind-verb-1 causes (e.g., wn20instances:synset-remember-verb-)
		String showCausesRelations =
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?causes "
						+ "WHERE {  wn20instances:synset-remind-verb-1 wn20schema:causes  ?causes . }"
						+ "LIMIT 10";
		
		// all synsets which cause synset-remind-verb-1 
		String showCausedByRelations =
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?causedby "
						+ "WHERE { "
						+ "?causedby wn20schema:causes* wn20instances:synset-remind-verb-1 . "
						+ "}"
						+ "LIMIT 10";
		
		// relationships between wn20instances:synset-call-verb-2 and any of a set of other synsets
		String showCallDialRelations =
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?relation "
						+ "WHERE { "
						+ " {wn20instances:synset-call-verb-2 ?relation wn20instances:synset-dial-verb-1}  "
						+ "UNION {wn20instances:synset-call-verb-2 ?relation wn20instances:synset-dial-verb-2}  "
						+ "UNION {wn20instances:synset-call-verb-2 ?relation wn20instances:synset-dial-noun-2} "
						+ "UNION {wn20instances:synset-call-verb-2 ?relation wn20instances:synset-dial-noun-3} "
						+ "UNION {wn20instances:synset-call-verb-2 ?relation wn20instances:synset-dial-noun-4} "
						+ "UNION {wn20instances:synset-call-verb-2 ?relation wn20instances:synset-dial-noun-1} "
						+ "} "
						+ "LIMIT 10";

		// positive closure hyponyms of synset-social_relation-noun-1 (e.g., synset-professional_relation-noun-1, synset-legal_relation-noun-1)
		String showPosiveClosureHyponyms =
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?hypo  "
						+ "WHERE { "
						+ "?hypo wn20schema:hyponymOf+ wn20instances:synset-social_relation-noun-1 "
						+ "}"
						+ "LIMIT 10";

		// all the relationships between two synsets
		String showRelationships = 
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?relation "
						+ "WHERE { "
						+ "?synset1 ?relation ?synset2 . "
						+ "FILTER(?synset1 = wn20instances:synset-call-verb-2 && " +
						"		?synset2 = wn20instances:synset-dial-verb-1 || wn20instances:synset-dial-verb-2 || wn20instances:synset-dial-noun-2 || wn20instances:synset-dial-noun-3 || wn20instances:synset-dial-noun-4 || wn20instances:synset-dial-noun-1)"
						+ "}"
						+ "LIMIT 10";

		// not working
		String showMultipleRelations =
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?synset1 ?relation1 ?relation2 ?synset2 "
						+ "WHERE { "
						+ " ?synset1 ?relation1 ?synset2 . "  
						+ " ?synset1 ?relation2 ?synset2 . "
						+ " FILTER ( ?relation1 = wn20schema:causes || wn20schema:entails  "
						+ "       && ?relation2 =                                            wn20schema:hyponymOf  || wn20schema:meronymOf  || wn20schema:memberMeronymOf  || wn20schema:partMeronymOf  ||  wn20schema:substanceMeronymOf "
						//+ "       && ?relation1 != ?relation2"
						+ ") "
						+ "} "
						+ "LIMIT 10";
		
		String partTwoTest1 = 
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "	SELECT  ?synset1 ?relation ?synset2 "
				+ "	WHERE   {  "
				+ "?synset1 wn20schema:senseLabel \"social relation\"@en-US . "
				+ "?synset2 wn20schema:senseLabel \"abstraction\"@en-US . "
				+ "   VALUES ?relation { ^wn20schema:causes wn20schema:entails } . "
				+ "?synset1 ?relation ?synset2 . "
				// + "?synset1 ^wn20schema:hyponymOf+ ?synset2 . "
				//+ gives inverse relations "?synset1 ^wn20schema:hyponymOf+ ?synset2 . "
				//works + "?synset1 wn20schema:hyponymOf+ ?synset2"
				//works sort of + "?synset1 ( wn20schema:causes | wn20schema:entails  | wn20schema:hyponymOf  | wn20schema:meronymOf  | wn20schema:memberMeronymOf  | wn20schema:partMeronymOf  |  wn20schema:substanceMeronymOf)+ ?synset2 "
				//+ " FILTER ( ?relation IN (wn20schema:causes,  wn20schema:entails,  wn20schema:hyponymOf, wn20schema:meronymOf, wn20schema:memberMeronymOf, wn20schema:partMeronymOf , wn20schema:substanceMeronymOf ) "
				//+ " ) "
				+ "}	 ";
		
		String showLabels = 
				"PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#> " 
				+ "	SELECT  ?thing ?label "
				+ "	WHERE   {  "
				+ "?thing rdfs:label \" \" . "
				+ "}	LIMIT 100 ";

		String partTwoTest2 = 
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "	PREFIX ff: <http://factforge.net/> "
				+ "		SELECT ?synset "
				+ "		WHERE { "
				+ "		    ?synset wn20schema:hypernymOf [ wn20schema:senseLabel ?genLex ] . "
				+ "		    FILTER (regex(STR(?genLex),\"^banana\",\"i\")) . "
				+ "		} LIMIT 20 ";
		
		String partTwoTestMeronymRelations = 
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "	SELECT  ?synset1 ?relation ?synset2 "
				+ "	WHERE   {  "
				+ "?synset1 wn20schema:senseLabel \"warp\"@en-US . "
				+ "?synset2 wn20schema:senseLabel \"fabric\"@en-US . "
				+ "?synset2 wn20schema:senseLabel \"textile\"@en-US . "
				+ "?synset1 ?relation ?synset2 . "
				+ "}";
				
		Query query = QueryFactory.create(partTwoTestMeronymRelations);
		QueryExecution qe = QueryExecutionFactory.create(query, unifiedModel);
		ResultSet results = qe.execSelect();
//		while(results.hasNext()) {
////			System.out.println(results.next().varNames().next());
//			System.out.println(results.next().getResource("synset"));
////			System.out.println(results.next());
//		}
		ResultSetFormatter.out(System.out, results, query);
		qe.close();
		
//		query = QueryFactory.create(showHypernymRelations);
//		qe = QueryExecutionFactory.create(query, unifiedModel);
//		results = qe.execSelect();
//		ResultSetFormatter.out(System.out, results, query);
//		qe.close();



		
	}
    

}