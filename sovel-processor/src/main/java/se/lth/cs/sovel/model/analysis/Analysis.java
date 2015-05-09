package se.lth.cs.sovel.model.analysis;

import java.util.List;

import se.lth.cs.sovel.model.Definition;

public interface Analysis {
	public boolean check(List<Definition> current, Definition added);
}