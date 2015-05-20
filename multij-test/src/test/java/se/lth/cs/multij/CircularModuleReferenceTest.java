package se.lth.cs.multij;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CircularModuleReferenceTest {
	@Module
	public interface A {
		@Module B b();
		default int test(Object o) {
			return 1;
		}
		default int test(String o) {
			return 2;
		}
	}

	@Module
	public interface B {
		@Module A a();
		default int test(Object o) {
			return 3;
		}
		default int test(String o) {
			return 4;
		}
	}

	@Test
	public void testCircularModuleRefereces() {
		A a = MultiJ.instance(A.class);
		Object str = "str";
		Object obj = 5;
		assertEquals("Wrong method selected", 1, a.test(obj));
		assertEquals("Wrong method selected", 2, a.test(str));
		assertEquals("Wrong method selected", 3, a.b().test(obj));
		assertEquals("Wrong method selected", 4, a.b().test(str));
		assertEquals("Wrong method selected", 1, a.b().a().test(obj));
		assertEquals("Wrong method selected", 2, a.b().a().test(str));
	}
}
