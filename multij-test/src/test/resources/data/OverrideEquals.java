package data;

import org.multij.Module;

@Module
public interface OverrideEquals {
	default boolean equals(String s) {
		return false;
	}
}