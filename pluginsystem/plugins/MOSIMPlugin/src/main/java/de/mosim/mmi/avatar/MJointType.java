/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.avatar;


@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2023-06-29")
public enum MJointType implements org.apache.thrift.TEnum {
  Undefined(0),
  LeftBallTip(1),
  LeftBall(2),
  LeftAnkle(3),
  LeftKnee(4),
  LeftHip(5),
  RightBallTip(6),
  RightBall(7),
  RightAnkle(8),
  RightKnee(9),
  RightHip(10),
  PelvisCentre(11),
  S1L5Joint(12),
  T12L1Joint(13),
  T1T2Joint(14),
  C4C5Joint(15),
  HeadJoint(16),
  HeadTip(17),
  MidEye(18),
  LeftShoulder(19),
  LeftElbow(20),
  LeftWrist(21),
  RightShoulder(22),
  RightElbow(23),
  RightWrist(24),
  LeftThumbMid(25),
  LeftThumbMeta(26),
  LeftThumbCarpal(27),
  LeftThumbTip(28),
  LeftIndexMeta(29),
  LeftIndexProximal(30),
  LeftIndexDistal(31),
  LeftIndexTip(32),
  LeftMiddleMeta(33),
  LeftMiddleProximal(34),
  LeftMiddleDistal(35),
  LeftMiddleTip(36),
  LeftRingMeta(37),
  LeftRingProximal(38),
  LeftRingDistal(39),
  LeftRingTip(40),
  LeftLittleMeta(41),
  LeftLittleProximal(42),
  LeftLittleDistal(43),
  LeftLittleTip(44),
  RightThumbMid(45),
  RightThumbMeta(46),
  RightThumbCarpal(47),
  RightThumbTip(48),
  RightIndexMeta(49),
  RightIndexProximal(50),
  RightIndexDistal(51),
  RightIndexTip(52),
  RightMiddleMeta(53),
  RightMiddleProximal(54),
  RightMiddleDistal(55),
  RightMiddleTip(56),
  RightRingMeta(57),
  RightRingProximal(58),
  RightRingDistal(59),
  RightRingTip(60),
  RightLittleMeta(61),
  RightLittleProximal(62),
  RightLittleDistal(63),
  RightLittleTip(64),
  Root(65);

  private final int value;

  private MJointType(int value) {
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
  public static MJointType findByValue(int value) { 
    switch (value) {
      case 0:
        return Undefined;
      case 1:
        return LeftBallTip;
      case 2:
        return LeftBall;
      case 3:
        return LeftAnkle;
      case 4:
        return LeftKnee;
      case 5:
        return LeftHip;
      case 6:
        return RightBallTip;
      case 7:
        return RightBall;
      case 8:
        return RightAnkle;
      case 9:
        return RightKnee;
      case 10:
        return RightHip;
      case 11:
        return PelvisCentre;
      case 12:
        return S1L5Joint;
      case 13:
        return T12L1Joint;
      case 14:
        return T1T2Joint;
      case 15:
        return C4C5Joint;
      case 16:
        return HeadJoint;
      case 17:
        return HeadTip;
      case 18:
        return MidEye;
      case 19:
        return LeftShoulder;
      case 20:
        return LeftElbow;
      case 21:
        return LeftWrist;
      case 22:
        return RightShoulder;
      case 23:
        return RightElbow;
      case 24:
        return RightWrist;
      case 25:
        return LeftThumbMid;
      case 26:
        return LeftThumbMeta;
      case 27:
        return LeftThumbCarpal;
      case 28:
        return LeftThumbTip;
      case 29:
        return LeftIndexMeta;
      case 30:
        return LeftIndexProximal;
      case 31:
        return LeftIndexDistal;
      case 32:
        return LeftIndexTip;
      case 33:
        return LeftMiddleMeta;
      case 34:
        return LeftMiddleProximal;
      case 35:
        return LeftMiddleDistal;
      case 36:
        return LeftMiddleTip;
      case 37:
        return LeftRingMeta;
      case 38:
        return LeftRingProximal;
      case 39:
        return LeftRingDistal;
      case 40:
        return LeftRingTip;
      case 41:
        return LeftLittleMeta;
      case 42:
        return LeftLittleProximal;
      case 43:
        return LeftLittleDistal;
      case 44:
        return LeftLittleTip;
      case 45:
        return RightThumbMid;
      case 46:
        return RightThumbMeta;
      case 47:
        return RightThumbCarpal;
      case 48:
        return RightThumbTip;
      case 49:
        return RightIndexMeta;
      case 50:
        return RightIndexProximal;
      case 51:
        return RightIndexDistal;
      case 52:
        return RightIndexTip;
      case 53:
        return RightMiddleMeta;
      case 54:
        return RightMiddleProximal;
      case 55:
        return RightMiddleDistal;
      case 56:
        return RightMiddleTip;
      case 57:
        return RightRingMeta;
      case 58:
        return RightRingProximal;
      case 59:
        return RightRingDistal;
      case 60:
        return RightRingTip;
      case 61:
        return RightLittleMeta;
      case 62:
        return RightLittleProximal;
      case 63:
        return RightLittleDistal;
      case 64:
        return RightLittleTip;
      case 65:
        return Root;
      default:
        return null;
    }
  }
}
