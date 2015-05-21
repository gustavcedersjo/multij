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

import se.lth.cs.multij.AmbiguityException;
import se.lth.cs.multij.MissingDefinitionException;
import se.lth.cs.multij.ModuleRepository;
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
			writer.format("public final class %s implements %s {\n", className, module.getTypeElement()
					.getQualifiedName());

			generateModuleRefs(module, writer);
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
						.map(t -> t.toString())
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
