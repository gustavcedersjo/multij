package se.lth.cs.multij;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InnerModulesTest {
	interface M {
		int test();
	}
	public interface A {
		@Module
		public interface X extends M {
			default int test() { return 1; }
		}
	}
	public interface B {
		@Module
		public interface X extends M {
			default int test() { return 2; }
		}
	}

	@Test
	public void testInnerModule() {
		M a = MultiJ.instance(A.X.class);
		assertTrue("Wrong type", a instanceof A.X);
		assertEquals("Wrong method selected", a.test(), 1);
		M b = MultiJ.instance(B.X.class);
		assertTrue("Wrong type", b instanceof B.X);
		assertEquals("Wrong method selected", b.test(), 2);
	}
}
