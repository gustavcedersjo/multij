package se.lth.cs.multij;

public class MultiJ {
	public static <T> T instance(Class<T> module) {
		ModuleRepository repo = new ModuleRepository();
		return repo.getModule(module);
	}
}
