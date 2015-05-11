package se.lth.cs.sovel;

import org.junit.Test;

public class MissingDefinitionTest {

	@Module
	interface SingleMissingDefinition {
		public void method();
	}

	@Test(expected = MissingDefinitionException.class)
	public void singleMissingDefinition() {
		Sovel.instance(SingleMissingDefinition.class).method();
	}

	@Module
	interface MissingDefinitionForSupertype {
		public void method(Object o);

		public default void method(String s) {
		}
	}

	@Test(expected = MissingDefinitionException.class)
	public void missingDefinitionForSupertype() {
		Sovel.instance(MissingDefinitionForSupertype.class).method(Integer.valueOf(3));
	}
}
