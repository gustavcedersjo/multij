package data;

import se.lth.cs.multij.Module;

@Module
public interface ReferenceToModule {
	@Module
	ReferenceToModule module();
}