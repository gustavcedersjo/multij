package se.lth.cs.multij;

public class Main {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		ExampleA ex = instanceOf(ExampleA.class);
		Object hej = "hej";
		System.out.println(ex.test(hej));
	}

	private static ExampleA instanceOf(Class<ExampleA> klass) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return (ExampleA) Class.forName(klass.getCanonicalName() + "MultiJ").newInstance();
	}

}
