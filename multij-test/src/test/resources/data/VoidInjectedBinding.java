package data;

import se.lth.cs.multij.Binding;
import se.lth.cs.multij.BindingKind;
import se.lth.cs.multij.Module;

@Module
public interface VoidInjectedBinding {
	@Binding(BindingKind.INJECTED)
	void binding();
}