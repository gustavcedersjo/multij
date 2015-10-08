package org.multij;

public class AmbiguityException extends RuntimeException {
	public AmbiguityException() {
		super("Ambiguous method choice.");
	}
}
