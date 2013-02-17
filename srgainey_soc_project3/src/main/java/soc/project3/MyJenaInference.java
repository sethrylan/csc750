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

		String queryString =
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX wn:<http://www.webkb.org/theKB_terms.rdf/wn#> "
                + "SELECT ?s "
                + "WHERE {"
                + "  ?s    rdf:type   ?o  . "
                + "      }";

		String query2 = 
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "	SELECT  ?synset "
				+ "	WHERE   { ?synset wn20schema:senseLabel \"bank\"@en-US . "
				+ "			  ?synset wn20schema:senseLabel  \"bank building\"@en-US . "
				+ "}	 ";

		String showPropertiesQuery = 
				"SELECT DISTINCT ?property "
				+ "			WHERE { "
				+ "			  ?s ?property ?o . "
				+ "			} "
				+ "			LIMIT 5 ";

		String findHyponymQuery = 
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?hypo "
						+ "WHERE {"
						+ "  wn20instances:synset-tank-noun-1 wn20schema:hyponymOf*  ?hypo . " 
						+ "}";
		
		String findHypernymQuery = 
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?hypo "
						+ "WHERE { ?hyper wn20schema:hyponymOf* wn20instances:synset-tank-noun-1 . }";
		
		String findAllHyponymsString =
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "	SELECT  ?aSynset "
				+ "	WHERE   { ?aSynset wn20schema:hyponymOf ?bSynset . }	";
		
		String showCausesRelations =
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?causes "
						+ "WHERE {  wn20instances:synset-remind-verb-1 wn20schema:causes  ?causes . }"
						+ "LIMIT 10";
		
		String showCausedByRelations =
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?causedby "
						+ "WHERE { "
						+ "?causedby wn20schema:causes* wn20instances:synset-remind-verb-1 . "
						+ "}"
						+ "LIMIT 10";
		

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

		String showRelations =
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?hypo ?hyper "
						+ "WHERE { "
						+ "?hyper wn20schema:hyponymOf+ wn20instances:synset-social_relation-noun-1 . "
						+ " wn20instances:synset-social_relation-noun-1 wn20schema:hyponymOf+  ?hypo . "
						+ "}"
						+ "LIMIT 10";
		
		
		// social relation is hypernym (positive closure) of:
		String showHyponymRelations =  
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT * "
						+ "WHERE { "
						+ "?hyponym wn20schema:hyponymOf+ wn20instances:synset-social_relation-noun-1 } "
//						+ "UNION {?hyponym wn20schema:hyponymOf+ wn20instances:synset-remember-verb-1 } "
						+ "LIMIT 10";
		
//		+ "UNION {?hyponym wn20schema:hyponymOf+ wn20instances:synset-remember-verb-1 } "



		// social relation is hyponym (positive closure) of:
		String showHypernymRelations =
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?hypernym "
						+ "WHERE { "
						+ " wn20instances:synset-social_relation-noun-1 wn20schema:hyponymOf+  ?hypernym . } "
//						+ "UNION { } "
						+ "LIMIT 10";
		


		String showRelations3 = 
				"PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> "
				+ "PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>"
				+ "SELECT ?relation "
						+ "WHERE { "
						+ "wn20instances:synset-remind-verb-1 ?relation wn20instances:synset-remember-verb-1 . "
						+ "}"
						+ "LIMIT 10";


		
		Query query = QueryFactory.create(showHyponymRelations);
		QueryExecution qe = QueryExecutionFactory.create(query, unifiedModel);
		ResultSet results = qe.execSelect();
//		while(results.hasNext()) {
////			System.out.println(results.next().varNames().next());
//			System.out.println(results.next().getResource("synset"));
////			System.out.println(results.next());
//		}
		ResultSetFormatter.out(System.out, results, query);
		qe.close();
		
		query = QueryFactory.create(showHypernymRelations);
		qe = QueryExecutionFactory.create(query, unifiedModel);
		results = qe.execSelect();
		ResultSetFormatter.out(System.out, results, query);
		qe.close();



		
	}
    
    
	// TODO: doesn't get inverse relations
	public List<Relation> getRelationsSparql(final List<Resource> synsetList1, final List<Resource> synsetList2) {
		if(synsetList1 == null || synsetList2 == null ) {
			throw new IllegalArgumentException("Synsets must not be null.");
		}
		List<Relation> relations = new ArrayList<Relation>();

//		System.out.println("s1= " + asCommaList(synsetList1));
//		System.out.println("s2= " + asCommaList(synsetList2));

		StringBuilder querySb = new StringBuilder();
		querySb.append("PREFIX  wn20schema: <http://www.w3.org/2006/03/wn/wn20/schema/> ");
		querySb.append("PREFIX  wn20instances: <http://www.w3.org/2006/03/wn/wn20/instances/>" );
		querySb.append("	SELECT  ?relation ");
		querySb.append("	WHERE   { ");
		
		boolean firstInWhereClause = true;
		for(Resource synset1 : synsetList1) {
			for(Resource synset2 : synsetList2) {
				querySb.append("		");
				if(!firstInWhereClause) {
					querySb.append("UNION");
				}
				querySb.append(" { wn20instances:" + synset1.getLocalName() + " ?relation wn20instances:" + synset2.getLocalName() + " } ");
				firstInWhereClause = false;
			}
		}
		querySb.append("	}");

		System.out.println(querySb.toString());
		Query query = QueryFactory.create(querySb.toString());
		QueryExecution qe = QueryExecutionFactory.create(query, unifiedModel);
		ResultSet results = qe.execSelect();
//		ResultSetFormatter.out(System.out, results, query);
		while(results.hasNext()) {
			QuerySolution next = results.next();
			System.out.println(next);
			System.out.println("  " + next.getResource("relation").getLocalName());
//			for(Entry<String, Relation> resourceStringRelation : resourceStringToRelationMap.entrySet()) {
//				System.out.println("  " + next.getResource(resourceStringRelation.getKey()));
//				if(next.getResource(resourceStringRelation.getKey()) != null ) {
//					relations.add(resourceStringRelation.getValue());
//				}
//			}
		}
		qe.close();							
		return relations;
	}

	
	
}
