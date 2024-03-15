package dev.bodewig.jcoprocessor.util;

/**
 * Utilities to cast Numbers to Double, Long or Integer
 */
public final class Numeric {

	private Numeric() {
	}

	/**
	 * Casts a Number to Double
	 *
	 * @param o the input to cast
	 * @return boxed Double
	 */
	public static Double doubleBoxed(Object o) {
		if (!(o instanceof Number)) {
			throw new IllegalArgumentException("Parameter is not a Number");
		}
		if (o instanceof Double) {
			return (Double) o;
		}
		Number n = (Number) o;
		return Double.valueOf(n.doubleValue());
	}

	/**
	 * Casts a Number to double
	 *
	 * @param o the input to cast
	 * @return simple double
	 */
	public static double doubleValue(Object o) {
		if (!(o instanceof Number)) {
			throw new IllegalArgumentException("Parameter is not a Number");
		}
		Number n = (Number) o;
		return n.doubleValue();
	}

	/**
	 * Casts a Number to Long
	 *
	 * @param o the input to cast
	 * @return boxed Long
	 */
	public static Long longBoxed(Object o) {
		if (!(o instanceof Number)) {
			throw new IllegalArgumentException("Parameter is not a Number");
		}
		if (o instanceof Long) {
			return (Long) o;
		}
		Number n = (Number) o;
		return Long.valueOf(n.longValue());
	}

	/**
	 * Casts a Number to long
	 *
	 * @param o the input to cast
	 * @return simple long
	 */
	public static long longValue(Object o) {
		if (!(o instanceof Number)) {
			throw new IllegalArgumentException("Parameter is not a Number");
		}
		Number n = (Number) o;
		return n.longValue();
	}

	/**
	 * Casts a Number to Integer
	 *
	 * @param o the input to cast
	 * @return boxed Integer
	 */
	public static Integer intBoxed(Object o) {
		if (!(o instanceof Number)) {
			throw new IllegalArgumentException("Parameter is not a Number");
		}
		if (o instanceof Integer) {
			return (Integer) o;
		}
		Number n = (Number) o;
		return Integer.valueOf(n.intValue());
	}

	/**
	 * Casts a Number to int
	 *
	 * @param o the input to cast
	 * @return simple int
	 */
	public static int intValue(Object o) {
		if (!(o instanceof Number)) {
			throw new IllegalArgumentException("Parameter is not a Number");
		}
		Number n = (Number) o;
		return n.intValue();
	}
}
