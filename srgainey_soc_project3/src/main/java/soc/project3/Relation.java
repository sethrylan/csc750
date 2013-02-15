package soc.project3;

public enum Relation {

	ENTAILMENT("Entails"),
	MERONYMY("Meronym"),
	HYPONYMY("Hyponym"),
	CAUSE("Causes"),
	NONE("Relationship unknown");
	
	private String string;
    
	Relation(String string) {
		this.string = string;
	}
	
	public String getString() {
		return this.string;
	}
}
