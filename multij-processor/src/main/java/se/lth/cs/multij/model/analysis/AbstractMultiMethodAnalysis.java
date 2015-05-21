package se.lth.cs.multij.model.analysis;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public abstract class AbstractMultiMethodAnalysis implements MultiMethodAnalysis {
	protected final ProcessingEnvironment processingEnv;

	public AbstractMultiMethodAnalysis(ProcessingEnvironment processingEnv) {
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
