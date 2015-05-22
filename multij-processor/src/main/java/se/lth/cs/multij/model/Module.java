package se.lth.cs.multij.model;

import se.lth.cs.multij.Cached;
import se.lth.cs.multij.model.analysis.*;

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
	private final List<ExecutableElement> cachedAttributes;
	private final List<MultiMethod> multiMethods;

	private Module(TypeElement typeElement, List<ExecutableElement> moduleReferences, List<ExecutableElement> cachedAttributes, List<MultiMethod> multiMethods) {
		this.typeElement = typeElement;
		this.moduleReferences = moduleReferences;
		this.cachedAttributes = cachedAttributes;
		this.multiMethods = multiMethods;
	}

	public TypeElement getTypeElement() {
		return typeElement;
	}

	public List<ExecutableElement> getModuleReferences() {
		return moduleReferences;
	}

	public List<ExecutableElement> getCachedAttributes() {
		return cachedAttributes;
	}

	public List<MultiMethod> getMultiMethods() {
		return multiMethods;
	}

	public static Optional<Module> fromTypeElement(TypeElement typeElement, ProcessingEnvironment processingEnv) {
		if (typeElement.getKind() != ElementKind.INTERFACE) {
			processingEnv.getMessager().printMessage(Kind.ERROR, "Can only create modules from interfaces.",
					typeElement);
			return Optional.empty();
		}
		boolean analysisPassed = true;
		Analysis analysis = Analysis.defaultAnalysis(processingEnv);

		List<ExecutableElement> methods = methodsIn(processingEnv.getElementUtils().getAllMembers(typeElement));

		List<ExecutableElement> moduleRefs = methods.stream()
				.filter(m -> m.getParameters().isEmpty())
				.filter(m -> m.getAnnotation(se.lth.cs.multij.Module.class) != null)
				.collect(Collectors.toList());

		for (ExecutableElement ref : moduleRefs) {
			if (!analysis.checkModuleRef(ref)) {
				analysisPassed = false;
			}
		}

		List<ExecutableElement> cachedAttrs = methods.stream()
				.filter(m -> m.getParameters().isEmpty())
				.filter(m -> m.getAnnotation(Cached.class) != null)
				.filter(d -> !moduleRefs.contains(d))
				.collect(Collectors.toList());

		for (ExecutableElement attr : cachedAttrs) {
			if (!analysis.checkCachedAttr(attr)) {
				analysisPassed = false;
			}
		}

		Set<Name> methodNames = methods.stream()
				.filter(d -> !isDefinedInObject(d))
				.filter(d -> !moduleRefs.contains(d))
				.filter(d -> !cachedAttrs.contains(d))
				.map(ExecutableElement::getSimpleName)
				.collect(Collectors.toSet());

		List<List<ExecutableElement>> multiMethodDefinitions = methodNames.stream()
				.map(name -> methods.stream()
						.filter(m -> m.getSimpleName().equals(name))
						.collect(Collectors.toList()))
				.collect(Collectors.toList());

		for (List<ExecutableElement> defs : multiMethodDefinitions) {
			if (!analysis.checkMultiMethod(defs)) {
				analysisPassed = false;
			}
		}

		if (analysisPassed) {
			List<MultiMethod> multiMethods = multiMethodDefinitions.stream()
					.map(defs -> MultiMethod.fromExecutableElements(defs, processingEnv))
					.collect(Collectors.toList());

			return Optional.of(new Module(typeElement, moduleRefs, cachedAttrs, multiMethods));
		} else {
			return Optional.empty();
		}
	}

	private static boolean isDefinedInObject(ExecutableElement d) {
		return ((TypeElement) d.getEnclosingElement()).getQualifiedName().contentEquals("java.lang.Object");
	}

}
