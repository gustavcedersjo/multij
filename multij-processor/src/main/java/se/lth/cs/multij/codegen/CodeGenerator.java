package se.lth.cs.multij.codegen;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import se.lth.cs.multij.AmbiguityException;
import se.lth.cs.multij.MissingDefinitionException;
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

	public void generateSource(Module module) {
		try {
			String className = module.getTypeElement().getSimpleName() + "MultiJ";
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

			for (MultiMethod multiMethod : module.getMultiMethods()) {
				for (EntryPoint entryPoint : multiMethod.getEntryPoints()) {
					MethodCodeGenerator gen = new MethodCodeGenerator(module.getTypeElement().getQualifiedName(),
							writer, entryPoint);
					gen.generateCode();
				}
			}

			writer.println("}");
			writer.close();
		} catch (IOException ex) {
			processingEnv.getMessager().printMessage(Kind.ERROR, ex.getMessage());
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