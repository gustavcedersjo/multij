package se.lth.cs.multij;

public class MissingDefinitionException extends RuntimeException {
	public MissingDefinitionException() {
		super("Missing definition");
	}
}
