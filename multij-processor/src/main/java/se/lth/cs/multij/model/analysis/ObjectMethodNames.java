package se.lth.cs.multij.model.analysis;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic.Kind;

public class ObjectMethodNames implements Analysis {
	private static final Set<String> forbidden = Stream.of(Object.class.getDeclaredMethods())
			.map(m -> m.getName())
			.collect(Collectors.toSet());

	private final ProcessingEnvironment processingEnv;

	public ObjectMethodNames(ProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
	}

	@Override
	public boolean check(List<ExecutableElement> definitions) {
		if (definitions.isEmpty()) {
			return true;
		} else {
			ExecutableElement def = definitions.get(0);
			if (forbidden.contains(def.getSimpleName().toString())) {
				processingEnv.getMessager().printMessage(Kind.ERROR,
						"Can not create multimethods with the methods in java.lang.Object.", def);
				return false;
			} else {
				return true;
			}
		}
	}

}
