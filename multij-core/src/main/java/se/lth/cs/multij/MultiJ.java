package se.lth.cs.multij;

public class MultiJ {
	@SuppressWarnings("unchecked")
	public static <T> T instance(Class<T> module) {
		if (module.getAnnotation(Module.class) != null) {
			try {
				String name = module.getPackage().getName() + "." + module.getSimpleName() + "MultiJ";
				return (T) Class.forName(name).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException("Type " + module.getCanonicalName() + " is not a module.");
		}
	}
}
