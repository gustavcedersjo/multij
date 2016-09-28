package org.multij;

import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ReturnTypeTest {

	@Test
	public void sameReturnType() {
		assert_().about(javaSource())
				.that(forResource("data/SameReturnType.java"))
				.processedWith(new ModuleProcessor())
				.compilesWithoutError();
	}

	@Test
	public void covariantReturnType() {
		assert_().about(javaSource())
				.that(forResource("data/CovariantReturnType.java"))
				.processedWith(new ModuleProcessor())
				.compilesWithoutError();
	}

	@Test
	public void differentReturnType() {
		assert_().about(javaSource())
				.that(forResource("data/DifferentReturnType.java"))
				.processedWith(new ModuleProcessor())
				.failsToCompile()
				.withErrorContaining("Return type java.lang.Double is not a subtype of java.lang.String.");
	}

}
