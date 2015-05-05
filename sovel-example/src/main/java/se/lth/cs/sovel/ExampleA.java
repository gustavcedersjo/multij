package se.lth.cs.sovel;

@Module
public interface ExampleA {
	public default int test(String s) {
		return 0;
	}
	public default int test(Object o) {
		return 1;
	}
}
