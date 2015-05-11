package se.lth.cs.sovel.model.analysis;

import java.util.Iterator;
import java.util.List;

import se.lth.cs.sovel.model.Definition;

public interface CheckOneAnalysis extends Analysis {
	public default boolean check(List<Definition> definitions) {
		Iterator<Definition> iter = definitions.iterator();
		if (iter.hasNext()) {
			Definition current = iter.next();
			boolean result = true;
			while (iter.hasNext()) {
				if (!checkOne(current, iter.next())) {
					result = false;
				}
			}
			return result;
		} else {
			return true;
		}
	}
	public boolean checkOne(Definition current, Definition added);
}