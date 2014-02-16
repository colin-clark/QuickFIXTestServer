package com.cep.messaging.util;

import java.io.DataOutputStream;

/**
 * An implementation of the DataOutputStream interface. This class is completely
 * thread unsafe.
 */
public final class DataOutputBuffer extends DataOutputStream {
	public DataOutputBuffer() {
		this(128);
	}

	public DataOutputBuffer(int size) {
		super(new OutputBuffer(size));
	}

	private OutputBuffer buffer() {
		return (OutputBuffer) out;
	}

	/**
	 * @return The valid contents of the buffer, possibly by copying: only safe
	 *         for one-time-use buffers.
	 */
	public byte[] asByteArray() {
		return buffer().asByteArray();
	}

	/**
	 * Returns the current contents of the buffer. Data is only valid to
	 * {@link #getLength()}.
	 */
	public byte[] getData() {
		return buffer().getData();
	}

	/** Returns the length of the valid data currently in the buffer. */
	public int getLength() {
		return buffer().getLength();
	}

	/** Resets the buffer to empty. */
	public DataOutputBuffer reset() {
		this.written = 0;
		buffer().reset();
		return this;
	}
}
