package se.lth.cs.multij.model.analysis;

import java.util.Iterator;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;

public abstract class DefinitionComparison extends AbstractMultiMethodAnalysis {

	public DefinitionComparison(ProcessingEnvironment processingEnv) {
		super(processingEnv);
	}

	@Override
	public boolean check(List<ExecutableElement> definitions) {
		Iterator<ExecutableElement> iter = definitions.iterator();
		if (iter.hasNext()) {
			ExecutableElement current = iter.next();
			boolean result = true;
			while (iter.hasNext()) {
				if (!checkOne(current, iter.next())) {
					result = false;
				}
			}
			return result;
		} else {
			return true;
		}
	}

	public abstract boolean checkOne(ExecutableElement current, ExecutableElement added);
}
