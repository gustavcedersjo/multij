package se.lth.cs.sovel.codegen;

import static javax.lang.model.util.ElementFilter.methodsIn;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import se.lth.cs.sovel.AmbiguityException;
import se.lth.cs.sovel.MissingDefinitionException;
import se.lth.cs.sovel.model.DecisionTree;
import se.lth.cs.sovel.model.DecisionTree.AmbiguityNode;
import se.lth.cs.sovel.model.DecisionTree.ConditionNode;
import se.lth.cs.sovel.model.DecisionTree.DecisionNode;
import se.lth.cs.sovel.model.DecisionTree.Node;
import se.lth.cs.sovel.model.DecisionTree.NodeVisitor;
import se.lth.cs.sovel.model.DecisionTreeGenerator;

public class CodeGenerator {
	private final ProcessingEnvironment processingEnv;

	public CodeGenerator(ProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
	}

	private Types typeUtil() {
		return processingEnv.getTypeUtils();
	}

	public void generateSource(TypeElement e) {
		List<ExecutableElement> methods = methodsIn(processingEnv.getElementUtils().getAllMembers((TypeElement) e));

		Set<Name> names = methods.stream()
				.filter(d -> !"java.lang.Object".equals(((TypeElement) d.getEnclosingElement()).getQualifiedName()
						.toString()))
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
			DecisionTreeGenerator generator = builder.build();
			if (generator == null) {
				return;
			}
			builders.put(name, generator);
		}

		try {
			String className = e.getSimpleName() + "Sovel";
			JavaFileObject file = processingEnv.getFiler().createSourceFile(className, e);
			PrintWriter writer = new PrintWriter(file.openWriter());

			writer.format("package %s;\n", processingEnv.getElementUtils().getPackageOf(e).getQualifiedName());
			writer.format("public final class %s implements %s {\n", className, e.getQualifiedName());

			defs.forEach(def -> {
				DecisionTreeGenerator builder = builders.get(def.getSimpleName());
				DecisionTree tree = builder.build(def);
				MethodCodeGenerator gen = new MethodCodeGenerator(e.getQualifiedName(), writer, tree, def);
				gen.generateCode();
			});

			writer.println("}");
			writer.close();
		} catch (IOException ex) {
			processingEnv.getMessager().printMessage(Kind.ERROR, ex.getMessage());
		}
	}

	private class MethodCodeGenerator implements NodeVisitor {

		private int indentation = 2;
		private final Name moduleName;
		private final PrintWriter writer;
		private final DecisionTree tree;
		private final ExecutableElement entryPoint;

		public MethodCodeGenerator(Name moduleName, PrintWriter writer, DecisionTree tree, ExecutableElement entryPoint) {
			this.moduleName = moduleName;
			this.writer = writer;
			this.tree = tree;
			this.entryPoint = entryPoint;
		}

		public void generateCode() {
			writer.println("\t/* " + tree + " */");
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
			generateForNode(tree.getRoot());
			writer.println("\t}\n");

		}

		private void generateForNode(Node node) {
			node.accept(this);
		}

		private void println(String s) {
			for (int i = 0; i < indentation; i++) {
				writer.append('\t');
			}
			writer.println(s);
		}

		@Override
		public void visitDecision(DecisionNode node) {
			if (node.getDefinition().isImplemented()) {
				String call = moduleName + ".super." + node.getDefinition().getMethodName() + "(";
				int i = 0;
				for (TypeMirror par : node.getDefinition().getParamTypes()) {
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
		public void visitAmbiguity(AmbiguityNode node) {
			println("throw new " + AmbiguityException.class.getCanonicalName() + "();");
		}

		@Override
		public void visitCondition(ConditionNode node) {
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
