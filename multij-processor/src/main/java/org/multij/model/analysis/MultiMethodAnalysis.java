package org.multij.model.analysis;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

public interface MultiMethodAnalysis {
	boolean check(List<ExecutableElement> definitions);
}
