package se.lth.cs.multij.model.analysis;

import java.util.Iterator;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;

public interface DefinitionComparison extends MultiMethodAnalysis {
	@Override
	default boolean check(List<ExecutableElement> definitions) {
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

	boolean checkOne(ExecutableElement current, ExecutableElement added);
}
