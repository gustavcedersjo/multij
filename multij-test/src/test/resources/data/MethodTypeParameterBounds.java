import org.multij.Module;

@Module
public interface MethodTypeParameterBounds {
	interface A {}
	interface B<T> {}

	default <T extends A & B<A>> T method(T t) {
		return t;
	}
}