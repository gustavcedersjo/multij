package se.lth.cs.sovel;

import static javax.lang.model.util.ElementFilter.methodsIn;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import se.lth.cs.sovel.model.DecisionTree;
import se.lth.cs.sovel.model.DecisionTreeGenerator;

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

			List<ExecutableElement> methods = methodsIn(
					processingEnv.getElementUtils().getAllMembers((TypeElement) e));

			Set<Name> names = methods.stream()
					.filter(d -> d.getAnnotation(Method.class) != null)
					.map(d -> d.getSimpleName())
					.collect(Collectors.toSet());

			List<ExecutableElement> defs = methods.stream()
					.filter(m -> names.contains(m.getSimpleName()))
					.collect(Collectors.toList());

			Map<Name, List<ExecutableElement>> groups = defs.stream()
					.collect(Collectors.groupingBy(m -> m.getSimpleName()));

			Map<Name, DecisionTreeGenerator> builders = new HashMap<>();
			for (Name name : groups.keySet()) {
				DecisionTreeGenerator.Builder builder = DecisionTreeGenerator.builder(processingEnv);
				for (ExecutableElement def : groups.get(name)) {
					builder.add(def);
				}
				builders.put(name, builder.build());
			}

			writer.format("package %s;\n", processingEnv.getElementUtils().getPackageOf(e).getQualifiedName());
			writer.format("public final class %s implements %s {\n", className, interfaceName);

			defs.forEach(def -> {
				DecisionTreeGenerator builder = builders.get(def.getSimpleName());
				DecisionTree tree = builder.build(def);
				writer.println("\t/* " + tree + " */");
				writer.format("\tpublic %s %s(", def.getReturnType(), def.getSimpleName());
				int i = 0;
				for (VariableElement var : def.getParameters()) {
					if (i > 0) {
						writer.print(", ");
					}
					writer.print(var.asType());
					writer.print(" p");
					writer.print(i++);
				}
				writer.println(") {");
				tree.getRoot().generateCode(writer, interfaceName.toString(), 2);
				writer.println("\t}\n");
			});

			writer.println("}");
			writer.close();
		} catch (IOException ex) {
			processingEnv.getMessager().printMessage(Kind.ERROR, ex.getMessage());
		}
	}

}
