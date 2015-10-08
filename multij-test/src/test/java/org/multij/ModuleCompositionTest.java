package org.multij;

import org.junit.Test;

import static org.junit.Assert.*;

public class ModuleCompositionTest {
	@Module
	public interface A {
		default int m(Object o) {
			return 1;
		}
		default int m(String s) {
			return 2;
		}
	}
	
	@Module
	public interface B extends A {
		default int m(String s) {
			return 3;
		}
	}
	
	@Test
	public void testOverride() {
		B b = MultiJ.instance(B.class);
		assertEquals("Does not select overridden method", 3, b.m((Object) "test"));
	}

	@Test
	public void testInherit() {
		B b = MultiJ.instance(B.class);
		assertEquals("Does not select inherited method", 1, b.m((Object) Integer.valueOf(4)));
	}

}
