package se.lth.cs.sovel.model.analysis;

import java.util.List;

import se.lth.cs.sovel.model.Definition;

public interface CheckOneAnalysis extends Analysis {
	public default boolean check(List<Definition> current, Definition added) {
		if (current.isEmpty()) {
			return true;
		} else {
			return checkOne(current.get(0), added);
		}
	}
	public boolean checkOne(Definition current, Definition added);
}