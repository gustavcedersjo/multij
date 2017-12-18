package org.multij;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleRepository {
	private static Map<Class<?>, Class<?>> classCache = new ConcurrentHashMap<>();
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
			((MultiJModule) instance).multij$init(this);
			return instance;
		}
	}

	private <T> T instantiate(Class<T> module) {
		Class<?> implClass = classCache.computeIfAbsent(module, this::lookupImplementationClass);
		try {
			return module.cast(implClass.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Class<?> lookupImplementationClass(Class<?> module) {
		if (module.getAnnotation(Module.class) != null) {
			try {
				Class<?> klass = module;
				String name = "MultiJ";
				do {
					name = klass.getSimpleName() + "$" + name;
					klass = klass.getEnclosingClass();
				} while (klass != null);
				Package pkg = module.getPackage();
				if (pkg != null) {
					name = pkg.getName() + "." + name;
				}
				return Class.forName(name);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException("Type " + module.getCanonicalName() + " is not a module.");
		}
	}
}
