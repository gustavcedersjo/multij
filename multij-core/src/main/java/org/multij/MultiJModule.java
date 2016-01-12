package org.multij;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface MultiJModule {
	void multij$init(ModuleRepository repo);

	MultiJModule multij$getModule(String name) throws NotFound;

	void multij$setField(String name, Object value) throws NotFound, WrongType, AlreadySet;

	default void multij$inject(String[] path, Object value) {
		try {
			if (path.length == 0) throw new IllegalArgumentException("Empty path");
			MultiJModule mod = this;
			for (int i = 0; i < path.length - 1; i++) {
				mod = mod.multij$getModule(path[i]);
			}
			mod.multij$setField(path[path.length - 1], value);
		} catch (NotFound nf) {
			throw InjectionException.noSuchPath(path);
		} catch (AlreadySet as) {
			throw InjectionException.alreadyInjected(path);
		} catch (WrongType wt) {
			throw InjectionException.wrongType(path, wt.expected, value.getClass());
		}
	}

	default void multij$checkInjected() {
		multij$checkInjected(new HashSet<>(), new ArrayList<>());
	}
	void multij$checkInjected(Set<MultiJModule> visited, List<String> pathPrefix);

	class NotFound extends Exception {}
	class AlreadySet extends Exception {}
	class WrongType extends Exception {
		private final Class<?> expected;
		public WrongType(Class<?> expected) {
			this.expected = expected;
		}
	}

}
