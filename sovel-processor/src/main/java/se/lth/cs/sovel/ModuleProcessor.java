package se.lth.cs.sovel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("se.lth.cs.sovel.Module")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ModuleProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element e : roundEnv.getElementsAnnotatedWith(Module.class)) {
			if (e.getKind() == ElementKind.INTERFACE) {
				generateSource((TypeElement) e);
			}
		}
		return true;
	}

	private void generateSource(TypeElement e) {
		try {
			Name interfaceName = e.getSimpleName();
			String className = interfaceName + "Sovel";
			JavaFileObject file = processingEnv.getFiler().createSourceFile(className, e);
			PrintWriter writer = new PrintWriter(file.openWriter());
			writer.format("package %s;\n", processingEnv.getElementUtils().getPackageOf(e).getQualifiedName());
			writer.format("public final class %s implements %s {\n", className, interfaceName);
			writer.println("}");
			writer.close();
		} catch (IOException ex) {
			processingEnv.getMessager().printMessage(Kind.ERROR, ex.getMessage());
		}
	}

}
