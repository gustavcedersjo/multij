package se.lth.cs.sovel.model.analysis;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

public interface CheckOneAnalysis extends Analysis {
	public default boolean check(List<ExecutableElement> definitions, ExecutableElement definition) {
		if (definitions.isEmpty()) {
			return true;
		} else {
			return checkOne(definitions.get(0), definition);
		}
	}
	public boolean checkOne(ExecutableElement current, ExecutableElement added);
}