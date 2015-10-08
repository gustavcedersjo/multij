package org.multij;

import org.junit.Test;

import static org.junit.Assert.*;

public class SelectionTest {
	public interface Top {
	}

	public interface Left extends Top {
	}

	public interface Right extends Top {
	}

	public interface Bottom extends Left, Right {
	}

	@Module
	public interface Examples {
		public default int withNoArg() {
			return 1;
		}

		public default int singleWithOneArg(String x) {
			return 2;
		}

		public default int classInheritance(Object o) {
			return 3;
		}

		public default int classInheritance(String s) {
			return 4;
		}

		public default int classInheritance(Integer i) {
			return 5;
		}

		public default int interfaceInheritance(Top top) {
			return 6;
		}

		public default int interfaceInheritance(Left left) {
			return 7;
		}

		public default int interfaceInheritance(Right right) {
			return 8;
		}

		public default int interfaceInheritance(Bottom bottom) {
			return 9;
		}
	}

	@Test
	public void selectWithNoArg() {
		assertEquals("Wrong method selected", 1, MultiJ.instance(Examples.class).withNoArg());
	}

	@Test
	public void selectSingleWithOneArg() {
		assertEquals("Wrong method selected", 2, MultiJ.instance(Examples.class).singleWithOneArg("hello"));
	}

	@Test
	public void classInheritance() {
		Examples example = MultiJ.instance(Examples.class);
		Object o = new Object();
		Object s = "hello";
		Object i = Integer.valueOf(42);
		assertEquals("Wrong method selected", 3, example.classInheritance(o));
		assertEquals("Wrong method selected", 4, example.classInheritance(s));
		assertEquals("Wrong method selected", 4, example.classInheritance("hello"));
		assertEquals("Wrong method selected", 5, example.classInheritance(i));
		assertEquals("Wrong method selected", 5, example.classInheritance(Integer.valueOf(42)));
	}

	@Test
	public void interfaceInheritance() {
		Examples example = MultiJ.instance(Examples.class);
		Top top = new Top() {
		};
		Left left = new Left() {
		};
		Right right = new Right() {
		};
		Bottom bottom = new Bottom() {
		};
		assertEquals("Wrong method selected", 6, example.interfaceInheritance(top));
		assertEquals("Wrong method selected", 7, example.interfaceInheritance(left));
		assertEquals("Wrong method selected", 7, example.interfaceInheritance((Top) left));
		assertEquals("Wrong method selected", 8, example.interfaceInheritance(right));
		assertEquals("Wrong method selected", 8, example.interfaceInheritance((Top) right));
		assertEquals("Wrong method selected", 9, example.interfaceInheritance(bottom));
		assertEquals("Wrong method selected", 9, example.interfaceInheritance((Top) bottom));
		assertEquals("Wrong method selected", 9, example.interfaceInheritance((Left) bottom));
		assertEquals("Wrong method selected", 9, example.interfaceInheritance((Right) bottom));
	}

	@Test
	public void nullParameter() {
		Object n = null;
		String s = null;
		Integer i = null;
		Examples example = MultiJ.instance(Examples.class);
		assertEquals("Wrong method selected", 3, example.classInheritance(n));
		assertEquals("Wrong method selected", 4, example.classInheritance(s));
		assertEquals("Wrong method selected", 5, example.classInheritance(i));
	}

}
