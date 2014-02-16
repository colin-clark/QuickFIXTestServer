package com.cep.messaging.impls.gossip.partitioning.bounds;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.cep.messaging.impls.gossip.partitioning.IPartitioner;
import com.cep.messaging.impls.gossip.partitioning.token.Token;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer2;
import com.cep.messaging.util.Pair;

public abstract class AbstractBounds implements Serializable {
	private static final long serialVersionUID = 1L;
	private static AbstractBoundsSerializer serializer = new AbstractBoundsSerializer();

	public static ICompactSerializer2<AbstractBounds> serializer() {
		return serializer;
	}

	private enum Type {
		RANGE, BOUNDS
	}

	@SuppressWarnings("rawtypes")
	public final Token left;
	@SuppressWarnings("rawtypes")
	public final Token right;

	@SuppressWarnings("rawtypes")
	protected transient final IPartitioner partitioner;

	@SuppressWarnings("rawtypes")
	public AbstractBounds(Token left, Token right, IPartitioner partitioner) {
		this.left = left;
		this.right = right;
		this.partitioner = partitioner;
	}

	/**
	 * Given token T and AbstractBounds ?L,R], returns Pair(?L,T], ?T,R]) (where
	 * ? means that the same type of Bounds is returned -- Range or Bounds -- as
	 * the original.) The original AbstractBounds must contain the token T. If
	 * the split would cause one of the left or right side to be empty, it will
	 * be null in the result pair.
	 */
	@SuppressWarnings("rawtypes")
	public Pair<AbstractBounds, AbstractBounds> split(Token token) {
		assert left.equals(token) || contains(token);
		AbstractBounds lb = createFrom(token);
		// we contain this token, so only one of the left or right can be empty
		AbstractBounds rb = lb != null && token.equals(right) ? null : new Range(token, right);
		return new Pair<AbstractBounds, AbstractBounds>(lb, rb);
	}

	@Override
	public int hashCode() {
		return 31 * left.hashCode() + right.hashCode();
	}

	public abstract boolean equals(Object obj);

	@SuppressWarnings("rawtypes")
	public abstract boolean contains(Token start);

	/**
	 * @return A clone of this AbstractBounds with a new right Token, or null if
	 *         an identical range would be created.
	 */
	@SuppressWarnings("rawtypes")
	public abstract AbstractBounds createFrom(Token right);

	public abstract List<AbstractBounds> unwrap();

	/**
	 * @return A copy of the given list of non-intersecting bounds with all
	 *         bounds unwrapped, sorted by bound.left. This method does not
	 *         allow overlapping ranges as input.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public static List<AbstractBounds> normalize(Collection<? extends AbstractBounds> bounds) {
		// unwrap all
		List<AbstractBounds> output = new ArrayList<AbstractBounds>();
		AbstractBounds previous = null;
		for (AbstractBounds bound : bounds) {
			List<AbstractBounds> unwrapped = bound.unwrap();
			output.addAll(unwrapped);
			previous = unwrapped.get(unwrapped.size() - 1);
		}

		// sort by left
		Collections.sort(output, new Comparator<AbstractBounds>() {
			public int compare(AbstractBounds b1, AbstractBounds b2) {
				return b1.left.compareTo(b2.left);
			}
		});
		return output;
	}

	private static class AbstractBoundsSerializer implements ICompactSerializer2<AbstractBounds> {
		public void serialize(AbstractBounds range, DataOutput out) throws IOException {
			out.writeInt(range instanceof Range ? Type.RANGE.ordinal() : Type.BOUNDS.ordinal());
			Token.serializer().serialize(range.left, out);
			Token.serializer().serialize(range.right, out);
		}

		public AbstractBounds deserialize(DataInput in) throws IOException {
			if (in.readInt() == Type.RANGE.ordinal())
				return new Range(Token.serializer().deserialize(in), Token.serializer().deserialize(in));
			return new Bounds(Token.serializer().deserialize(in), Token.serializer().deserialize(in));
		}
	}
}
