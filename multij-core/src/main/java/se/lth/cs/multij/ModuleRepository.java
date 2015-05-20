package se.lth.cs.multij;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ModuleRepository {
	private final Map<Class<?>, Object> cache;

	public ModuleRepository() {
		cache = new HashMap<>();
	}

	public <T> T getModule(Class<T> module) {
		if (cache.containsKey(module)) {
			return module.cast(cache.get(module));
		} else {
			T instance = instantiate(module);
			cache.put(module, instance);
			try {
				instance.getClass().getMethod("multij$init", ModuleRepository.class).invoke(instance, this);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
			return instance;
		}
	}

	private <T> T instantiate(Class<T> module) {
		if (module.getAnnotation(Module.class) != null) {
			try {
				Class<?> klass = module;
				String name = "MultiJ";
				do {
					name = klass.getSimpleName() + "$" + name;
					klass = klass.getEnclosingClass();
				} while (klass != null);
				name = module.getPackage().getName() + "." + name;
				return module.cast(Class.forName(name).newInstance());
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException("Type " + module.getCanonicalName() + " is not a module.");
		}
	}
}
