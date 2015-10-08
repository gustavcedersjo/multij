package org.multij;

public class CircularityException extends RuntimeException {
	public CircularityException() {
		super("Circular definition.");
	}
}
