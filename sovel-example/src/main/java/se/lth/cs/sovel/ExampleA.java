package se.lth.cs.sovel;

import java.io.Closeable;

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

}
