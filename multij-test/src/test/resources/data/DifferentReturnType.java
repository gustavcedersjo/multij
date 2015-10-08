package data;

import org.multij.Module;

@Module
public interface DifferentReturnType {
	public default String test(Object o) {
		return "asdf";
	}
	public default Double test(Integer o) {
		return 5.3;
	}
}
