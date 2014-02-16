package com.cep.messaging.util;

/**
 * Methods for manipulating arrays.
 *
 * @lucene.internal
 */

final class ArrayUtil {
  public static long[] grow(long[] array, int minSize) {
    if (array.length < minSize) {
      long[] newArray = new long[Math.max(array.length << 1, minSize)];
      System.arraycopy(array, 0, newArray, 0, array.length);
      return newArray;
    } else
      return array;
  }

  public static long[] grow(long[] array) {
    return grow(array, 1 + array.length);
  }
}
