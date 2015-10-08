package org.multij.model.analysis;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DispatchOnGenerics extends AnalysisBase implements MultiMethodAnalysis {
	private static final HasGenerics hasGenerics = new HasGenerics();

	public DispatchOnGenerics(ProcessingEnvironment processingEnv) {
		super(processingEnv);
	}

	@Override
	public boolean check(List<ExecutableElement> definitions) {
		Iterator<ExecutableElement> iter = definitions.iterator();
		if (iter.hasNext()) {
			List<? extends VariableElement> types = iter.next().getParameters();
			boolean[] noDispatch = new boolean[types.size()];
			Arrays.fill(noDispatch, true);
			while (iter.hasNext()) {
				ExecutableElement def = iter.next();
				for (int i = 0; i < noDispatch.length; i++) {
					if (noDispatch[i] && !typeUtils().isSameType(types.get(i).asType(), def.getParameters().get(i).asType())) {
						noDispatch[i] = false;
					}
				}
			}

			boolean result = true;
			for (ExecutableElement def : definitions) {
				for (int i = 0; i < noDispatch.length; i++) {
					VariableElement par = def.getParameters().get(i);
					if (!noDispatch[i] && hasGenerics(par.asType())) {
						result = false;
						messager().printMessage(Diagnostic.Kind.ERROR, "Can not do dynamic dispatch on generic type.",
								par);
					}
				}
			}
			return result;
		} else {
			return true;
		}
	}

	private boolean hasGenerics(TypeMirror typeMirror) {
		return typeMirror.accept(hasGenerics, null);
	}

	private static class HasGenerics extends SimpleTypeVisitor8<Boolean, Void> {

		public HasGenerics() {
			super(false);
		}

		@Override
		public Boolean visitDeclared(DeclaredType type, Void p) {
			return !type.getTypeArguments().isEmpty();
		}

		@Override
		public Boolean visitTypeVariable(TypeVariable type, Void p) {
			return true;
		}
	}
}
