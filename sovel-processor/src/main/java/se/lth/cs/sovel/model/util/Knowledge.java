package se.lth.cs.sovel.model.util;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.type.TypeMirror;

import se.lth.cs.sovel.model.Condition;

public class Knowledge {
	private final Universe universe;
	private final Map<Condition, Boolean> knowledge;

	private Knowledge(Universe universe, Map<Condition, Boolean> knowledge) {
		this.universe = universe;
		this.knowledge = knowledge;
	}

	public static Builder builder(Universe universe) {
		return new Builder(universe, new HashMap<>());
	}

	public boolean isTrue(Condition c) {
		return knowledge.getOrDefault(c, false);
	}

	public boolean isFalse(Condition c) {
		return !knowledge.getOrDefault(c, true);
	}

	public boolean isKnown(Condition c) {
		return knowledge.containsKey(c);
	}

	public Builder copy() {
		return new Builder(universe, new HashMap<>(knowledge));
	}

	public static class Builder {
		private Map<Condition, Boolean> knowledge;
		private final Universe universe;

		private Builder(Universe universe, Map<Condition, Boolean> knowledge) {
			this.knowledge = knowledge;
			this.universe = universe;
		}

		public Builder add(Condition cond, boolean truth) {
			if (knowledge == null) {
				throw new IllegalStateException("Knowledge already built.");
			}
			knowledge.put(cond, truth);
			if (truth) {
				for (TypeMirror t : universe.ifThen(cond.getType())) {
					Condition c = new Condition(cond.getArgument(), t);
					knowledge.put(c, true);
				}
				for (TypeMirror t : universe.ifThenNot(cond.getType())) {
					Condition c = new Condition(cond.getArgument(), t);
					knowledge.put(c, false);
				}
			} else {
				for (TypeMirror t : universe.ifNotThenNot(cond.getType())) {
					Condition c = new Condition(cond.getArgument(), t);
					knowledge.put(c, false);
				}
			}
			return this;
		}

		public Knowledge build() {
			Knowledge result = new Knowledge(universe, knowledge);
			knowledge = null;
			return result;
		}
	}
}
