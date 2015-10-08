package org.multij.model.analysis;

import javax.lang.model.element.ExecutableElement;

public interface InjectedBindingAnalysis {
	boolean check(ExecutableElement methodRef);
}
