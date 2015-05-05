package se.lth.cs.sovel.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor8;

public class Definition {
	private final ExecutableElement method;

	public Definition(ExecutableElement method) {
		this.method = method;
	}

	public List<Condition> getConditions() {
		List<Condition> result = new ArrayList<>();
		int i = 0;
		for (TypeMirror t : getParamTypes()) {
			t.accept(new ConditionAdder(result, i++), null);
		}
		return result;
	}
	
	private static class ConditionAdder extends SimpleTypeVisitor8<Void, Void> {
		private final List<Condition> conditions;
		private final int parameterPosition;
		
		private ConditionAdder(List<Condition> conditions, int parameterPosition) {
			this.conditions = conditions;
			this.parameterPosition = parameterPosition;
		}

		@Override
		public Void visitArray(ArrayType t, Void p) {
			conditions.add(new Condition(parameterPosition, t));
			return null;
		}

		@Override
		public Void visitDeclared(DeclaredType t, Void p) {
			conditions.add(new Condition(parameterPosition, t));
			return null;
		}

		@Override
		public Void visitTypeVariable(TypeVariable t, Void p) {
			t.getUpperBound().accept(this, null);
			return null;
		}

		@Override
		public Void visitIntersection(IntersectionType t, Void p) {
			t.getBounds().stream().forEach(b -> b.accept(this, null));
			return null;
		}

	}

	public CharSequence getMethodName() {
		return method.getSimpleName();
	}

	public TypeMirror getReturnType() {
		return method.getReturnType();
	}

	public List<TypeMirror> getParamTypes() {
		return method.getParameters().stream().map(VariableElement::asType).collect(Collectors.toList());
	}
}