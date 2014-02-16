package com.cep.messaging.impls.gossip.thrift;

/**
 * CQL query compression
 */
public enum Compression implements org.apache.thrift.TEnum {
  GZIP(1),
  NONE(2);

  private final int value;

  private Compression(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static Compression findByValue(int value) { 
    switch (value) {
      case 1:
        return GZIP;
      case 2:
        return NONE;
      default:
        return null;
    }
  }
}
