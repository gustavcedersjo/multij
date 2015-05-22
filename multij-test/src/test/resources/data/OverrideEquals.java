package data;

import se.lth.cs.multij.Module;

@Module
public interface OverrideEquals {
	default boolean equals(String s) {
		return false;
	}
}