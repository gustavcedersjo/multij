package se.lth.cs.sovel.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
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

	private static Builder builder(ProcessingEnvironment procEnv) {
		List<Analysis> analyses = new ArrayList<>();
		analyses.add(new MethodArity(procEnv));
		analyses.add(new MatchingPrimitiveTypes(procEnv));
		analyses.add(new ReturnTypeAnalysis(procEnv));
		analyses.add(new ObjectMethodNames(procEnv));
		analyses.add(new DispatchOnGenerics(procEnv));
		return new Builder(analyses, procEnv.getTypeUtils());
	}

	private static class Builder {
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
	public List<EntryPoint> getEntryPoints() {
		return definitions.stream().map(this::getEntryPoint).collect(Collectors.toList());
	}
	
	private EntryPoint getEntryPoint(ExecutableElement entry) {
		Knowledge.Builder builder = Knowledge.builder(universe);
		for (Condition cond : getConditions(entry)) {
			builder.add(cond, true);
		}
		return new EntryPoint(entry, buildNode(builder.build()));
	}
	
	public static List<Condition> getConditions(ExecutableElement def) {
		List<Condition> result = new ArrayList<>(def.getParameters().size());
		int i = 0;
		for (VariableElement par : def.getParameters()) {
			result.add(new Condition(i++, par.asType())); 
		}
		return result;
	}

	private DecisionTree buildNode(Knowledge knowledge) {
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
							.filter(cond -> !knowledge.isKnown(cond)))
					.sorted(conditionComparator)
					.findFirst();
			if (test.isPresent()) {
				return new DecisionTree.ConditionNode(
						test.get(),
						buildNode(knowledge.copy().add(test.get(), true).build()),
						buildNode(knowledge.copy().add(test.get(), false).build()));
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

	private List<ExecutableElement> unknown(Knowledge knowledge) {
		return definitions.stream()
				.filter(def -> isUnknown(def, knowledge))
				.collect(Collectors.toList());
	}
	
	private boolean isUnknown(ExecutableElement def, Knowledge knowledge) {
		return getConditions(def).stream()
				.anyMatch(cond -> !knowledge.isKnown(cond));
	}


	private List<ExecutableElement> selectable(Knowledge knowledge) {
		return definitions.stream()
				.filter(def -> isSelectable(def, knowledge))
				.collect(Collectors.toList());
	}
	
	private boolean isSelectable(ExecutableElement def, Knowledge knowledge) {
		return getConditions(def).stream()
				.allMatch(cond -> knowledge.isTrue(cond));

	}

	public static Optional<MultiMethod> fromExecutableElements(List<ExecutableElement> definitions, ProcessingEnvironment processingEnv) {
		MultiMethod.Builder builder = MultiMethod.builder(processingEnv);
		for (ExecutableElement def : definitions) {
			builder.add(def);
		}
		return Optional.ofNullable(builder.build());
	}
}