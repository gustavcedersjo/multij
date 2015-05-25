package se.lth.cs.multij.model.analysis;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;

public class VoidBinding extends AnalysisBase implements ModuleBindingAnalysis, LazyBindingAnalysis, InjectedBindingAnalysis {

	public VoidBinding(ProcessingEnvironment processingEnv) {
		super(processingEnv);
	}

	@Override
	public boolean check(ExecutableElement definition) {
		if (definition.getReturnType().getKind() == TypeKind.VOID) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "A binding can not be void.", definition);
			return false;
		} else {
			return true;
		}
	}
}
