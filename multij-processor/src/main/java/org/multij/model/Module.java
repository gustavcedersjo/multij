package org.multij.model;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.model.analysis.Analysis;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static javax.lang.model.util.ElementFilter.methodsIn;

public class Module {
	private final TypeElement typeElement;
	private final List<ExecutableElement> moduleReferences;
	private final List<ExecutableElement> cachedAttributes;
	private final List<ExecutableElement> injectedAttributes;
	private final List<MultiMethod> multiMethods;

	private Module(TypeElement typeElement, List<ExecutableElement> moduleReferences, List<ExecutableElement> cachedAttributes, List<ExecutableElement> injectedAttributes, List<MultiMethod> multiMethods) {
		this.typeElement = typeElement;
		this.moduleReferences = moduleReferences;
		this.cachedAttributes = cachedAttributes;
		this.injectedAttributes = injectedAttributes;
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

	public List<ExecutableElement> getInjectedAttributes() {
		return injectedAttributes;
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

		List<ExecutableElement> methods = methodsIn(processingEnv.getElementUtils().getAllMembers(typeElement));

		List<ExecutableElement> bindings = methods.stream()
				.filter(m -> m.getParameters().isEmpty())
				.filter(m -> m.getAnnotation(Binding.class) != null)
				.collect(Collectors.toList());

		List<ExecutableElement> lazyBindings = bindings.stream()
				.filter(Module::isLazyBinding)
				.collect(Collectors.toList());

		List<ExecutableElement> moduleBindings = bindings.stream()
				.filter(m -> !lazyBindings.contains(m))
				.filter(Module::isModuleBinding)
				.collect(Collectors.toList());

		List<ExecutableElement> injectedBindings = bindings.stream()
				.filter(m -> !lazyBindings.contains(m))
				.filter(m -> !moduleBindings.contains(m))
				.collect(Collectors.toList());

		Set<Name> methodNames = methods.stream()
				.filter(d -> !isDefinedInObject(d))
				.filter(d -> !bindings.contains(d))
				.map(ExecutableElement::getSimpleName)
				.collect(Collectors.toSet());

		List<List<ExecutableElement>> multiMethodDefinitions = methodNames.stream()
				.map(name -> methods.stream()
						.filter(m -> m.getSimpleName().equals(name))
						.collect(Collectors.toList()))
				.collect(Collectors.toList());

		Analysis analysis = Analysis.defaultAnalysis(processingEnv);

		boolean analysisResult =
				checkAll(moduleBindings, analysis::checkModuleBinding) &
				checkAll(lazyBindings, analysis::checkLazyBinding) &
				checkAll(injectedBindings, analysis::checkInjectedBinding) &
				checkAll(multiMethodDefinitions, analysis::checkMultiMethod);

		if (analysisResult) {
			List<MultiMethod> multiMethods = multiMethodDefinitions.stream()
					.map(defs -> MultiMethod.fromExecutableElements(defs, processingEnv))
					.collect(Collectors.toList());

			return Optional.of(new Module(typeElement, moduleBindings, lazyBindings, injectedBindings, multiMethods));
		} else {
			return Optional.empty();
		}
	}

	private static <T> boolean checkAll(List<T> subjects, Predicate<T> check) {
		boolean result = true;
		for (T subject : subjects) {
			if (!check.test(subject)) {
				result = false;
			}
		}
		return result;
	}

	private static boolean isModuleType(TypeMirror type) {
		if (type.getKind() == TypeKind.DECLARED) {
			Element element = ((DeclaredType) type).asElement();
			return element.getAnnotation(org.multij.Module.class) != null;
		} else {
			return false;
		}
	}

	private static boolean isModuleBinding(ExecutableElement m) {
		Binding annotation = m.getAnnotation(Binding.class);
		return annotation.value() == BindingKind.MODULE || (annotation.value() == BindingKind.AUTO && isModuleType(m.getReturnType()));
	}

	private static boolean isLazyBinding(ExecutableElement m) {
		Binding annotation = m.getAnnotation(Binding.class);
		return annotation.value() == BindingKind.LAZY || (annotation.value() == BindingKind.AUTO && m.isDefault());
	}

	private static boolean isDefinedInObject(ExecutableElement d) {
		return ((TypeElement) d.getEnclosingElement()).getQualifiedName().contentEquals("java.lang.Object");
	}

}
