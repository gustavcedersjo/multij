package org.multij.model.analysis;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic;

public class ReturnTypeAnalysis extends AnalysisBase implements DefinitionComparison {
	public ReturnTypeAnalysis(ProcessingEnvironment processingEnv) {
		super(processingEnv);
	}

	@Override
	public boolean checkOne(ExecutableElement current, ExecutableElement added) {
		if (!typeUtils().isSameType(current.getReturnType(), added.getReturnType())) {
			messager().printMessage(Diagnostic.Kind.ERROR, "Method is defined with other return type elsewhere", added);
			return false;
		} else {
			return true;
		}
	}

}
