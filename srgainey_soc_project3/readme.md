
SPARQL Query Interface to WordNet:
http://wordnet.rkbexplorer.com/sparql/


    'entails': 'Entails',
    'hyponymOf': 'IsA',
    'instanceOf': 'InstanceOf',
    'memberMeronymOf': 'MemberOf',
    'partMeronymOf': 'PartOf',
    'sameVerbGroupAs': 'SimilarTo',
    'similarTo': 'SimilarTo',
    'substanceMeronymOf': '~MadeOf',


## WordNet files
Core:
wordnet-senselabels.rdf

Relationships:
wordnet-causes.rdf
wordnet-entailment.rdf
wordnet-hyponym.rdf
wordnet-membermeronym.rdf
wordnet-partmeronym.rdf
wordnet-substancemeronym.rdf

## Useful SPARQL Queries
http://stackoverflow.com/questions/2930246/exploratory-sparql-queries
Show Classes:
```
SELECT DISTINCT ?class
WHERE {
  ?s a ?class .
}
LIMIT 25
OFFSET 0
```

```
SELECT DISTINCT ?property
WHERE {
  ?s ?property ?o .
}
LIMIT 5
```

Return properties used on any instances of triple, rdf:type of http://xmlns.com/foaf/0.1/Person.
```
SELECT DISTINCT ?property
WHERE {
  ?s a <http://xmlns.com/foaf/0.1/Person>;
     ?property ?o .
}
```
