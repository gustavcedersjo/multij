package se.lth.cs.sovel.model;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class DecisionTree {
	private final Node root;

	public DecisionTree(Node root) {
		this.root = root;
	}

	public Node getRoot() {
		return root;
	}

	@Override
	public String toString() {
		return "DecisionTree("+root+")";
	}


	public static abstract class Node {
		private Node() {
		}

		public abstract <R, P> R accept(NodeVisitor<R, P> visitor, P parameter);
		
		public abstract void generateCode(PrintWriter writer, String module, int indent);
	}

	private static String indent(int size) {
		return Stream.generate(() -> "\t").limit(size).collect(Collectors.joining());
	}
	
	public static interface NodeVisitor<R, P> {
		public R visitDecision(DecisionNode node, P parameter);

		public R visitAmbiguity(AmbiguityNode node, P parameter);

		public R visitCondition(ConditionNode node, P parameter);
	}

	public static final class DecisionNode extends Node {
		private final Definition definition;

		public DecisionNode(Definition definition) {
			this.definition = definition;
		}

		public Definition getDefinition() {
			return definition;
		}

		public <R, P> R accept(NodeVisitor<R, P> visitor, P parameter) {
			return visitor.visitDecision(this, parameter);
		}

		@Override
		public String toString() {
			return "DecisionNode("+definition+")";
		}
		
		public void generateCode(PrintWriter writer, String module, int indent) {
			String call = module + ".super." + definition.getMethodName() + "(";
			int i = 0;
			for (TypeMirror par : definition.getParamTypes()) {
				if (i > 0) {
					call += ", ";
				}
				call += "(" + par.toString() + ") p" + i++;
			}
			call += ")";
			if (definition.getReturnType().getKind() != TypeKind.VOID) {
				writer.println(indent(indent) + "return " + call + ";");				
			} else {
				writer.println(indent(indent) + call + ";");
				writer.println(indent(indent) + "return;");
			}
		}
	}

	public static final class AmbiguityNode extends Node {
		private final List<Definition> definitions;

		public AmbiguityNode(List<Definition> definitions) {
			this.definitions = definitions;
		}

		public List<Definition> getDefinitions() {
			return definitions;
		}

		public <R, P> R accept(NodeVisitor<R, P> visitor, P parameter) {
			return visitor.visitAmbiguity(this, parameter);
		}

		@Override
		public String toString() {
			return "AmbiguityNode("+definitions+")";
		}
		
		public void generateCode(PrintWriter writer, String module, int indent) {
			writer.println(indent(indent) + "throw new se.lth.cs.sovel.AmbiguityException();");
		}

	}

	public static final class ConditionNode extends Node {
		private final Condition condition;
		private final Node isTrue;
		private final Node isFalse;

		public ConditionNode(Condition condition, Node isTrue, Node isFalse) {
			this.condition = condition;
			this.isTrue = isTrue;
			this.isFalse = isFalse;
		}

		public Condition getCondition() {
			return condition;
		}

		public Node getIsTrue() {
			return isTrue;
		}

		public Node getIsFalse() {
			return isFalse;
		}

		public <R, P> R accept(NodeVisitor<R, P> visitor, P parameter) {
			return visitor.visitCondition(this, parameter);
		}

		@Override
		public String toString() {
			return "ConditionNode("+condition+", "+isTrue+", "+isFalse+")";
		}
		
		public void generateCode(PrintWriter writer, String module, int indent) {
			writer.println(indent(indent) + "if (p" + condition.getArgument() + " instanceof " + condition.getType() + ") {");
			isTrue.generateCode(writer, module, indent+1);
			writer.println(indent(indent) + "} else {");
			isFalse.generateCode(writer, module, indent+1);
			writer.println(indent(indent) + "}");
		}

	}
}
