package se.lth.cs.multij;

import org.junit.Test;

public class AmbiguityTest {
	interface A {
	}

	interface B extends A {
	}

	interface C extends A {
	}

	class X implements B, C {
	}

	@Module
	interface InterfaceAmbiguity {
		public default void method(A a) {
		}

		public default void method(B b) {
		}

		public default void method(C b) {
		}
	}

	@Test(expected = AmbiguityException.class)
	public void interfaceAmbiguity() {
		InterfaceAmbiguity inst = MultiJ.instance(InterfaceAmbiguity.class);
		A a = new X();
		inst.method(a);
	}

	@Module
	interface MostSpecificMethod {
		public default void method(Object a, Object b) {
		}

		public default void method(String a, Object b) {
		}

		public default void method(Object a, String b) {
		}
	}

	@Test(expected = AmbiguityException.class)
	public void noMethodIsMostSpecific() {
		MostSpecificMethod inst = MultiJ.instance(MostSpecificMethod.class);
		Object a = "a";
		Object b = "b";
		inst.method(a, b);
	}

}
