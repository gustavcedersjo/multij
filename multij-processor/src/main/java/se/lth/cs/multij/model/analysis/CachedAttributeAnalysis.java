package se.lth.cs.multij.model.analysis;

import javax.lang.model.element.ExecutableElement;

public interface CachedAttributeAnalysis {
	boolean check(ExecutableElement methodRef);
}
