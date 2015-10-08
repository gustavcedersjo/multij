package org.multij.model.analysis;

import javax.lang.model.element.ExecutableElement;

public interface ModuleBindingAnalysis {
	boolean check(ExecutableElement methodRef);
}
