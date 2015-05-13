package se.lth.cs.multij.model;

import javax.lang.model.element.ExecutableElement;

public class EntryPoint {
	private final ExecutableElement entryPoint;
	private final DecisionTree root;

	public EntryPoint(ExecutableElement entryPoint, DecisionTree root) {
		this.entryPoint = entryPoint;
		this.root = root;
	}

	public DecisionTree getDecisionTree() {
		return root;
	}

	public ExecutableElement getEntryPoint() {
		return entryPoint;
	}

	@Override
	public String toString() {
		return "EntryPoint(" + root + ")";
	}
}
