package se.lth.cs.multij.model.analysis;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import java.util.Arrays;
import java.util.List;

public interface Analysis {
	boolean checkMultiMethod(List<ExecutableElement> definitions);

	boolean checkModuleRef(ExecutableElement definition);

	boolean checkCachedAttr(ExecutableElement definition);

	static Analysis defaultAnalysis(ProcessingEnvironment processingEnv) {
		class Default implements Analysis {
			private final List<MultiMethodAnalysis> multiMethodAnalyses = Arrays.asList(
					new MethodArity(processingEnv),
					new MatchingPrimitiveTypes(processingEnv),
					new ReturnTypeAnalysis(processingEnv),
					new ObjectMethodNames(processingEnv),
					new DispatchOnGenerics(processingEnv)
			);
			private final List<ModuleReferenceAnalysis> moduleReferenceAnalyses = Arrays.asList(
					new ModuleReference(processingEnv)
			);
			private final List<CachedAttributeAnalysis> cachedAttributeAnalyses = Arrays.asList();

			public boolean checkMultiMethod(List<ExecutableElement> definitions) {
				return multiMethodAnalyses.stream().allMatch(analysis -> analysis.check(definitions));
			}

			public boolean checkModuleRef(ExecutableElement definition) {
				return moduleReferenceAnalyses.stream().allMatch(analysis -> analysis.check(definition));
			}

			public boolean checkCachedAttr(ExecutableElement definition) {
				return cachedAttributeAnalyses.stream().allMatch(analysis -> analysis.check(definition));
			}
		}
		return new Default();
	}

}
