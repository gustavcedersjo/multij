package data;

import se.lth.cs.multij.Binding;
import se.lth.cs.multij.BindingKind;
import se.lth.cs.multij.Module;

@Module
public interface VoidLazyBinding {
	@Binding(BindingKind.LAZY)
	default void binding() {}
}