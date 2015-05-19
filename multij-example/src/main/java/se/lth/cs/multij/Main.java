package se.lth.cs.multij;

public class Main {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		ExampleA ex = MultiJ.instance(ExampleA.class);
		Object hej = "hej";
		System.out.println(ex.test(hej));
	}
}
