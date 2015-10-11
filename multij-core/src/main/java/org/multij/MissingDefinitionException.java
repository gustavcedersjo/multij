package org.multij;

public class MissingDefinitionException extends RuntimeException {
	private final String module;
	private final String method;
	private final String[] types;
	public MissingDefinitionException(String module, String method, String... types) {
		super("Missing definition for " + module + "." + method + "(" + String.join(", ", types) + ")");
		this.module = module;
		this.method = method;
		this.types = types;
	}
}
