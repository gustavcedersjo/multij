package se.lth.cs.sovel;

import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import se.lth.cs.sovel.codegen.CodeGenerator;
import se.lth.cs.sovel.model.Module;

@SupportedAnnotationTypes("se.lth.cs.sovel.Module")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ModuleProcessor extends AbstractProcessor {
	
	private CodeGenerator codeGen;
	
	public synchronized void init(javax.annotation.processing.ProcessingEnvironment processingEnv) {
		codeGen = new CodeGenerator(processingEnv);
		super.init(processingEnv);
	};

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element e : roundEnv.getElementsAnnotatedWith(se.lth.cs.sovel.Module.class)) {
			if (e instanceof TypeElement) {
				Optional<Module> module = Module.fromTypeElement((TypeElement) e, processingEnv);
				if (module.isPresent()) {
					codeGen.generateSource(module.get());
				}
			}
		}
		return false;
	}

}
