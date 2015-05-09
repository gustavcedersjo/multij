package se.lth.cs.sovel.model.analysis;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

public interface Analysis {
	public boolean check(List<ExecutableElement> definitions, ExecutableElement definition);
}