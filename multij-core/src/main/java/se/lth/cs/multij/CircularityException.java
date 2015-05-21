package se.lth.cs.multij;

public class CircularityException extends RuntimeException {
	public CircularityException() {
		super("Circular definition.");
	}
}
