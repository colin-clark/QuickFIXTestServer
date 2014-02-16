package com.cep.messaging.impls.gossip.thrift;

public enum IndexType implements org.apache.thrift.TEnum {
  KEYS(0);

  private final int value;

  private IndexType(int value) {
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
  public static IndexType findByValue(int value) { 
    switch (value) {
      case 0:
        return KEYS;
      default:
        return null;
    }
  }
}
