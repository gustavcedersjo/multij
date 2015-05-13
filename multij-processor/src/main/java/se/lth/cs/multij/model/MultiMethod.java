package se.lth.cs.multij.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;

import se.lth.cs.multij.model.analysis.Analysis;
import se.lth.cs.multij.model.analysis.DispatchOnGenerics;
import se.lth.cs.multij.model.analysis.MatchingPrimitiveTypes;
import se.lth.cs.multij.model.analysis.MethodArity;
import se.lth.cs.multij.model.analysis.ObjectMethodNames;
import se.lth.cs.multij.model.analysis.ReturnTypeAnalysis;
import se.lth.cs.multij.model.util.DecisionTreeBuilder;

public class MultiMethod {
	private final List<EntryPoint> entryPoints;

	private MultiMethod(List<EntryPoint> entryPoints) {
		this.entryPoints = entryPoints;
	}

	public List<EntryPoint> getEntryPoints() {
		return entryPoints;
	}

	public static Optional<MultiMethod> fromExecutableElements(List<ExecutableElement> definitions,
			ProcessingEnvironment processingEnv) {
		Stream<Analysis> analyses = Stream.of(
				new MethodArity(processingEnv),
				new MatchingPrimitiveTypes(processingEnv),
				new ReturnTypeAnalysis(processingEnv),
				new ObjectMethodNames(processingEnv),
				new DispatchOnGenerics(processingEnv));
		if (analyses.allMatch(analysis -> analysis.check(definitions))) {
			DecisionTreeBuilder builder = new DecisionTreeBuilder(definitions, processingEnv.getTypeUtils());
			List<EntryPoint> entryPoints = definitions.stream()
					.map(entryPoint -> new EntryPoint(entryPoint, builder.build(entryPoint)))
					.collect(Collectors.toList());
			return Optional.of(new MultiMethod(entryPoints));
		} else {
			return Optional.empty();
		}
	}
}
