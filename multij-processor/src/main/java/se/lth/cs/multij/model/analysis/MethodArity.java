package se.lth.cs.multij.model.analysis;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic;

public class MethodArity implements CheckOneAnalysis {
	private final Messager messager;

	public MethodArity(ProcessingEnvironment procEnv) {
		messager = procEnv.getMessager();
	}

	public boolean checkOne(ExecutableElement current, ExecutableElement added) {
		int numCur = current.getParameters().size();
		int numAdd = added.getParameters().size();
		if (numCur != numAdd) {
			messager.printMessage(Diagnostic.Kind.ERROR, "Wrong number of parameters: " + "expected " + numCur
					+ " but was " + numAdd + ".", added);
			return false;
		} else {
			return true;
		}
	}
}
