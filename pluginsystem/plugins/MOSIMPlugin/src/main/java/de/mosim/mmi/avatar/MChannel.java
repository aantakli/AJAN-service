/**
 * Autogenerated by Thrift Compiler (0.12.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.avatar;


@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.12.0)", date = "2021-06-29")
public enum MChannel implements org.apache.thrift.TEnum {
  XOffset(0),
  YOffset(1),
  ZOffset(2),
  XRotation(3),
  YRotation(4),
  ZRotation(5),
  WRotation(6);

  private final int value;

  private MChannel(int value) {
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
  @org.apache.thrift.annotation.Nullable
  public static MChannel findByValue(int value) { 
    switch (value) {
      case 0:
        return XOffset;
      case 1:
        return YOffset;
      case 2:
        return ZOffset;
      case 3:
        return XRotation;
      case 4:
        return YRotation;
      case 5:
        return ZRotation;
      case 6:
        return WRotation;
      default:
        return null;
    }
  }
}
