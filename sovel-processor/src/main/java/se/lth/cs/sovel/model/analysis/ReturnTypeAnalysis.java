package se.lth.cs.sovel.model.analysis;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class ReturnTypeAnalysis implements CheckOneAnalysis {
	private final Types util;
	private final Messager messager;

	public ReturnTypeAnalysis(ProcessingEnvironment processingEnv) {
		util = processingEnv.getTypeUtils();
		messager = processingEnv.getMessager();
	}

	@Override
	public boolean checkOne(ExecutableElement current, ExecutableElement added) {
		if (!util.isSameType(current.getReturnType(), added.getReturnType())) {
			messager.printMessage(Diagnostic.Kind.ERROR, "Method is defined with other return type elsewhere", added);
			return false;
		} else {
			return true;
		}
	}

}
