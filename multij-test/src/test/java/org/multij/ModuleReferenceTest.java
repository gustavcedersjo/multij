package org.multij;

import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ModuleReferenceTest {
	@Test
	public void referenceToNonModule() {
		assert_().about(javaSource())
				.that(forResource("data/ReferenceToNonModule.java"))
				.processedWith(new ModuleProcessor())
				.failsToCompile()
				.withErrorContaining("Module reference must refer to a type that is declared as a @Module.");
	}
	@Test
	public void referenceToModule() {
		assert_().about(javaSource())
				.that(forResource("data/ReferenceToModule.java"))
				.processedWith(new ModuleProcessor())
				.compilesWithoutError();
	}

}
