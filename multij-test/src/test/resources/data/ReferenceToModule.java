package data;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;

@Module
public interface ReferenceToModule {
	@Binding(BindingKind.MODULE)
	ReferenceToModule module();
}