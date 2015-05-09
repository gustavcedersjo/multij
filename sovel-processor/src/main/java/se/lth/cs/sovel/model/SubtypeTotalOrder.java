package se.lth.cs.sovel.model;

import java.util.Comparator;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class SubtypeTotalOrder implements Comparator<TypeMirror> {
	private final Types util;

	public SubtypeTotalOrder(Types util) {
		this.util = util;
	}

	public int compare(TypeMirror a, TypeMirror b) {
		if (util.isSameType(a, b)) {
			return 0;
		} else if (util.isSubtype(a, b)) {
			return 1;
		} else if (util.isSubtype(b, a)) {
			return -1;
		} else {
			return a.toString().compareTo(b.toString());
		}
	}
}