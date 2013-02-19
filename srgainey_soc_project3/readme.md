# CSC750 Project #3 #
====================

This application loads [WordNet] [1] RDF files and an OWL ontology to derive single-step and multiple-step (positive closure) relationships from one group of words to another, where each group represents a [synset] [2].

### Build instructions ###
Verify that the following files are included in the root project directory:
 * Core File: wordnet-senselabels.rdf
 * Relationship Files: wordnet-causes.rdf, wordnet-entailment.rdf, wordnet-hyponym.rdf, wordnet-membermeronym.rdf, wordnet-partmeronym.rdf, wordnet-substancemeronym.rdf
 * Ontology Files: wnbasic.owl

Verify that the classpath includes the [Jena] [3] 2.7.4 libraries.

Build using javac:

```
> javac -cp . MyWordnetReasoner.java
> java MyWordnetReasoner <wordGroup1> <wordGroup2>
```

### Usage Examples ###

```
> java MyWordnetReasoner "call, ring" "dial"
Calculating single-step relations...   	done
	Entails
Calculating positive-closure relations... 	done.
	Entails
```

```
> java MyWordnetReasoner "social relation" "abstraction"
Calculating single-step relations...   	done
	Relationship unknown
Calculating positive-closure relations... 	done.
	Hyponym
```

```
> java MyWordnetReasoner "teach, instruct" "learn, acquire"
Calculating single-step relations...   	done
	Causes
Calculating positive-closure relations... 	done.
	Causes
```

```
> java MyWordnetReasoner "learn, acquire" "teach, instruct" 
Calculating single-step relations...   	done
	Caused By
Calculating positive-closure relations... 	done.
	Caused By
```

### Fine Print ###
Each group of words must represent a [synset] [2]; if it does not, then an error message will be displayed.

Relationships are only derived from the first group to the second; however, the reflective relationships (e.g., 'caused by' to 'causes') from group 1 to group 2 are also found.

If more than one of the same relationship is found from wordgroup 1 to wordgroup 2 (e.g., "do" "make" has several hyponymOf and hypernymOf relationships), then only one of each type is displayed.

### WordNet Relationships Used ###

<table>
  <tr>
    <th>Relation</th><th>Reflective/Inverse</th><th>a.k.a.</th>
  </tr>
  <tr>
    <td>entails</td><td>entailed by</td><td>Entails</td>
  </tr>
  <tr>
    <td>hyponymOf</td><td>hypernym of</td><td>is a</td>
  </tr>
  <tr>
    <td>meronymOf</td><td>holonym of</td><td>part of, member of, made of</td>
  </tr>
  <tr>
    <td>causes</td><td>casued by</td><td>causes</td>
  </tr>
</table>

[1]: http://wordnet.princeton.edu   "WordNet"
[2]: http://en.wikipedia.org/wiki/Synonym_ring       "synset"

[3]: http://jena.apache.org/download/index.html       "Jena"



