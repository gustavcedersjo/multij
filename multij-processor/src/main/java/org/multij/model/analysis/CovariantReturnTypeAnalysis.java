package org.multij.model.analysis;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CovariantReturnTypeAnalysis extends AnalysisBase implements MultiMethodAnalysis {
	public CovariantReturnTypeAnalysis(ProcessingEnvironment processingEnv) {
		super(processingEnv);
	}

	@Override
	public boolean check(List<ExecutableElement> definitions) {
		boolean result = true;
		for (ExecutableElement m1 : definitions) {
			for (ExecutableElement m2 : definitions) {
				if (m1 != m2 && isSignatureSubtype(m1, m2)) {
					TypeMirror r1 = m1.getReturnType();
					TypeMirror r2 = m2.getReturnType();
					if (!typeUtils().isSubtype(r1, r2)) {
						String message = String.format("Return type %s is not a subtype of %s.", r1, r2);
						messager().printMessage(Diagnostic.Kind.ERROR, message, m1);
						result = false;
					}
				}
			}
		}
		return result;
	}

	private boolean isSignatureSubtype(ExecutableElement m1, ExecutableElement m2) {
		Iterator<TypeMirror> params1 = getParameterTypes(m1).iterator();
		Iterator<TypeMirror> params2 = getParameterTypes(m2).iterator();
		while (params1.hasNext() && params2.hasNext()) {
			TypeMirror p1 = params1.next();
			TypeMirror p2 = params2.next();
			if (!typeUtils().isSubtype(p1, p2)) {
				return false;
			}
		}
		return !params1.hasNext() && !params2.hasNext();
	}

	private List<TypeMirror> getParameterTypes(ExecutableElement element) {
		return element.getParameters().stream()
				.map(VariableElement::asType)
				.collect(Collectors.toList());
	}
}
