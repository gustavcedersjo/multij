package org.multij.model;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

public abstract class DecisionTree {
	public static interface Visitor {
		public void visitDecision(DecisionTree.DecisionNode node);

		public void visitAmbiguity(DecisionTree.AmbiguityNode node);

		public void visitCondition(DecisionTree.ConditionNode node);
	}

	public static final class DecisionNode extends DecisionTree {
		private final ExecutableElement definition;

		public DecisionNode(ExecutableElement definition) {
			this.definition = definition;
		}

		public ExecutableElement getDefinition() {
			return definition;
		}

		public void accept(DecisionTree.Visitor visitor) {
			visitor.visitDecision(this);
		}

		@Override
		public String toString() {
			return "DecisionNode(" + definition + ")";
		}
	}

	public static final class AmbiguityNode extends DecisionTree {
		private final List<ExecutableElement> definitions;

		public AmbiguityNode(List<ExecutableElement> definitions) {
			this.definitions = definitions;
		}

		public List<ExecutableElement> getDefinitions() {
			return definitions;
		}

		public void accept(DecisionTree.Visitor visitor) {
			visitor.visitAmbiguity(this);
		}

		@Override
		public String toString() {
			return "AmbiguityNode(" + definitions + ")";
		}

	}

	public static final class ConditionNode extends DecisionTree {
		private final Condition condition;
		private final DecisionTree isTrue;
		private final DecisionTree isFalse;

		public ConditionNode(Condition condition, DecisionTree isTrue, DecisionTree isFalse) {
			this.condition = condition;
			this.isTrue = isTrue;
			this.isFalse = isFalse;
		}

		public Condition getCondition() {
			return condition;
		}

		public DecisionTree getIsTrue() {
			return isTrue;
		}

		public DecisionTree getIsFalse() {
			return isFalse;
		}

		public void accept(DecisionTree.Visitor visitor) {
			visitor.visitCondition(this);
		}

		@Override
		public String toString() {
			return "ConditionNode(" + condition + ", " + isTrue + ", " + isFalse + ")";
		}
	}

	private DecisionTree() {
	}

	public abstract void accept(DecisionTree.Visitor visitor);
}
