package data;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;

@Module
public interface ReferenceToNonModule {
	@Binding(BindingKind.MODULE)
	String nonModule();
}