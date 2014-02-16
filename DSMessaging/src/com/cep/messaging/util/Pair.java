package com.cep.messaging.util;

import com.google.common.base.Objects;

public class Pair<T1, T2> {
	public final T1 left;
	public final T2 right;

	public Pair(T1 left, T2 right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public final int hashCode() {
		int hashCode = 31 + (left == null ? 0 : left.hashCode());
		return 31 * hashCode + (right == null ? 0 : right.hashCode());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof Pair)) {
			return false;
		}
		Pair that = (Pair) o;
		// handles nulls properly
		return Objects.equal(left, that.left) && Objects.equal(right, that.right);
	}

	@Override
	public String toString() {
		return "(" + left + "," + right + ")";
	}

	public static <X, Y> Pair<X, Y> create(X x, Y y) {
		return new Pair<X, Y>(x, y);
	}
}
