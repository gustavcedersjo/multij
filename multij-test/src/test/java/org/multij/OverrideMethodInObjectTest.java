package org.multij;

import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class OverrideMethodInObjectTest {

	@Test
	public void overrideEquals() {
		assert_().about(javaSource())
				.that(forResource("data/OverrideEquals.java"))
				.processedWith(new ModuleProcessor())
				.failsToCompile()
				.withErrorContaining("Can not create multimethods with the methods in java.lang.Object.");
	}
}
