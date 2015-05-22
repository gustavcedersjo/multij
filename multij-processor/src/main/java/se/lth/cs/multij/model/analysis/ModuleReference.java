package se.lth.cs.multij.model.analysis;

import se.lth.cs.multij.Module;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class ModuleReference extends AnalysisBase implements ModuleReferenceAnalysis {

	public ModuleReference(ProcessingEnvironment processingEnv) {
		super(processingEnv);
	}

	@Override
	public boolean check(ExecutableElement methodRef) {
		TypeMirror type = methodRef.getReturnType();
		if (type.getKind() == TypeKind.DECLARED) {
			DeclaredType declared = (DeclaredType) type;
			Element element = declared.asElement();
			if (element.getAnnotation(Module.class) != null) {
				return true;
			}
		}
		messager().printMessage(Diagnostic.Kind.ERROR, "Module reference must refer to a type that is declared as a @Module.", methodRef);
		return false;
	}
}
