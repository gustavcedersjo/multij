package data;

import se.lth.cs.multij.Binding;
import se.lth.cs.multij.BindingKind;
import se.lth.cs.multij.Module;

@Module
public interface ReferenceToNonModule {
	@Binding(BindingKind.MODULE)
	String nonModule();
}