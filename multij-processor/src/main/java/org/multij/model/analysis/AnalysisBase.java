package org.multij.model.analysis;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public abstract class AnalysisBase {
	protected final ProcessingEnvironment processingEnv;

	public AnalysisBase(ProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
	}

	protected Types typeUtils() {
		return processingEnv.getTypeUtils();
	}

	protected Elements elementUtils() {
		return processingEnv.getElementUtils();
	}

	protected Messager messager() {
		return processingEnv.getMessager();
	}
}
