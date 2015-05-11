package se.lth.cs.sovel.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import se.lth.cs.sovel.model.DecisionTree.AmbiguityNode;
import se.lth.cs.sovel.model.DecisionTree.ConditionNode;
import se.lth.cs.sovel.model.DecisionTree.DecisionNode;
import se.lth.cs.sovel.model.analysis.Analysis;
import se.lth.cs.sovel.model.analysis.MatchingPrimitiveTypes;
import se.lth.cs.sovel.model.analysis.MethodArity;
import se.lth.cs.sovel.model.analysis.ObjectMethodNames;

public class DecisionTreeGenerator {
	private final List<Definition> definitions;
	private final Universe universe;
	private final Comparator<Condition> conditionComparator;
	private final Types util;

	private DecisionTreeGenerator(List<Definition> definitions, Universe universe, Types util) {
		SubtypeTotalOrder subtypeComp = new SubtypeTotalOrder(util);
		conditionComparator = Comparator
				.comparingInt(Condition::getArgument)
				.thenComparing(Condition::getType, subtypeComp);
		this.definitions = definitions;
		this.universe = universe;
		this.util = util;
	}

	public static Builder builder(ProcessingEnvironment procEnv) {
		List<Analysis> analyses = new ArrayList<>();
		analyses.add(new MethodArity(procEnv));
		analyses.add(new MatchingPrimitiveTypes(procEnv));
		analyses.add(new ObjectMethodNames(procEnv));
		return new Builder(analyses, procEnv.getTypeUtils());
	}

	public static class Builder {
		private final List<Definition> definitions;
		private final List<Analysis> analyses;
		private final Types util;
		private final Universe.Builder universe;

		private Builder(List<Analysis> analyses, Types util) {
			definitions = new ArrayList<>();
			this.analyses = analyses;
			this.util = util;
			universe = Universe.builder(util);
		}

		public void add(ExecutableElement element) {
			Definition def = new Definition(element);
			definitions.add(def);
			def.getConditions().stream()
					.forEach(c -> universe.add(c.getType()));
		}

		public DecisionTreeGenerator build() {
			if (analyses.stream().allMatch(a -> a.check(definitions))) {
				return new DecisionTreeGenerator(definitions, universe.build(), util);
			} else {
				return null;
			}
		}

	}

	public DecisionTree build(ExecutableElement entry) {
		Map<Condition, Boolean> knowledge = Collections.emptyMap();
		if (entry != null) {
			for (Condition cond : new Definition(entry).getConditions()) {
				knowledge = addKnowledge(knowledge, cond, true);
			}
		}
		return new DecisionTree(buildNode(knowledge));
	}

	private Map<Condition, Boolean> addKnowledge(Map<Condition, Boolean> knowledge, Condition cond, boolean truth) {
		Map<Condition, Boolean> result = new HashMap<>(knowledge);
		result.put(cond, truth);
		if (truth) {
			for (TypeMirror t : universe.ifThen(cond.getType())) {
				Condition c = new Condition(cond.getArgument(), t);
				result.put(c, true);
			}
			for (TypeMirror t : universe.ifThenNot(cond.getType())) {
				Condition c = new Condition(cond.getArgument(), t);
				result.put(c, false);
			}
		} else {
			for (TypeMirror t : universe.ifNotThenNot(cond.getType())) {
				Condition c = new Condition(cond.getArgument(), t);
				result.put(c, false);
			}
		}
		return result;
	}

	private DecisionTree.Node buildNode(Map<Condition, Boolean> knowledge) {
		List<Definition> unknown = unknown(knowledge);
		if (unknown.isEmpty()) {
			List<Definition> selectable = selectable(knowledge);
			if (selectable.size() == 1) {
				return new DecisionNode(selectable.get(0));
			} else {
				List<Definition> mostSpecific = selectMostSpecific(selectable);
				if (mostSpecific.size() == 1) {
					return new DecisionNode(mostSpecific.get(0));
				} else {
					return new AmbiguityNode(selectable);
				}
			}
		} else {
			Optional<Condition> test = unknown.stream()
					.flatMap(def -> def.getConditions().stream()
							.filter(cond -> !knowledge.containsKey(cond)))
					.sorted(conditionComparator)
					.findFirst();
			if (test.isPresent()) {
				return new ConditionNode(
						test.get(),
						buildNode(addKnowledge(knowledge, test.get(), true)),
						buildNode(addKnowledge(knowledge, test.get(), false)));
			} else {
				return null;
			}
		}
	}

	private List<Definition> selectMostSpecific(List<Definition> definitions) {
		List<Definition> mostSpecific = new ArrayList<>();
		for (Definition def : definitions) {
			boolean moreSpecific = true;
			defs: for (Definition other : definitions) {
				for (int i = 0; i < def.getParamTypes().size(); i++) {
					if (!util.isSubtype(def.getParamTypes().get(i), other.getParamTypes().get(i))) {
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

	private List<Definition> unknown(Map<Condition, Boolean> knowledge) {
		return definitions.stream()
				.filter(def -> def.isUnknown(knowledge))
				.collect(Collectors.toList());
	}

	private List<Definition> selectable(Map<Condition, Boolean> knowledge) {
		return definitions.stream()
				.filter(def -> def.isSelectable(knowledge))
				.collect(Collectors.toList());
	}
}