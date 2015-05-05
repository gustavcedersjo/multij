package se.lth.cs.sovel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.lth.cs.sovel.Types.Type;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

public class TestTypes {

	private Types types;

	@Before
	public void setUp() {
		types = new Types();
		types.addInterface("A", Arrays.asList());
		types.addInterface("B1", Arrays.asList("A"));
		types.addInterface("B2", Arrays.asList("A"));
		types.addInterface("C", Arrays.asList("B1", "B2"));
		types.addClass("X", null, Arrays.asList("B2"), true);
		types.addClass("Y", null, Arrays.asList("B2"), false);
		types.addClass("Z", "Y", Arrays.asList("C"), false);
	}

	@After
	public void tearDown() {
		types = null;
	}

	private Set<Type> typesOf(String... names) {
		return Arrays.stream(names).map(types::lookup).collect(Collectors.toSet());
	}

	@Test
	public void interfaceImplies() {
		Set<Type> expected = typesOf("java.lang.Object", "A", "B1", "B2");
		Set<Type> actual = types.lookup("C").implies();
		assertEquals("Wrong types", expected, actual);
	}

	@Test
	public void interfaceNotImpliesNot() {
		Set<Type> expected = typesOf("C", "Z");
		Set<Type> actual = types.lookup("B1").notImpliesNot();
		assertEquals("Wrong types", expected, actual);
	}

	@Test
	public void interfaceImpliesNot() {
		Set<Type> expected = typesOf("X");
		Set<Type> actual = types.lookup("B1").impliesNot();
		assertEquals("Wrong types", expected, actual);
	}

	@Test
	public void finalClassImplies() {
		Set<Type> expected = typesOf("java.lang.Object", "A", "B2");
		Set<Type> actual = types.lookup("X").implies();
		assertEquals("Wrong types", expected, actual);
	}

	@Test
	public void finalClassImpliesNot() {
		Set<Type> expected = typesOf("B1", "C", "Y", "Z");
		Set<Type> actual = types.lookup("X").impliesNot();
		assertEquals("Wrong types", expected, actual);
	}

	@Test
	public void finalClassNotImpliesNot() {
		Set<Type> expected = typesOf();
		Set<Type> actual = types.lookup("X").notImpliesNot();
		assertEquals("Wrong types", expected, actual);
	}

	@Test
	public void nonFinalClassImplies() {
		Set<Type> expected = typesOf("java.lang.Object", "A", "B2");
		Set<Type> actual = types.lookup("Y").implies();
		assertEquals("Wrong types", expected, actual);
	}

	@Test
	public void nonFinalClassImpliesNot() {
		Set<Type> expected = typesOf("X");
		Set<Type> actual = types.lookup("Y").impliesNot();
		assertEquals("Wrong types", expected, actual);
	}

	@Test
	public void nonFinalClassNotImpliesNot() {
		Set<Type> expected = typesOf("Z");
		Set<Type> actual = types.lookup("Y").notImpliesNot();
		assertEquals("Wrong types", expected, actual);
	}

	@Test
	public void totalOrder() {
		List<Type> expected = Stream.of("java.lang.Object", "A", "B1", "B2", "C", "X", "Y", "Z")
				.map(types::lookup)
				.collect(Collectors.toList());
		List<Type> input = new ArrayList<>(expected);
		Collections.shuffle(input);
		Collections.sort(input, types.inheritanceTotalOrder);
		assertEquals("Wrong order", expected, input);
	}

}