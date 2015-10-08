package org.multij.model.analysis;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic.Kind;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObjectMethodNames extends AnalysisBase implements MultiMethodAnalysis {
	private static final Set<String> forbidden = Stream.of(Object.class.getDeclaredMethods())
			.map(Method::getName)
			.collect(Collectors.toSet());

	public ObjectMethodNames(ProcessingEnvironment processingEnv) {
		super(processingEnv);
	}

	@Override
	public boolean check(List<ExecutableElement> definitions) {
		if (definitions.isEmpty()) {
			return true;
		} else {
			ExecutableElement def = definitions.get(0);
			if (forbidden.contains(def.getSimpleName().toString())) {
				messager().printMessage(Kind.ERROR,
						"Can not create multimethods with the methods in java.lang.Object.", def);
				return false;
			} else {
				return true;
			}
		}
	}

}
