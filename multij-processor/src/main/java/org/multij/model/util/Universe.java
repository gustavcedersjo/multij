package org.multij.model.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public class Universe {
	private final List<TypeMirror> types;
	private final Types util;

	private Universe(Types util, List<TypeMirror> types) {
		this.util = util;
		this.types = types;
	}

	public List<TypeMirror> ifThen(TypeMirror type) {
		return types.stream()
				.filter(t -> util.isSubtype(type, t))
				.collect(toList());
	}

	public List<TypeMirror> ifThenNot(TypeMirror type) {
		if (isClass(type)) {
			if (isFinal(type)) {
				return types.stream()
						.filter(t -> !util.isSubtype(type, t))
						.collect(toList());
			} else {
				return types.stream()
						.filter(this::isClass)
						.filter(t -> !util.isSubtype(t, type))
						.filter(t -> !util.isSubtype(type, t))
						.collect(toList());
			}
		} else {
			return types.stream()
					.filter(this::isClass)
					.filter(this::isFinal)
					.filter(t -> !util.isSubtype(t, type))
					.collect(toList());
		}
	}

	public List<TypeMirror> ifNotThenNot(TypeMirror type) {
		return types.stream().filter(t -> util.isSubtype(t, type)).collect(toList());
	}

	private boolean isClass(TypeMirror type) {
		if (type.getKind() == TypeKind.DECLARED) {
			Element element = ((DeclaredType) type).asElement();
			ElementKind kind = element.getKind();
			return kind == ElementKind.CLASS || kind == ElementKind.ENUM;
		}
		return false;
	}

	private boolean isFinal(TypeMirror type) {
		if (type.getKind() == TypeKind.DECLARED) {
			Element element = ((DeclaredType) type).asElement();
			return element.getModifiers().contains(Modifier.FINAL);
		}
		return false;
	}

	public static Builder builder(Types util) {
		return new Builder(util);
	}

	public static class Builder implements Consumer<TypeMirror> {
		private final Types util;
		private final List<TypeMirror> types;
		private boolean built;

		private Builder(Types util) {
			this.util = util;
			types = new ArrayList<>();
			built = false;
		}

		public Builder add(TypeMirror type) {
			accept(type);
			return this;
		}

		public void accept(TypeMirror type) {
			if (built) {
				throw new IllegalStateException();
			}
			TypeMirror erasure = util.erasure(type);
			if (types.stream().anyMatch(t -> util.isSameType(t, erasure))) {
				return;
			}
			types.add(erasure);
		}

		public Universe build() {
			built = true;
			return new Universe(util, types);
		}
	}

	public static Universe of(Collection<TypeMirror> collect, Types util) {
		Builder builder = builder(util);
		collect.forEach(builder);
		return builder.build();
	}
}
