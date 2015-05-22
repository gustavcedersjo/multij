package se.lth.cs.multij.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;

import se.lth.cs.multij.model.util.DecisionTreeBuilder;

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
