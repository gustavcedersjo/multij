package se.lth.cs.sovel;

public class MissingDefinitionException extends RuntimeException {
	public MissingDefinitionException() {
		super("Missing definition");
	}
}
