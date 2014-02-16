package com.cep.messaging.impls.gossip.util;

import static com.google.common.base.Charsets.UTF_8;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;


/**
 * Utility methods to make ByteBuffers less painful The following should
 * illustrate the different ways byte buffers can be used
 * 
 * public void testArrayOffet() {
 * 
 * byte[] b = "test_slice_array".getBytes(); ByteBuffer bb =
 * ByteBuffer.allocate(1024);
 * 
 * assert bb.position() == 0; assert bb.limit() == 1024; assert bb.capacity() ==
 * 1024;
 * 
 * bb.put(b);
 * 
 * assert bb.position() == b.length; assert bb.remaining() == bb.limit() -
 * bb.position();
 * 
 * ByteBuffer bb2 = bb.slice();
 * 
 * assert bb2.position() == 0;
 * 
 * //slice should begin at other buffers current position assert
 * bb2.arrayOffset() == bb.position();
 * 
 * //to match the position in the underlying array one needs to //track
 * arrayOffset assert bb2.limit()+bb2.arrayOffset() == bb.limit();
 * 
 * 
 * assert bb2.remaining() == bb.remaining();
 * 
 * }
 * 
 * }
 * 
 */
public class ByteBufferUtil {
	public static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.wrap(ArrayUtils.EMPTY_BYTE_ARRAY);

	public static int compareUnsigned(ByteBuffer o1, ByteBuffer o2) {
		assert o1 != null;
		assert o2 != null;

		int minLength = Math.min(o1.remaining(), o2.remaining());
		for (int x = 0, i = o1.position(), j = o2.position(); x < minLength; x++, i++, j++) {
			if (o1.get(i) == o2.get(j)) {
				continue;
			}
			// compare non-equal bytes as unsigned
			return (o1.get(i) & 0xFF) < (o2.get(j) & 0xFF) ? -1 : 1;
		}

		return (o1.remaining() == o2.remaining()) ? 0 : ((o1.remaining() < o2.remaining()) ? -1 : 1);
	}

	public static int compare(byte[] o1, ByteBuffer o2) {
		return compareUnsigned(ByteBuffer.wrap(o1), o2);
	}

	public static int compare(ByteBuffer o1, byte[] o2) {
		return compareUnsigned(o1, ByteBuffer.wrap(o2));
	}

	/**
	 * Decode a String representation. This method assumes that the encoding
	 * charset is UTF_8.
	 * 
	 * @param buffer
	 *            a byte buffer holding the string representation
	 * @return the decoded string
	 */
	public static String string(ByteBuffer buffer) throws CharacterCodingException {
		return string(buffer, UTF_8);
	}

	/**
	 * Decode a String representation. This method assumes that the encoding
	 * charset is UTF_8.
	 * 
	 * @param buffer
	 *            a byte buffer holding the string representation
	 * @param position
	 *            the starting position in {@code buffer} to start decoding from
	 * @param length
	 *            the number of bytes from {@code buffer} to use
	 * @return the decoded string
	 */
	public static String string(ByteBuffer buffer, int position, int length) throws CharacterCodingException {
		return string(buffer, position, length, UTF_8);
	}

	/**
	 * Decode a String representation.
	 * 
	 * @param buffer
	 *            a byte buffer holding the string representation
	 * @param position
	 *            the starting position in {@code buffer} to start decoding from
	 * @param length
	 *            the number of bytes from {@code buffer} to use
	 * @param charset
	 *            the String encoding charset
	 * @return the decoded string
	 */
	public static String string(ByteBuffer buffer, int position, int length, Charset charset) throws CharacterCodingException {
		ByteBuffer copy = buffer.duplicate();
		copy.position(position);
		copy.limit(copy.position() + length);
		return string(copy, charset);
	}

	/**
	 * Decode a String representation.
	 * 
	 * @param buffer
	 *            a byte buffer holding the string representation
	 * @param charset
	 *            the String encoding charset
	 * @return the decoded string
	 */
	public static String string(ByteBuffer buffer, Charset charset) throws CharacterCodingException {
		return charset.newDecoder().decode(buffer.duplicate()).toString();
	}

	/**
	 * You should almost never use this. Instead, use the write* methods to
	 * avoid copies.
	 */
	public static byte[] getArray(ByteBuffer buffer) {
		int length = buffer.remaining();

		if (buffer.hasArray()) {
			int start = buffer.position();
			if (buffer.arrayOffset() == 0 && start == 0 && length == buffer.array().length) {
				return buffer.array();
			} else {
				return Arrays.copyOfRange(buffer.array(), start + buffer.arrayOffset(), start + length + buffer.arrayOffset());
			}
		}
		// else, DirectByteBuffer.get() is the fastest route
		byte[] bytes = new byte[length];
		buffer.duplicate().get(bytes);

		return bytes;
	}

	/**
	 * ByteBuffer adaptation of org.apache.commons.lang.ArrayUtils.lastIndexOf
	 * method
	 * 
	 * @param buffer
	 *            the array to traverse for looking for the object, may be
	 *            <code>null</code>
	 * @param valueToFind
	 *            the value to find
	 * @param startIndex
	 *            the start index (i.e. BB position) to travers backwards from
	 * @return the last index (i.e. BB position) of the value within the array
	 *         [between buffer.position() and buffer.limit()]; <code>-1</code>
	 *         if not found.
	 */
	public static int lastIndexOf(ByteBuffer buffer, byte valueToFind, int startIndex) {
		assert buffer != null;

		if (startIndex < buffer.position()) {
			return -1;
		} else if (startIndex >= buffer.limit()) {
			startIndex = buffer.limit() - 1;
		}

		for (int i = startIndex; i >= buffer.position(); i--) {
			if (valueToFind == buffer.get(i)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Encode a String in a ByteBuffer using UTF_8.
	 * 
	 * @param s
	 *            the string to encode
	 * @return the encoded string
	 */
	public static ByteBuffer bytes(String s) {
		return ByteBuffer.wrap(s.getBytes(UTF_8));
	}

	/**
	 * Encode a String in a ByteBuffer using the provided charset.
	 * 
	 * @param s
	 *            the string to encode
	 * @param charset
	 *            the String encoding charset to use
	 * @return the encoded string
	 */
	public static ByteBuffer bytes(String s, Charset charset) {
		return ByteBuffer.wrap(s.getBytes(charset));
	}

	/**
	 * @return a new copy of the data in @param buffer USUALLY YOU SHOULD USE
	 *         ByteBuffer.duplicate() INSTEAD, which creates a new Buffer (so
	 *         you can mutate its position without affecting the original)
	 *         without copying the underlying array.
	 */
	public static ByteBuffer clone(ByteBuffer buffer) {
		assert buffer != null;

		if (buffer.remaining() == 0) {
			return EMPTY_BYTE_BUFFER;
		}
		ByteBuffer clone = ByteBuffer.allocate(buffer.remaining());

		if (buffer.hasArray()) {
			System.arraycopy(buffer.array(), buffer.arrayOffset() + buffer.position(), clone.array(), 0, buffer.remaining());
		} else {
			clone.put(buffer.duplicate());
			clone.flip();
		}

		return clone;
	}

	public static void arrayCopy(ByteBuffer buffer, int position, byte[] bytes, int offset, int length) {
		if (buffer.hasArray()) {
			System.arraycopy(buffer.array(), buffer.arrayOffset() + position, bytes, offset, length);
		} else {
			((ByteBuffer) buffer.duplicate().position(position)).get(bytes, offset, length);
		}
	}

	/**
	 * Transfer bytes from one ByteBuffer to another. This function acts as
	 * System.arrayCopy() but for ByteBuffers.
	 * 
	 * @param src
	 *            the source ByteBuffer
	 * @param srcPos
	 *            starting position in the source ByteBuffer
	 * @param dst
	 *            the destination ByteBuffer
	 * @param dstPos
	 *            starting position in the destination ByteBuffer
	 * @param length
	 *            the number of bytes to copy
	 */
	public static void arrayCopy(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int length) {
		if (src.hasArray() && dst.hasArray()) {
			System.arraycopy(src.array(), src.arrayOffset() + srcPos, dst.array(), dst.arrayOffset() + dstPos, length);
		} else {
			if (src.limit() - srcPos < length || dst.limit() - dstPos < length) {
				throw new IndexOutOfBoundsException();
			}
			for (int i = 0; i < length; i++) {
				dst.put(dstPos++, src.get(srcPos++));
			}
		}
	}

	public static void writeWithLength(ByteBuffer bytes, DataOutput out) throws IOException {
		out.writeInt(bytes.remaining());
		write(bytes, out); // writing data bytes to output source
	}

	public static void write(ByteBuffer buffer, DataOutput out) throws IOException {
		if (buffer.hasArray()) {
			out.write(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
		} else {
			for (int i = buffer.position(); i < buffer.limit(); i++) {
				out.writeByte(buffer.get(i));
			}
		}
	}

	public static void writeWithShortLength(ByteBuffer buffer, DataOutput out) {
		int length = buffer.remaining();
		assert 0 <= length && length <= GossipUtilities.MAX_UNSIGNED_SHORT : length;
		try {
			out.writeByte((length >> 8) & 0xFF);
			out.writeByte(length & 0xFF);
			write(buffer, out); // writing data bytes to output source
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static ByteBuffer readWithLength(DataInput in) throws IOException {
		int length = in.readInt();
		if (length < 0) {
			throw new IOException("Corrupt (negative) value length encountered");
		}
		return ByteBufferUtil.read(in, length);
	}

	/* @return An unsigned short in an integer. */
	private static int readShortLength(DataInput in) throws IOException {
		int length = (in.readByte() & 0xFF) << 8;
		return length | (in.readByte() & 0xFF);
	}

	/**
	 * @param in
	 *            data input
	 * @return An unsigned short in an integer.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public static ByteBuffer readWithShortLength(DataInput in) throws IOException {
		return ByteBufferUtil.read(in, readShortLength(in));
	}

	private static ByteBuffer read(DataInput in, int length) throws IOException {
		ByteBuffer array;

		byte[] buff = new byte[length];
		in.readFully(buff);
		array = ByteBuffer.wrap(buff);

		return array;
	}

	/**
	 * Convert a byte buffer to an integer. Does not change the byte buffer
	 * position.
	 * 
	 * @param bytes
	 *            byte buffer to convert to integer
	 * @return int representation of the byte buffer
	 */
	public static int toInt(ByteBuffer bytes) {
		return bytes.getInt(bytes.position());
	}

	public static long toLong(ByteBuffer bytes) {
		return bytes.getLong(bytes.position());
	}

	public static float toFloat(ByteBuffer bytes) {
		return bytes.getFloat(bytes.position());
	}

	public static double toDouble(ByteBuffer bytes) {
		return bytes.getDouble(bytes.position());
	}

	public static ByteBuffer bytes(int i) {
		return ByteBuffer.allocate(4).putInt(0, i);
	}

	public static ByteBuffer bytes(long n) {
		return ByteBuffer.allocate(8).putLong(0, n);
	}

	public static ByteBuffer bytes(float f) {
		return ByteBuffer.allocate(4).putFloat(0, f);
	}

	public static ByteBuffer bytes(double d) {
		return ByteBuffer.allocate(8).putDouble(0, d);
	}

	public static InputStream inputStream(ByteBuffer bytes) {
		final ByteBuffer copy = bytes.duplicate();

		return new InputStream() {
			public int read() throws IOException {
				if (!copy.hasRemaining()) {
					return -1;
				}
				return copy.get() & 0xFF;
			}

			@Override
			public int read(byte[] bytes, int off, int len) throws IOException {
				if (!copy.hasRemaining()) {
					return -1;
				}
				len = Math.min(len, copy.remaining());
				copy.get(bytes, off, len);
				return len;
			}

			@Override
			public int available() throws IOException {
				return copy.remaining();
			}
		};
	}

	public static String bytesToHex(ByteBuffer bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = bytes.position(); i < bytes.limit(); i++) {
			int bint = bytes.get(i) & 0xff;
			if (bint <= 0xF) {
				// toHexString does not 0 pad its results.
				sb.append("0");
			}
			sb.append(Integer.toHexString(bint));
		}
		return sb.toString();
	}

	public static ByteBuffer hexToBytes(String str) {
		return ByteBuffer.wrap(GossipUtilities.hexToBytes(str));
	}

	/**
	 * Compare two ByteBuffer at specified offsets for length. Compares the non
	 * equal bytes as unsigned.
	 * 
	 * @param bytes1
	 *            First byte buffer to compare.
	 * @param offset1
	 *            Position to start the comparison at in the first array.
	 * @param bytes2
	 *            Second byte buffer to compare.
	 * @param offset2
	 *            Position to start the comparison at in the second array.
	 * @param length
	 *            How many bytes to compare?
	 * @return -1 if byte1 is less than byte2, 1 if byte2 is less than byte1 or
	 *         0 if equal.
	 */
	public static int compareSubArrays(ByteBuffer bytes1, int offset1, ByteBuffer bytes2, int offset2, int length) {
		if (null == bytes1) {
			if (null == bytes2) {
				return 0;
			} else {
				return -1;
			}
		}
		if (null == bytes2) {
			return 1;
		}
		assert bytes1.limit() >= offset1 + length : "The first byte array isn't long enough for the specified offset and length.";
		assert bytes2.limit() >= offset2 + length : "The second byte array isn't long enough for the specified offset and length.";
		for (int i = 0; i < length; i++) {
			byte byte1 = bytes1.get(offset1 + i);
			byte byte2 = bytes2.get(offset2 + i);
			if (byte1 == byte2) {
				continue;
			}
			// compare non-equal bytes as unsigned
			return (byte1 & 0xFF) < (byte2 & 0xFF) ? -1 : 1;
		}
		return 0;
	}

	public static void skipShortLength(BufferedRandomAccessFile dfile) {
		// TODO Auto-generated method stub
		
	}
}
