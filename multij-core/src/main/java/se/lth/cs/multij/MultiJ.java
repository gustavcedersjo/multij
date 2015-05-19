package se.lth.cs.multij;

public class MultiJ {
	@SuppressWarnings("unchecked")
	public static <T> T instance(Class<T> module) {
		if (module.getAnnotation(Module.class) != null) {
			try {
				Class<?> klass = module;
				String name = "MultiJ";
				do {
					name = klass.getSimpleName() + "$" + name;
					klass = klass.getEnclosingClass();
				} while (klass != null);
				name = module.getPackage().getName() + "." + name;
				return (T) Class.forName(name).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException("Type " + module.getCanonicalName() + " is not a module.");
		}
	}
}
