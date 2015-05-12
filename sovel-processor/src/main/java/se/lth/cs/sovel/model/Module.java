package se.lth.cs.sovel.model;

import static javax.lang.model.util.ElementFilter.methodsIn;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

public class Module {
	private final TypeElement typeElement;
	private final List<MultiMethod> multiMethods;
	
	private Module(TypeElement typeElement, List<MultiMethod> multiMethods) {
		this.typeElement = typeElement;
		this.multiMethods = multiMethods;
	}

	public TypeElement getTypeElement() {
		return typeElement;
	}
	
	public List<MultiMethod> getMultiMethods() {
		return multiMethods;
	}
	
	public static Optional<Module> fromTypeElement(TypeElement typeElement, ProcessingEnvironment processingEnv) {
		if (typeElement.getKind() == ElementKind.INTERFACE) {
			List<ExecutableElement> methods = methodsIn(processingEnv.getElementUtils().getAllMembers(typeElement));

			Set<Name> methodNames = methods.stream()
					.filter(d -> !"java.lang.Object".equals(((TypeElement) d.getEnclosingElement()).getQualifiedName()
							.toString()))
					.map(d -> d.getSimpleName())
					.collect(Collectors.toSet());

			List<ExecutableElement> definitions = methods.stream()
					.filter(m -> methodNames.contains(m.getSimpleName()))
					.collect(Collectors.toList());

			Map<Name, List<ExecutableElement>> groups = definitions.stream()
					.collect(Collectors.groupingBy(m -> m.getSimpleName()));

			List<Optional<MultiMethod>> multiMethods = groups.values().stream()
					.map(e -> MultiMethod.fromExecutableElements(e, processingEnv))
					.collect(Collectors.toList());

			if (multiMethods.stream().allMatch(Optional::isPresent)) {
				return Optional.of(new Module(typeElement, multiMethods.stream()
						.map(Optional::get)
						.collect(Collectors.toList())));
			} else {
				return Optional.empty();
			}
		} else {
			processingEnv.getMessager().printMessage(Kind.ERROR, "Can only create modules from interfaces.", typeElement);
			return Optional.empty();
		}

	}
}
