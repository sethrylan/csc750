package soc.project3;

public enum Relation {

	ENTAILMENT("Entails"),
	MERONYMY("Meronym"),
	HYPONYMY("Hyponym"),
	CAUSE("Causes"),
	
	ENTAILMENT_I("Entailed By"),
	MERONYMY_I("Holonym Of"),
	HYPONYMY_I("Hypernym Of"),
	CAUSE_I("Caused By"),

	NONE("Relationship unknown");
	
	private String string;
    
	Relation(String string) {
		this.string = string;
	}
	
	public String toString() {
		return this.string;
	}
	
	public Relation getInverse() {
		switch(this) {
			case ENTAILMENT: return ENTAILMENT_I;
			case MERONYMY: return MERONYMY_I;
			case HYPONYMY: return HYPONYMY_I;
			case CAUSE: return CAUSE_I;
			
			case ENTAILMENT_I: return ENTAILMENT;
			case MERONYMY_I: return MERONYMY;
			case HYPONYMY_I: return HYPONYMY;
			case CAUSE_I: return CAUSE;

			default: return NONE;
		}
	}
	
}
