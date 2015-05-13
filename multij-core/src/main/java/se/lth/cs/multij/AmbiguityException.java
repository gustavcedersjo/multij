package se.lth.cs.multij;

public class AmbiguityException extends RuntimeException {
	public AmbiguityException() {
		super("Ambiguous method choice.");
	}
}
