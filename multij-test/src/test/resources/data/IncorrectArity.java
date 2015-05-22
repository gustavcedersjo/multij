package data;

import se.lth.cs.multij.Module;

@Module
public interface IncorrectArity {
	void m(Object a, Object b);
	void m(Object a);
}