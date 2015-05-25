package se.lth.cs.multij.model.analysis;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.*;
import javax.lang.model.util.AbstractTypeVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class InjectedBindingTypeParameter extends AnalysisBase implements InjectedBindingAnalysis {

	private final TypeVisitor<Boolean,Void> hasTypeParameter = new SimpleTypeVisitor8<Boolean, Void>(false) {
		@Override
		public Boolean visitTypeVariable(TypeVariable t, Void v) {
			return true;
		}

		@Override
		public Boolean visitDeclared(DeclaredType t, Void v) {
			return t.getTypeArguments().stream().anyMatch(p -> !isUnboundWildcard(p));
		}

		private boolean isUnboundWildcard(TypeMirror type) {
			if (type.getKind() == TypeKind.WILDCARD) {
				WildcardType w = (WildcardType) type;
				return w.getExtendsBound() == null && w.getSuperBound() == null;
			} else {
				return false;
			}
		}
	};

	public InjectedBindingTypeParameter(ProcessingEnvironment processingEnv) {
		super(processingEnv);
	}

	@Override
	public boolean check(ExecutableElement methodRef) {
		if (methodRef.getReturnType().accept(hasTypeParameter, null)) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Injected bindings can not use type parameters.", methodRef);
			return false;
		}
		return true;
	}
}
