package data;

import se.lth.cs.sovel.Module;

@Module
public interface SameReturnType {
	public default String test(Object o) {
		return "Object";
	}
	public default String test(Integer o) {
		return "Integer";
	}
}