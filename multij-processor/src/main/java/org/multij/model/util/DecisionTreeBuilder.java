package org.multij.model.util;

import org.multij.model.Condition;
import org.multij.model.DecisionTree;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DecisionTreeBuilder {
	private final List<ExecutableElement> definitions;
	private final Types typeUtil;
	private final Universe universe;
	private final Comparator<Condition> conditionComparator;

	public DecisionTreeBuilder(List<ExecutableElement> definitions, Types typeUtil) {
		this.definitions = definitions;
		this.typeUtil = typeUtil;
		this.universe = Universe.of(definitions.stream()
				.flatMap(d -> d.getParameters().stream())
				.map(p -> p.asType())
				.collect(Collectors.toList()), typeUtil);
		conditionComparator = Comparator
				.comparingInt(Condition::getArgument)
				.thenComparing(Condition::getType, new SubtypeTotalOrder(typeUtil));

	}

	public DecisionTree build(ExecutableElement entryPoint) {
		Knowledge.Builder knowledge = Knowledge.builder(universe);
		for (Condition cond : getConditions(entryPoint)) {
			knowledge.add(cond, true);
		}
		return build(knowledge.build());
	}

	private List<Condition> getConditions(ExecutableElement definition) {
		List<Condition> result = new ArrayList<>(definition.getParameters().size());
		int i = 0;
		for (VariableElement par : definition.getParameters()) {
			result.add(new Condition(i++, par.asType()));
		}
		return result;
	}

	private DecisionTree build(Knowledge knowledge) {
		List<ExecutableElement> unknown = unknown(knowledge);
		if (unknown.isEmpty()) {
			List<ExecutableElement> selectable = selectable(knowledge);
			if (selectable.size() == 1) {
				return new DecisionTree.DecisionNode(selectable.get(0));
			} else {
				List<ExecutableElement> mostSpecific = selectMostSpecific(selectable);
				if (mostSpecific.size() == 1) {
					return new DecisionTree.DecisionNode(mostSpecific.get(0));
				} else {
					return new DecisionTree.AmbiguityNode(mostSpecific);
				}
			}
		} else {
			Optional<Condition> test = unknown.stream()
					.flatMap(def -> getConditions(def).stream().filter(cond -> !knowledge.isKnown(cond)))
					.sorted(conditionComparator)
					.findFirst();
			if (test.isPresent()) {
				return new DecisionTree.ConditionNode(test.get(),
						build(knowledge.copy().add(test.get(), true).build()), build(knowledge.copy()
								.add(test.get(), false)
								.build()));
			} else {
				return null;
			}
		}
	}

	private List<ExecutableElement> unknown(Knowledge knowledge) {
		return definitions.stream().filter(def -> isUnknown(def, knowledge)).collect(Collectors.toList());
	}

	private boolean isUnknown(ExecutableElement def, Knowledge knowledge) {
		return getConditions(def).stream().anyMatch(cond -> !knowledge.isKnown(cond));
	}

	private List<ExecutableElement> selectable(Knowledge knowledge) {
		return definitions.stream().filter(def -> isSelectable(def, knowledge)).collect(Collectors.toList());
	}

	private boolean isSelectable(ExecutableElement def, Knowledge knowledge) {
		return getConditions(def).stream().allMatch(cond -> knowledge.isTrue(cond));

	}

	private List<ExecutableElement> selectMostSpecific(List<ExecutableElement> definitions) {
		List<ExecutableElement> mostSpecific = new ArrayList<>();
		for (ExecutableElement def : definitions) {
			boolean moreSpecific = true;
			defs: for (ExecutableElement other : definitions) {
				for (int i = 0; i < def.getParameters().size(); i++) {
					if (!typeUtil.isSubtype(def.getParameters().get(i).asType(), other.getParameters().get(i).asType())) {
						moreSpecific = false;
						break defs;
					}
				}
			}
			if (moreSpecific) {
				mostSpecific.add(def);
			}
		}
		return mostSpecific;
	}

}
