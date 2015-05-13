package se.lth.cs.multij.model.analysis;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

public interface Analysis {
	public boolean check(List<ExecutableElement> definitions);
}
