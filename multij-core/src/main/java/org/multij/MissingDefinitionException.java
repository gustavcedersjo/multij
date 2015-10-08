package org.multij;

public class MissingDefinitionException extends RuntimeException {
	public MissingDefinitionException() {
		super("Missing definition");
	}
}
