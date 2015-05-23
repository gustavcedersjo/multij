package se.lth.cs.multij;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CacheTest {
	@Module
	interface ObjectRef {
		@Binding
		default Object obj() {
			return new Object();
		}
	}

	@Test
	public void testCachedReference() {
		ObjectRef a = MultiJ.instance(ObjectRef.class);
		Object x = a.obj();
		Object y = a.obj();
		assertEquals("Was not cached", x, y);
	}

	@Module
	interface Primitive {
		@Binding
		default AtomicInteger intBox() {
			return new AtomicInteger();
		}

		@Binding
		default int intValA() {
			return intBox().get();
		}

		@Binding
		default int intValB() {
			return intBox().get();
		}
	}

	@Test
	public void testCachedPrimitive() {
		Primitive b = MultiJ.instance(Primitive.class);
		b.intBox().set(5);
		assertEquals("Wrong value", 5, b.intValA());
		b.intBox().set(6);
		assertEquals("Wrong value", 6, b.intValB());
		assertEquals("Wrong value", 5, b.intValA());
		assertEquals("Wrong value", 6, b.intValB());
	}

	@Module
	interface Circularity {
		@Binding
		default Object a() {
			return b();
		}

		@Binding
		default Object b() {
			return a();
		}
	}


	@Test(expected = CircularityException.class)
	public void testCircularityException() {
		MultiJ.instance(Circularity.class).a();
	}

	public static class Ex extends RuntimeException {}

	@Module
	interface InitException {
		@Binding
		default Object a() {
			throw new Ex();
		}
		@Binding
		default Object b() {
			return a();
		}
	}

	@Test
	public void testInitException() {
		InitException m = MultiJ.instance(InitException.class);
		try {
			m.a();
			fail("Expected exception");
		} catch (Ex e) {
		}
		try {
			m.a();
			fail("Expected exception again");
		} catch (Ex e) {
		}
	}

	@Test
	public void testIndirectInitException() {
		InitException m = MultiJ.instance(InitException.class);
		try {
			m.b();
			fail("Expected exception");
		} catch (Ex e) {
		}
		try {
			m.b();
			fail("Expected exception again");
		} catch (Ex e) {
		}
	}
}
