package org.multij.model.analysis;

import javax.lang.model.element.ExecutableElement;

public interface LazyBindingAnalysis {
	boolean check(ExecutableElement methodRef);
}
