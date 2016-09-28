package data;

import org.multij.Module;

@Module
public interface CovariantReturnType {
	public default Object test(Object o) {
		return "Object";
	}
	public default String test(Integer o) {
		return "Integer";
	}
}
