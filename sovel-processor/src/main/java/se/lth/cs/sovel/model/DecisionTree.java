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

	public static abstract class Node {
		private Node() {
		}

		public abstract <R, P> R accept(NodeVisitor<R, P> visitor, P parameter);
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

	}

}
