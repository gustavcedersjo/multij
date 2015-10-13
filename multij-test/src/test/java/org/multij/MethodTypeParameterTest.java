package org.multij;

import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class MethodTypeParameterTest {
	@Test
	public void testMethodParameterBounds() {
		assert_().about(javaSource())
				.that(forResource("data/MethodTypeParameterBounds.java"))
				.processedWith(new ModuleProcessor())
				.compilesWithoutError();
	}
}
