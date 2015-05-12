package se.lth.cs.sovel.model;

import javax.lang.model.type.TypeMirror;

public class Condition {
	private final int argument;
	private final TypeMirror type;

	public Condition(int argument, TypeMirror type) {
		this.argument = argument;
		this.type = type;
	}

	public int getArgument() {
		return argument;
	}

	public TypeMirror getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Condition(" + argument + ", " + type + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + argument;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Condition other = (Condition) obj;
		if (argument != other.argument)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
