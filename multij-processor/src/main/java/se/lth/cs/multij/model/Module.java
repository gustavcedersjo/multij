package se.lth.cs.multij.model;

import static javax.lang.model.util.ElementFilter.methodsIn;

import java.util.List;
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
	private final List<ExecutableElement> moduleReferences;
	private final List<MultiMethod> multiMethods;

	private Module(TypeElement typeElement, List<ExecutableElement> moduleReferences, List<MultiMethod> multiMethods) {
		this.typeElement = typeElement;
		this.moduleReferences = moduleReferences;
		this.multiMethods = multiMethods;
	}

	public TypeElement getTypeElement() {
		return typeElement;
	}

	public List<ExecutableElement> getModuleReferences() { return moduleReferences; }

	public List<MultiMethod> getMultiMethods() {
		return multiMethods;
	}

	public static Optional<Module> fromTypeElement(TypeElement typeElement, ProcessingEnvironment processingEnv) {
		if (typeElement.getKind() != ElementKind.INTERFACE) {
			processingEnv.getMessager().printMessage(Kind.ERROR, "Can only create modules from interfaces.",
					typeElement);
			return Optional.empty();
		}
		List<ExecutableElement> methods = methodsIn(processingEnv.getElementUtils().getAllMembers(typeElement));

		List<ExecutableElement> moduleRefs = methods.stream()
				.filter(m -> m.getParameters().isEmpty())
				.filter(m -> m.getAnnotation(se.lth.cs.multij.Module.class) != null)
				.collect(Collectors.toList());

		Set<Name> methodNames = methods.stream()
				.filter(d -> !isDefinedInObject(d))
				.filter(d -> !moduleRefs.contains(d))
				.map(d -> d.getSimpleName())
				.collect(Collectors.toSet());

		List<Optional<MultiMethod>> multiMethods = methodNames.stream()
				.map(name -> methods.stream()
						.filter(m -> m.getSimpleName().equals(name))
						.collect(Collectors.toList()))
				.map(defs -> MultiMethod.fromExecutableElements(defs, processingEnv))
				.collect(Collectors.toList());

		if (multiMethods.stream().allMatch(Optional::isPresent)) {
			return Optional.of(new Module(typeElement, moduleRefs, multiMethods.stream()
					.map(Optional::get)
					.collect(Collectors.toList())));
		} else {
			return Optional.empty();
		}

	}

	private static boolean isDefinedInObject(ExecutableElement d) {
		return ((TypeElement) d.getEnclosingElement()).getQualifiedName().contentEquals("java.lang.Object");
	}
}
