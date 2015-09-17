package se.lth.cs.multij.codegen;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import se.lth.cs.multij.*;
import se.lth.cs.multij.model.DecisionTree;
import se.lth.cs.multij.model.EntryPoint;
import se.lth.cs.multij.model.Module;
import se.lth.cs.multij.model.MultiMethod;

public class CodeGenerator {
	private final ProcessingEnvironment processingEnv;

	public CodeGenerator(ProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
	}

	private Types typeUtil() {
		return processingEnv.getTypeUtils();
	}

	private String className(Element element) {
		String name = "MultiJ";
		do {
			name = element.getSimpleName() + "$" + name;
			element = element.getEnclosingElement();
		} while (element.getKind() != ElementKind.PACKAGE);
		return name;
	}

	public void generateSource(Module module) {
		try {
			String className = className(module.getTypeElement());
			PackageElement pkg = processingEnv.getElementUtils()
					.getPackageOf(module.getTypeElement());
			String qualifiedName = pkg.isUnnamed() ? className : pkg.getQualifiedName() + "." + className;
			JavaFileObject file = processingEnv.getFiler().createSourceFile(qualifiedName, module.getTypeElement());
			PrintWriter writer = new PrintWriter(file.openWriter());

			if (!pkg.isUnnamed()) {
				writer.format("package %s;\n", pkg.getQualifiedName());
			}
			writer.format("public final class %s implements %s, %s {\n", className, module.getTypeElement()
					.getQualifiedName(), MultiJModule.class.getCanonicalName());

			generateModuleRefs(module, writer);
			generateInjecions(module, writer);
			generateCachedAttrs(module, writer);
			generateMultiMethods(module, writer);

			writer.println("}");
			writer.close();
		} catch (IOException ex) {
			processingEnv.getMessager().printMessage(Kind.ERROR, ex.getMessage());
		}
	}

	private void generateMultiMethods(Module module, PrintWriter writer) {
		for (MultiMethod multiMethod : module.getMultiMethods()) {
			for (EntryPoint entryPoint : multiMethod.getEntryPoints()) {
				MethodCodeGenerator gen = new MethodCodeGenerator(module.getTypeElement().getQualifiedName(),
						writer, entryPoint);
				gen.generateCode();
			}
		}
	}

	private void generateModuleRefs(Module module, PrintWriter writer) {
		for (ExecutableElement modRef : module.getModuleReferences()) {
			writer.format("\tprivate %s module$%s;\n", modRef.getReturnType(), modRef.getSimpleName());
		}
		for (ExecutableElement modRef : module.getModuleReferences()) {
			writer.format("\tpublic %s %s() { return module$%2$s; }\n", modRef.getReturnType(), modRef.getSimpleName());
		}
		writer.println("\tprivate boolean multij$initialized = false;");
		writer.format("\tpublic void multij$init(%s repo) {\n", ModuleRepository.class.getCanonicalName());
		writer.format("\t\tif (multij$initialized) throw new %s();\n", IllegalStateException.class.getCanonicalName());
		for (ExecutableElement modRef : module.getModuleReferences()) {
			writer.format("\t\tmodule$%s = repo.getModule(%s.class);\n", modRef.getSimpleName(), modRef.getReturnType());
		}
		writer.println("\t\tmultij$initialized = true;");
		writer.println("\t}\n");
	}

	private void generateInjecions(Module module, PrintWriter writer) {
		for (ExecutableElement attr : module.getInjectedAttributes()) {
			Name name = attr.getSimpleName();
			TypeMirror type = attr.getReturnType();
			writer.format("\tprivate boolean inj$init$%s;\n", name);
			writer.format("\tprivate %s inj$attr$%s;\n\n", type, name);
			writer.format("\tpublic %s %s() { return inj$attr$%2$s; }\n\n", type, name);
		}

		String notFound = MultiJModule.NotFound.class.getCanonicalName();
		String wrongType = MultiJModule.WrongType.class.getCanonicalName();
		String alreadySet = MultiJModule.AlreadySet.class.getCanonicalName();
		String multijModule = MultiJModule.class.getCanonicalName();
		writer.format("\tpublic %s multij$getModule(String name) throws %s {\n", multijModule, notFound);
		writer.format("\t\tswitch(name) {\n");
		for (ExecutableElement modRef : module.getModuleReferences()) {
			writer.format("\t\tcase \"%1$s\": return (%2$s) module$%1$s;\n", modRef.getSimpleName(), MultiJModule.class.getCanonicalName());
		}
		writer.format("\t\tdefault: throw new %s();\n", notFound);
		writer.format("\t\t}\n");
		writer.format("\t}\n\n");

		writer.format("\tpublic void multij$setField(String name, Object value) throws %s, %s, %s{\n", notFound, wrongType, alreadySet);
		writer.format("\t\tswitch(name) {\n");
		for (ExecutableElement inj : module.getInjectedAttributes()) {
			Name name = inj.getSimpleName();
			TypeMirror type = processingEnv.getTypeUtils().erasure(inj.getReturnType());
			writer.format("\t\tcase \"%s\":\n", name);
			writer.format("\t\t\tif (inj$init$%s) {\n", name);
			writer.format("\t\t\t\tthrow new %s();\n", alreadySet);
			writer.format("\t\t\t} else if (value instanceof %s) {\n", type);
			writer.format("\t\t\t\tinj$init$%s = true;\n", name);
			writer.format("\t\t\t\tinj$attr$%s = (%s) value;\n", name, type);
			writer.format("\t\t\t\treturn;\n");
			writer.format("\t\t\t} else {\n");
			writer.format("\t\t\t\tthrow new %s(%s.class);\n", wrongType, type);
			writer.format("\t\t\t}\n");
		}
		writer.format("\t\tdefault: throw new %s();\n", notFound);
		writer.format("\t\t}\n");
		writer.format("\t}\n\n");

		writer.format("\tpublic void multij$checkInjected(java.util.Set<%s> visited, java.util.List<String> prefix) {\n", multijModule);
		writer.format("\t\tif (visited.contains(this)) return;\n");
		writer.format("\t\tvisited.add(this);\n");
		for (ExecutableElement inj : module.getInjectedAttributes()) {
			writer.format("\t\tif (!inj$init$%s) {\n", inj.getSimpleName());
			writer.format("\t\t\tprefix.add(\"%s\");\n", inj.getSimpleName());
			writer.format("\t\t\tthrow %s.notInjected(prefix.toArray(new String[prefix.size()]));\n", InjectionException.class.getCanonicalName());
			writer.format("\t\t}\n");
		}
		writer.format("\t}\n\n");
	}

	private void generateCachedAttrs(Module module, PrintWriter writer) {
		for (ExecutableElement attr : module.getCachedAttributes()) {
			Name name = attr.getSimpleName();
			TypeMirror type = attr.getReturnType();
			Name moduleName = module.getTypeElement().getQualifiedName();
			writer.format("boolean cache$init$%s = false;\n", name);
			writer.format("boolean cache$done$%s = false;\n", name);
			writer.format("%s cache$value$%s;\n\n", type, name);
			writer.format("public synchronized %s %s() {\n", type, name);
			writer.format("\tif (cache$done$%s) {\n", name);
			writer.format("\t\treturn cache$value$%s;\n", name);
			writer.format("\t} else if (cache$init$%s) {\n", name);
			writer.format("\t\tthrow new %s();\n", CircularityException.class.getCanonicalName());
			writer.format("\t} else {\n");
			writer.format("\t\ttry {\n");
			writer.format("\t\t\tcache$init$%s = true;\n", name);
			writer.format("\t\t\tcache$value$%s = %s.super.%1$s();\n", name, moduleName);
			writer.format("\t\t\tcache$done$%s = true;\n", name);
			writer.format("\t\t\treturn cache$value$%s;\n", name);
			writer.format("\t\t} catch (%s e) {\n", CircularityException.class.getCanonicalName());
			writer.format("\t\t\tthrow e;\n");
			writer.format("\t\t} catch (%s | %s e) {\n", RuntimeException.class.getCanonicalName(), Error.class.getCanonicalName());
			writer.format("\t\t\tcache$init$%s = false;\n", name);
			writer.format("\t\t\tthrow e;\n");
			writer.format("\t\t}\n");
			writer.format("\t}\n");
			writer.format("}\n\n");
		}
	}

	private class MethodCodeGenerator implements DecisionTree.Visitor {

		private int indentation = 2;
		private final Name moduleName;
		private final PrintWriter writer;
		private final EntryPoint tree;
		private final ExecutableElement entryPoint;

		public MethodCodeGenerator(Name moduleName, PrintWriter writer, EntryPoint tree) {
			this.moduleName = moduleName;
			this.writer = writer;
			this.tree = tree;
			this.entryPoint = tree.getEntryPoint();
		}

		public void generateCode() {
			String typeParDecl;
			if (entryPoint.getTypeParameters().isEmpty()) {
				typeParDecl = "";
			} else {
				typeParDecl = entryPoint.getTypeParameters()
						.stream()
						.map(type -> {
							String name = type.toString();
							if (!type.getBounds().isEmpty()) {
								name += type.getBounds().stream()
										.map(TypeMirror::toString)
										.collect(Collectors.joining(" & ", " extends ", ""));
							}
							return name;
						})
						.collect(Collectors.joining(", ", "<", "> "));
			}
			writer.format("\tpublic %s%s %s(", typeParDecl, entryPoint.getReturnType(), entryPoint.getSimpleName());
			int i = 0;
			for (VariableElement var : entryPoint.getParameters()) {
				if (i > 0) {
					writer.print(", ");
				}
				writer.print(var.asType());
				writer.print(" p");
				writer.print(i++);
			}
			writer.println(") {");
			generateForNode(tree.getDecisionTree());
			writer.println("\t}\n");

		}

		private void generateForNode(DecisionTree decisionTree) {
			decisionTree.accept(this);
		}

		private void println(String s) {
			for (int i = 0; i < indentation; i++) {
				writer.append('\t');
			}
			writer.println(s);
		}

		@Override
		public void visitDecision(DecisionTree.DecisionNode node) {
			if (node.getDefinition().isDefault()) {
				String call = moduleName + ".super." + node.getDefinition().getSimpleName() + "(";
				int i = 0;
				for (VariableElement parElem : node.getDefinition().getParameters()) {
					TypeMirror par = parElem.asType();
					if (i > 0) {
						call += ", ";
					}
					if (!typeUtil().isSameType(par, entryPoint.getParameters().get(i).asType())) {
						call += "(" + par.toString() + ") ";
					}
					call += "p" + i++;
				}
				call += ")";
				if (node.getDefinition().getReturnType().getKind() != TypeKind.VOID) {
					println("return " + call + ";");
				} else {
					println(call + ";");
					println("return;");
				}
			} else {
				println("throw new " + MissingDefinitionException.class.getCanonicalName() + "();");
			}
		}

		@Override
		public void visitAmbiguity(DecisionTree.AmbiguityNode node) {
			println("throw new " + AmbiguityException.class.getCanonicalName() + "();");
		}

		@Override
		public void visitCondition(DecisionTree.ConditionNode node) {
			println("if (p" + node.getCondition().getArgument() + " instanceof "
					+ processingEnv.getTypeUtils().erasure(node.getCondition().getType()) + ") {");
			indentation++;
			generateForNode(node.getIsTrue());
			indentation--;
			println("} else {");
			indentation++;
			generateForNode(node.getIsFalse());
			indentation--;
			println("}");
		}
	}
}
