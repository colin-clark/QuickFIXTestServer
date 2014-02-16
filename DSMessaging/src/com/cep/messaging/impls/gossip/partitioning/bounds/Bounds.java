package com.cep.messaging.impls.gossip.partitioning.bounds;

import java.util.Collections;
import java.util.List;

import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.partitioning.IPartitioner;
import com.cep.messaging.impls.gossip.partitioning.token.Token;

public class Bounds extends AbstractBounds {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	public Bounds(Token left, Token right) {
		this(left, right, StorageService.getPartitioner());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	Bounds(Token left, Token right, IPartitioner partitioner) {
		super(left, right, partitioner);
		// unlike a Range, a Bounds may not wrap
		assert left.compareTo(right) <= 0
				|| right.equals(partitioner.getMinimumToken()) : "[" + left + "," + right + "]";
	}

	@SuppressWarnings("rawtypes")
	public boolean contains(Token token) {
		return Range.contains(left, right, token) || left.equals(token);
	}

	@SuppressWarnings("rawtypes")
	public AbstractBounds createFrom(Token token) {
		return new Bounds(left, token);
	}

	public List<AbstractBounds> unwrap() {
		// Bounds objects never wrap
		return Collections.<AbstractBounds> singletonList(this);
	}

	public boolean equals(Object o) {
		if (!(o instanceof Bounds)) {
			return false;
		}
		Bounds rhs = (Bounds) o;
		return left.equals(rhs.left) && right.equals(rhs.right);
	}

	public String toString() {
		return "[" + left + "," + right + "]";
	}
}
