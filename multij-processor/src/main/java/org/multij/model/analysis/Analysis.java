package org.multij.model.analysis;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import java.util.Arrays;
import java.util.List;

public interface Analysis {
	boolean checkMultiMethod(List<ExecutableElement> definitions);

	boolean checkModuleBinding(ExecutableElement definition);

	boolean checkLazyBinding(ExecutableElement definition);

	boolean checkInjectedBinding(ExecutableElement definition);

	static Analysis defaultAnalysis(ProcessingEnvironment processingEnv) {
		final List<MultiMethodAnalysis> multiMethodAnalyses = Arrays.asList(
				new MethodArity(processingEnv),
				new MatchingPrimitiveTypes(processingEnv),
				new ReturnTypeAnalysis(processingEnv),
				new ObjectMethodNames(processingEnv),
				new DispatchOnGenerics(processingEnv)
		);
		final List<ModuleBindingAnalysis> moduleBindingAnalyses = Arrays.asList(
				new ModuleBinding(processingEnv),
				new VoidBinding(processingEnv)
		);
		final List<LazyBindingAnalysis> lazyBindingAnalyses = Arrays.asList(
				new VoidBinding(processingEnv)
		);
		final List<InjectedBindingAnalysis> injectedBindingAnalyses = Arrays.asList(
				new InjectedBindingTypeParameter(processingEnv),
				new VoidBinding(processingEnv)
		);
		return new Analysis() {
			public boolean checkMultiMethod(List<ExecutableElement> definitions) {
				return multiMethodAnalyses.stream().allMatch(analysis -> analysis.check(definitions));
			}
			public boolean checkModuleBinding(ExecutableElement definition) {
				return moduleBindingAnalyses.stream().allMatch(analysis -> analysis.check(definition));
			}
			public boolean checkLazyBinding(ExecutableElement definition) {
				return lazyBindingAnalyses.stream().allMatch(analysis -> analysis.check(definition));
			}
			public boolean checkInjectedBinding(ExecutableElement definition) {
				return injectedBindingAnalyses.stream().allMatch(analysis -> analysis.check(definition));
			}
		};
	}

}
