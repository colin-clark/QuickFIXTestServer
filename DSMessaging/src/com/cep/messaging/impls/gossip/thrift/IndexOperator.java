package com.cep.messaging.impls.gossip.thrift;

public enum IndexOperator implements org.apache.thrift.TEnum {
  EQ(0),
  GTE(1),
  GT(2),
  LTE(3),
  LT(4);

  private final int value;

  private IndexOperator(int value) {
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
  public static IndexOperator findByValue(int value) { 
    switch (value) {
      case 0:
        return EQ;
      case 1:
        return GTE;
      case 2:
        return GT;
      case 3:
        return LTE;
      case 4:
        return LT;
      default:
        return null;
    }
  }
}
