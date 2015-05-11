package se.lth.cs.sovel;

import java.io.Closeable;
import java.util.List;

@Module
public interface ExampleA {
	public default int test(String[] s) {
		return 0;
	}

	public default int test(Object[] s) {
		return 3;
	}

	public default int test(Object o) {
		return 1;
	}

	public default int hello(Object o, int x) {
		return 3;
	}

	public default void ambiguous(Runnable r) {
	}

	public default void ambiguous(Closeable c) {
	}

	public void missingDefinition();

	public default void wildcard(Object o, List<?> list) {
	}

	public default void wildcard(String s, List<?> list) {
	}

	public default void wildcard(Integer i, List<?> List) {
	}
}
