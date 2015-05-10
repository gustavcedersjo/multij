package se.lth.cs.sovel.model;

import java.util.List;

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
		return "DecisionTree(" + root + ")";
	}

	public static abstract class Node {
		private Node() {
		}

		public abstract void accept(NodeVisitor visitor);
	}

	public static interface NodeVisitor {
		public void visitDecision(DecisionNode node);

		public void visitAmbiguity(AmbiguityNode node);

		public void visitCondition(ConditionNode node);
	}

	public static final class DecisionNode extends Node {
		private final Definition definition;

		public DecisionNode(Definition definition) {
			this.definition = definition;
		}

		public Definition getDefinition() {
			return definition;
		}

		public void accept(NodeVisitor visitor) {
			visitor.visitDecision(this);
		}

		@Override
		public String toString() {
			return "DecisionNode(" + definition + ")";
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

		public void accept(NodeVisitor visitor) {
			visitor.visitAmbiguity(this);
		}

		@Override
		public String toString() {
			return "AmbiguityNode(" + definitions + ")";
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

		public void accept(NodeVisitor visitor) {
			visitor.visitCondition(this);
		}

		@Override
		public String toString() {
			return "ConditionNode(" + condition + ", " + isTrue + ", " + isFalse + ")";
		}
	}
}
