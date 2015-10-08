package org.multij;

import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class VoidBindingTest {
	@Test
	public void testVoidLazyBinding() {
		voidBindingFail("data/VoidLazyBinding.java");
	}

	@Test
	public void testVoidInjectedBinding() {
		voidBindingFail("data/VoidInjectedBinding.java");
	}

	private void voidBindingFail(String resourceName) {
		assert_().about(javaSource())
				.that(forResource(resourceName))
				.processedWith(new ModuleProcessor())
				.failsToCompile()
				.withErrorContaining("A binding can not be void.");
	}
}
