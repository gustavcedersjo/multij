package se.lth.cs.sovel.model;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

public class EntryPoint {
	private final ExecutableElement entryPoint;
	private final Node root;

	public EntryPoint(ExecutableElement entryPoint, Node root) {
		this.entryPoint = entryPoint;
		this.root = root;
	}

	public Node getDecisionTree() {
		return root;
	}

	@Override
	public String toString() {
		return "EntryPoint(" + root + ")";
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
		private final ExecutableElement definition;

		public DecisionNode(ExecutableElement definition) {
			this.definition = definition;
		}

		public ExecutableElement getDefinition() {
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
		private final List<ExecutableElement> definitions;

		public AmbiguityNode(List<ExecutableElement> definitions) {
			this.definitions = definitions;
		}

		public List<ExecutableElement> getDefinitions() {
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
