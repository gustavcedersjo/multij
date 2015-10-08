package data;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;

@Module
public interface VoidLazyBinding {
	@Binding(BindingKind.LAZY)
	default void binding() {}
}