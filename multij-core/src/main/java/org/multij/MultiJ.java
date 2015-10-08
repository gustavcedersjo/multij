package org.multij;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MultiJ {
	public static <T> T instance(Class<T> module) {
		return from(module).instance();
	}

	public static <T> Builder<T> from(Class<T> module) {
		return new Builder<>(module, Collections.emptyMap());
	}

	public static class Builder<T> {
		private final Class<T> module;
		private final Map<String, Object> injections;

		private Builder(Class<T> module, Map<String, Object> injections) {
			this.module = module;
			this.injections = injections;
		}

		public Injection<T> bind(String path) {
			return new Injection<>(this, path);
		}
		public T instance() {
			ModuleRepository repo = new ModuleRepository();
			T instance = repo.getModule(module);
			MultiJModule module = (MultiJModule) instance;
			for (Map.Entry<String, Object> entry : injections.entrySet()) {
				module.multij$inject(entry.getKey().split("\\."), entry.getValue());
			}
			module.multij$checkInjected();
			return instance;
		}
	}

	public static class Injection<T> {
		private final Builder<T> builder;
		private final String path;

		private Injection(Builder<T> builder, String path) {
			this.builder = builder;
			this.path = path;
		}

		public Builder<T> to(Object obj) {
			Map<String, Object> map = new LinkedHashMap<>(builder.injections);
			map.put(path, obj);
			return new Builder<>(builder.module, map);
		}
	}
}
