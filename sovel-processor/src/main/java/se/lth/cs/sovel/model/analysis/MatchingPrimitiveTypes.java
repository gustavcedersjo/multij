package se.lth.cs.sovel.model.analysis;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class MatchingPrimitiveTypes implements CheckOneAnalysis {
	private final Messager messager;

	public MatchingPrimitiveTypes(ProcessingEnvironment procEnv) {
		messager = procEnv.getMessager();
	}

	private static final Set<TypeKind> primitive = EnumSet.of(
			TypeKind.BOOLEAN,
			TypeKind.BYTE,
			TypeKind.CHAR,
			TypeKind.DOUBLE,
			TypeKind.FLOAT,
			TypeKind.INT,
			TypeKind.LONG,
			TypeKind.SHORT);

	public boolean checkOne(ExecutableElement current, ExecutableElement added) {
		List<? extends VariableElement> curPar = current.getParameters();
		List<? extends VariableElement> addPar = added.getParameters();

		assert curPar.size() == addPar.size();
		boolean result = true;

		for (int i = 0; i < curPar.size(); i++) {
			TypeMirror cur = curPar.get(i).asType();
			TypeMirror add = addPar.get(i).asType();

			if (isPrimitive(cur) || isPrimitive(add)) {
				if (cur.getKind() != add.getKind()) {
					result = false;
					String message = "Dynamic dispatch over primitive types is not allowed." +
							" The parameter " + addPar.get(i).getSimpleName() +
							" is defined with " +
							(isPrimitive(add) ? "another" : "a") +
							" primitive type elsewhere.";
					messager.printMessage(Diagnostic.Kind.ERROR, message, addPar.get(i));
				}
			}
		}
		return result;
	}

	private boolean isPrimitive(TypeMirror type) {
		return primitive.contains(type.getKind());
	}
}