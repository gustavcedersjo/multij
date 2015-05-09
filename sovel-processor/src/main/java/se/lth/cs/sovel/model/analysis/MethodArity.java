package se.lth.cs.sovel.model.analysis;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

import se.lth.cs.sovel.model.Definition;

public class MethodArity implements CheckOneAnalysis {
	private final Messager messager;

	public MethodArity(ProcessingEnvironment procEnv) {
		messager = procEnv.getMessager();
	}

	public boolean checkOne(Definition current, Definition added) {
		int numCur = current.getParamTypes().size();
		int numAdd = added.getParamTypes().size();
		if (numCur != numAdd) {
			messager.printMessage(
				Diagnostic.Kind.ERROR,
				"Wrong number of parameters: " +
					"expected "+numCur+" but was "+numAdd+".",
				added.getMethod());
			return false;
		} else {
			return true;
		}
	}
}