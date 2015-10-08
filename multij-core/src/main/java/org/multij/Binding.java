package org.multij;

public @interface Binding {
	BindingKind value() default BindingKind.AUTO;
}
