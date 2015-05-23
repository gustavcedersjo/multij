package se.lth.cs.multij;

public class InjectionException extends RuntimeException {
	public InjectionException(String message) {
		super(message);
	}

	public static InjectionException noSuchPath(String[] path) {
		return new InjectionException("Could not find path " + String.join(".", path));
	}

	public static InjectionException alreadyInjected(String[] path) {
		return new InjectionException("Path " + String.join(".", path) + " is already injected.");
	}

	public static InjectionException wrongType(String[] path, Class<?> expected, Class<?> actual) {
		return new InjectionException("Injected object for " + String.join(".", path) + " not of compatible type. Expected " + expected + " but was " + actual);
	}

	public static InjectionException notInjected(String[] path) {
		return new InjectionException("Path " + String.join(".", path) + " was not injected.");
	}
}
