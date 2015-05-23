package se.lth.cs.multij;

public @interface Binding {
	BindingKind value() default BindingKind.AUTO;
}
