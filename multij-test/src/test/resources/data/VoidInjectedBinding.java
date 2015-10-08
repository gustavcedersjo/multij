package data;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;

@Module
public interface VoidInjectedBinding {
	@Binding(BindingKind.INJECTED)
	void binding();
}