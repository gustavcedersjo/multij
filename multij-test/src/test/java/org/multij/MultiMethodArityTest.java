package org.multij;

import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class MultiMethodArityTest {
	@Test
	public void incorrectArity() {
		assert_().about(javaSource())
				.that(forResource("data/IncorrectArity.java"))
				.processedWith(new ModuleProcessor())
				.failsToCompile()
				.withErrorContaining("Wrong number of parameters");
	}

	@Test
	public void correctArity() {
		assert_().about(javaSource())
				.that(forResource("data/CorrectArity.java"))
				.processedWith(new ModuleProcessor())
				.compilesWithoutError();
	}

}
