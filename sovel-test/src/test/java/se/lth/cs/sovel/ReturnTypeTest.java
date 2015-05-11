package se.lth.cs.sovel;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

import org.junit.Test;

public class ReturnTypeTest {

	@Test
	public void sameReturnType() {
		assert_().about(javaSource())
				.that(forResource("data/SameReturnType.java"))
				.processedWith(new ModuleProcessor())
				.compilesWithoutError();
	}

	@Test
	public void differentReturnType() {
		assert_().about(javaSource())
				.that(forResource("data/DifferentReturnType.java"))
				.processedWith(new ModuleProcessor())
				.failsToCompile()
				.withErrorContaining("Method is defined with other return type elsewhere");
	}

}
