package se.lth.cs.sovel;

public class AmbiguityException extends RuntimeException {
	public AmbiguityException() {
		super("Ambiguous method choice.");
	}
}
