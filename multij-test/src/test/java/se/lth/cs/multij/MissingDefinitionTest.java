package se.lth.cs.multij;

import org.junit.Test;

public class MissingDefinitionTest {

	@Module
	interface SingleMissingDefinition {
		public void method();
	}

	@Test(expected = MissingDefinitionException.class)
	public void singleMissingDefinition() {
		MultiJ.instance(SingleMissingDefinition.class).method();
	}

	@Module
	interface MissingDefinitionForSupertype {
		public void method(Object o);

		public default void method(String s) {
		}
	}

	@Test(expected = MissingDefinitionException.class)
	public void missingDefinitionForSupertype() {
		MultiJ.instance(MissingDefinitionForSupertype.class).method(Integer.valueOf(3));
	}
}
