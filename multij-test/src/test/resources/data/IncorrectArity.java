package data;

import org.multij.Module;

@Module
public interface IncorrectArity {
	void m(Object a, Object b);
	void m(Object a);
}