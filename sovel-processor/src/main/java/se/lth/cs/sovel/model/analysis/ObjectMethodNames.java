package se.lth.cs.sovel.model.analysis;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;

import se.lth.cs.sovel.model.Definition;

public class ObjectMethodNames implements Analysis {
	private static final Set<String> forbidden = Stream
			.of(Object.class.getDeclaredMethods())
			.map(m -> m.getName())
			.collect(Collectors.toSet());
	
	private final ProcessingEnvironment processingEnv;
	
	public ObjectMethodNames(ProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
	}
	
	@Override
	public boolean check(List<Definition> current, Definition added) {
		if (forbidden.contains(added.getMethodName().toString())) {
			processingEnv.getMessager().printMessage(Kind.ERROR, "Can not create multimethods with the methods in java.lang.Object.", added.getMethod());
			return false;
		} else {
			return true;
		}
	}

}
