package data;

import se.lth.cs.multij.Module;

@Module
public interface ReferenceToNonModule {
	@Module
	String nonModule();
}