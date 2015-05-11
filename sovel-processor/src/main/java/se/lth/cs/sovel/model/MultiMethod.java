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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import se.lth.cs.sovel.model.analysis.Analysis;
import se.lth.cs.sovel.model.analysis.DispatchOnGenerics;
import se.lth.cs.sovel.model.analysis.MatchingPrimitiveTypes;
import se.lth.cs.sovel.model.analysis.MethodArity;
import se.lth.cs.sovel.model.analysis.ObjectMethodNames;
import se.lth.cs.sovel.model.analysis.ReturnTypeAnalysis;

public class MultiMethod {
	private final List<ExecutableElement> definitions;
	private final Universe universe;
	private final Comparator<Condition> conditionComparator;
	private final Types util;

	private MultiMethod(List<ExecutableElement> definitions, Universe universe, Types util) {
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
		analyses.add(new ReturnTypeAnalysis(procEnv));
		analyses.add(new ObjectMethodNames(procEnv));
		analyses.add(new DispatchOnGenerics(procEnv));
		return new Builder(analyses, procEnv.getTypeUtils());
	}

	public static class Builder {
		private final List<ExecutableElement> definitions;
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
			definitions.add(element);
			getConditions(element).stream()
					.forEach(c -> universe.add(c.getType()));
		}

		public MultiMethod build() {
			boolean okay = true;
			for (Analysis analysis : analyses) {
				if (!analysis.check(definitions)) {
					okay = false;
				}
			}
			if (okay) {
				return new MultiMethod(definitions, universe.build(), util);
			} else {
				return null;
			}
		}

	}
	
	public EntryPoint getEntryPoint(ExecutableElement entry) {
		Map<Condition, Boolean> knowledge = Collections.emptyMap();
		if (entry != null) {
			for (Condition cond : getConditions(entry)) {
				knowledge = addKnowledge(knowledge, cond, true);
			}
		}
		return new EntryPoint(entry, buildNode(knowledge));
	}
	
	public static List<Condition> getConditions(ExecutableElement def) {
		List<Condition> result = new ArrayList<>(def.getParameters().size());
		int i = 0;
		for (VariableElement par : def.getParameters()) {
			result.add(new Condition(i++, par.asType())); 
		}
		return result;
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

	private DecisionTree buildNode(Map<Condition, Boolean> knowledge) {
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
					return new DecisionTree.AmbiguityNode(selectable);
				}
			}
		} else {
			Optional<Condition> test = unknown.stream()
					.flatMap(def -> getConditions(def).stream()
							.filter(cond -> !knowledge.containsKey(cond)))
					.sorted(conditionComparator)
					.findFirst();
			if (test.isPresent()) {
				return new DecisionTree.ConditionNode(
						test.get(),
						buildNode(addKnowledge(knowledge, test.get(), true)),
						buildNode(addKnowledge(knowledge, test.get(), false)));
			} else {
				return null;
			}
		}
	}

	private List<ExecutableElement> selectMostSpecific(List<ExecutableElement> definitions) {
		List<ExecutableElement> mostSpecific = new ArrayList<>();
		for (ExecutableElement def : definitions) {
			boolean moreSpecific = true;
			defs: for (ExecutableElement other : definitions) {
				for (int i = 0; i < def.getParameters().size(); i++) {
					if (!util.isSubtype(def.getParameters().get(i).asType(), other.getParameters().get(i).asType())) {
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

	private List<ExecutableElement> unknown(Map<Condition, Boolean> knowledge) {
		return definitions.stream()
				.filter(def -> isUnknown(def, knowledge))
				.collect(Collectors.toList());
	}
	
	private boolean isUnknown(ExecutableElement def, Map<Condition, Boolean> knowledge) {
		return getConditions(def).stream()
				.anyMatch(cond -> !knowledge.containsKey(cond));
	}


	private List<ExecutableElement> selectable(Map<Condition, Boolean> knowledge) {
		return definitions.stream()
				.filter(def -> isSelectable(def, knowledge))
				.collect(Collectors.toList());
	}
	
	private boolean isSelectable(ExecutableElement def, Map<Condition, Boolean> knowledge) {
		return getConditions(def).stream()
				.allMatch(cond -> knowledge.getOrDefault(cond, false));

	}
}