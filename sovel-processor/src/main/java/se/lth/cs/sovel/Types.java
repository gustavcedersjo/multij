package se.lth.cs.sovel;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Types {

	private final Map<String, Type> types;
	private List<ClassType> classes;
	private final Map<Type, Set<Type>> implies;
	private final Map<Type, Set<Type>> impliesNot;
	private final Map<Type, Set<Type>> notImpliesNot;

	private final ClassType baseType;

	public final Comparator<Type> inheritanceTotalOrder = new Comparator<Type>() {
		public int compare(Type a, Type b) {
			assert lookup(a.getName()) == a;
			assert lookup(b.getName()) == b;
			if (a == b) {
				return 0;
			} else if (superTypes(a).contains(b)) {
				return 1;
			} else if (superTypes(b).contains(a)) {
				return -1;
			} else {
				return a.getName().compareTo(b.getName());
			}
		}
	};

	private static Set<Type> superTypes(Type t) {
		return t.superTypes();
	}

	public Types() {
		types = new HashMap<>();
		classes = null;
		implies = new HashMap<>();
		impliesNot = new HashMap<>();
		notImpliesNot = new HashMap<>();
		baseType = new ClassType("java.lang.Object", null, Collections.emptySet(), false);
		types.put("java.lang.Object", baseType);
	}

	private void clearCaches() {
		classes = null;
		implies.clear();
		impliesNot.clear();
		notImpliesNot.clear();
	}

	private List<ClassType> classes() {
		if (classes == null) {
			classes = types.values().stream()
					.filter(t -> t instanceof ClassType)
					.map(t -> (ClassType) t)
					.collect(Collectors.toList());
		}
		return classes;
	}

	public boolean isDefined(String name) {
		return types.containsKey(name);
	}

	public Type lookup(String name) {
		Type result = types.get(name);
		if (result == null) {
			throw new NoSuchElementException(name);
		} else {
			return result;
		}
	}

	private ClassType lookupClass(String name) {
		Type result = types.get(name);
		if (result == null) {
			throw new NoSuchElementException(name);
		} else if (!(result instanceof ClassType)) {
			throw new IllegalStateException("Not a class: " + name);
		} else {
			return (ClassType) result;
		}
	}

	private InterfaceType lookupInterface(String name) {
		Type result = types.get(name);
		if (result == null) {
			throw new NoSuchElementException();
		} else if (!(result instanceof InterfaceType)) {
			throw new IllegalStateException("Not an interface: " + name);
		} else {
			return (InterfaceType) result;
		}
	}

	private void checkDuplicate(String name) {
		if (isDefined(name)) {
			throw new IllegalStateException("Duplicate type: " + name);
		}		
	}

	private Set<InterfaceType> collectInterfaces(Stream<String> types) {
		return types
				.map(this::lookup)
				.flatMap(t -> {
					Stream<InterfaceType> interfaces = t.interfaces().stream();
					if (t instanceof InterfaceType) {
						interfaces = Stream.concat(Stream.of((InterfaceType) t), interfaces);
					}
					return interfaces;
				})
				.collect(Collectors.toSet());
	}

	private static <T, U extends T> Stream<T> prependStream(U element, Stream<? extends T> stream) {
		return Stream.concat(Stream.<T> of(element), stream);
	}

	public void addClass(String name, String superClassName, Collection<String> interfaceNames, boolean isFinal) {
		checkDuplicate(name);
		ClassType superClass;
		Set<InterfaceType> interfaces;
		if (superClassName == null) {
			superClass = baseType;
			interfaces = collectInterfaces(interfaceNames.stream());
		} else {
			superClass = lookupClass(superClassName);
			interfaces = collectInterfaces(prependStream(superClassName, interfaceNames.stream()));
		}
		assert !superClass.isFinal;
		ClassType type = new ClassType(name, superClass, interfaces, isFinal);
		clearCaches();
		types.put(name, type);
	}

	public void addInterface(String name, Collection<String> superInterfaceNames) {
		checkDuplicate(name);
		Set<InterfaceType> interfaces = collectInterfaces(superInterfaceNames.stream());
		InterfaceType type = new InterfaceType(name, interfaces);
		clearCaches();
		types.put(name, type);
	}

	public abstract class Type {
		private final String name;
		private final ClassType superClass;
		private final Set<InterfaceType> interfaces;
		private final Set<Type> superTypes;

		public Type(String name, ClassType superClass, Set<InterfaceType> interfaces) {
			this.name = name;
			this.superClass = superClass;
			this.interfaces = interfaces;
			this.superTypes = superTypes();
		}

		private Set<Type> superTypes() {
			Stream.Builder<Type> superClasses = Stream.builder();
			Type sup = superClass;
			while (sup != null) {
				superClasses.add(sup);
				sup = sup.superClass;
			}
			return Stream.concat(superClasses.build(), interfaces.stream())
					.collect(Collectors.toSet());
		}

		public String getName() {
			return name;
		}

		protected ClassType superClass() {
			return superClass;
		}

		protected Set<InterfaceType> interfaces() {
			return interfaces;
		}

		public Set<Type> implies() {
			return cached(implies, this::superTypes);
		}

		public abstract Set<Type> impliesNot();
		public abstract Set<Type> notImpliesNot();

		protected Set<Type> cached(Map<Type, Set<Type>> cache, Supplier<Set<Type>> compute) {
			return cache.computeIfAbsent(this, t -> Collections.unmodifiableSet(compute.get()));
		}
	}

	private class ClassType extends Type {
		private final boolean isFinal;

		public ClassType(String name, ClassType superClass, Set<InterfaceType> interfaces, boolean isFinal) {
			super(name, superClass, interfaces);
			this.isFinal = isFinal;
		}

		public boolean isSubclassOf(ClassType that) {
			if (superClass() == that) {
				return true;
			} else if (superClass() != null) {
				return superClass().isSubclassOf(that);
			} else {
				return false;
			}
		}

		public Set<Type> impliesNot() {
			return cached(impliesNot, () -> {
				if (isFinal) {
					Set<Type> implies = implies();
					return types.values().stream()
							.filter(t -> !implies.contains(t))
							.filter(t -> t != this)
							.collect(Collectors.toSet());
				} else {
					return classes().stream()
							.filter(that -> this != that)
							.filter(that -> !that.isSubclassOf(this) && !this.isSubclassOf(that))
							.collect(Collectors.toSet());
				}
			});
		}

		public Set<Type> notImpliesNot() {
			return cached(notImpliesNot, () -> classes().stream()
					.filter(t -> t.isSubclassOf(this))
					.collect(Collectors.toSet()));
		}

		@Override
		public String toString() {
			String fin = isFinal ? "final " : "";
			return fin + "class " + getName();
		}

	}

	private class InterfaceType extends Type {
		public InterfaceType(String name, Set<InterfaceType> interfaces) {
			super(name, baseType, interfaces);
		}

		public Set<Type> notImpliesNot() {
			return cached(notImpliesNot, () -> types.values().stream()
					.filter(t -> t.interfaces().contains(this))
					.collect(Collectors.toSet()));
		}

		public Set<Type> impliesNot() {
			return cached(impliesNot, () -> classes().stream()
					.filter(t -> t.isFinal)
					.filter(t -> !t.interfaces().contains(this))
					.collect(Collectors.toSet()));
		}

		@Override
		public String toString() {
			return "interface " + getName();
		}

	}
}
