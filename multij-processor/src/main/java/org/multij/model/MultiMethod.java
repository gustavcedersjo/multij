package org.multij.model;

import org.multij.model.util.DecisionTreeBuilder;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.stream.Collectors;

public class MultiMethod {
	private final List<EntryPoint> entryPoints;

	private MultiMethod(List<EntryPoint> entryPoints) {
		this.entryPoints = entryPoints;
	}

	public List<EntryPoint> getEntryPoints() {
		return entryPoints;
	}

	public static MultiMethod fromExecutableElements(List<ExecutableElement> definitions,
		ProcessingEnvironment processingEnv) {
		DecisionTreeBuilder builder = new DecisionTreeBuilder(definitions, processingEnv.getTypeUtils());
		List<EntryPoint> entryPoints = definitions.stream()
				.map(entryPoint -> new EntryPoint(entryPoint, builder.build(entryPoint)))
				.collect(Collectors.toList());
		return new MultiMethod(entryPoints);
	}

}
