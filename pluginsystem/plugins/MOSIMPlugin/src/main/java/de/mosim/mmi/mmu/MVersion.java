/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.mmu;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2023-06-29")
public class MVersion implements org.apache.thrift.TBase<MVersion, MVersion._Fields>, java.io.Serializable, Cloneable, Comparable<MVersion> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MVersion");

  private static final org.apache.thrift.protocol.TField MAJOR_FIELD_DESC = new org.apache.thrift.protocol.TField("Major", org.apache.thrift.protocol.TType.I16, (short)1);
  private static final org.apache.thrift.protocol.TField MINOR_FIELD_DESC = new org.apache.thrift.protocol.TField("Minor", org.apache.thrift.protocol.TType.I16, (short)2);
  private static final org.apache.thrift.protocol.TField SUB_FIELD_DESC = new org.apache.thrift.protocol.TField("Sub", org.apache.thrift.protocol.TType.I16, (short)3);
  private static final org.apache.thrift.protocol.TField SUBSUB_FIELD_DESC = new org.apache.thrift.protocol.TField("Subsub", org.apache.thrift.protocol.TType.I16, (short)4);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MVersionStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MVersionTupleSchemeFactory();

  public short Major; // required
  public short Minor; // optional
  public short Sub; // optional
  public short Subsub; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    MAJOR((short)1, "Major"),
    MINOR((short)2, "Minor"),
    SUB((short)3, "Sub"),
    SUBSUB((short)4, "Subsub");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // MAJOR
          return MAJOR;
        case 2: // MINOR
          return MINOR;
        case 3: // SUB
          return SUB;
        case 4: // SUBSUB
          return SUBSUB;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __MAJOR_ISSET_ID = 0;
  private static final int __MINOR_ISSET_ID = 1;
  private static final int __SUB_ISSET_ID = 2;
  private static final int __SUBSUB_ISSET_ID = 3;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.MINOR,_Fields.SUB,_Fields.SUBSUB};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.MAJOR, new org.apache.thrift.meta_data.FieldMetaData("Major", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I16)));
    tmpMap.put(_Fields.MINOR, new org.apache.thrift.meta_data.FieldMetaData("Minor", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I16)));
    tmpMap.put(_Fields.SUB, new org.apache.thrift.meta_data.FieldMetaData("Sub", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I16)));
    tmpMap.put(_Fields.SUBSUB, new org.apache.thrift.meta_data.FieldMetaData("Subsub", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I16)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MVersion.class, metaDataMap);
  }

  public MVersion() {
  }

  public MVersion(
    short Major)
  {
    this();
    this.Major = Major;
    setMajorIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MVersion(MVersion other) {
    __isset_bitfield = other.__isset_bitfield;
    this.Major = other.Major;
    this.Minor = other.Minor;
    this.Sub = other.Sub;
    this.Subsub = other.Subsub;
  }

  public MVersion deepCopy() {
    return new MVersion(this);
  }

  @Override
  public void clear() {
    setMajorIsSet(false);
    this.Major = 0;
    setMinorIsSet(false);
    this.Minor = 0;
    setSubIsSet(false);
    this.Sub = 0;
    setSubsubIsSet(false);
    this.Subsub = 0;
  }

  public short getMajor() {
    return this.Major;
  }

  public MVersion setMajor(short Major) {
    this.Major = Major;
    setMajorIsSet(true);
    return this;
  }

  public void unsetMajor() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __MAJOR_ISSET_ID);
  }

  /** Returns true if field Major is set (has been assigned a value) and false otherwise */
  public boolean isSetMajor() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __MAJOR_ISSET_ID);
  }

  public void setMajorIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __MAJOR_ISSET_ID, value);
  }

  public short getMinor() {
    return this.Minor;
  }

  public MVersion setMinor(short Minor) {
    this.Minor = Minor;
    setMinorIsSet(true);
    return this;
  }

  public void unsetMinor() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __MINOR_ISSET_ID);
  }

  /** Returns true if field Minor is set (has been assigned a value) and false otherwise */
  public boolean isSetMinor() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __MINOR_ISSET_ID);
  }

  public void setMinorIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __MINOR_ISSET_ID, value);
  }

  public short getSub() {
    return this.Sub;
  }

  public MVersion setSub(short Sub) {
    this.Sub = Sub;
    setSubIsSet(true);
    return this;
  }

  public void unsetSub() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __SUB_ISSET_ID);
  }

  /** Returns true if field Sub is set (has been assigned a value) and false otherwise */
  public boolean isSetSub() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __SUB_ISSET_ID);
  }

  public void setSubIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __SUB_ISSET_ID, value);
  }

  public short getSubsub() {
    return this.Subsub;
  }

  public MVersion setSubsub(short Subsub) {
    this.Subsub = Subsub;
    setSubsubIsSet(true);
    return this;
  }

  public void unsetSubsub() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __SUBSUB_ISSET_ID);
  }

  /** Returns true if field Subsub is set (has been assigned a value) and false otherwise */
  public boolean isSetSubsub() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __SUBSUB_ISSET_ID);
  }

  public void setSubsubIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __SUBSUB_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case MAJOR:
      if (value == null) {
        unsetMajor();
      } else {
        setMajor((java.lang.Short)value);
      }
      break;

    case MINOR:
      if (value == null) {
        unsetMinor();
      } else {
        setMinor((java.lang.Short)value);
      }
      break;

    case SUB:
      if (value == null) {
        unsetSub();
      } else {
        setSub((java.lang.Short)value);
      }
      break;

    case SUBSUB:
      if (value == null) {
        unsetSubsub();
      } else {
        setSubsub((java.lang.Short)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case MAJOR:
      return getMajor();

    case MINOR:
      return getMinor();

    case SUB:
      return getSub();

    case SUBSUB:
      return getSubsub();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case MAJOR:
      return isSetMajor();
    case MINOR:
      return isSetMinor();
    case SUB:
      return isSetSub();
    case SUBSUB:
      return isSetSubsub();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MVersion)
      return this.equals((MVersion)that);
    return false;
  }

  public boolean equals(MVersion that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_Major = true;
    boolean that_present_Major = true;
    if (this_present_Major || that_present_Major) {
      if (!(this_present_Major && that_present_Major))
        return false;
      if (this.Major != that.Major)
        return false;
    }

    boolean this_present_Minor = true && this.isSetMinor();
    boolean that_present_Minor = true && that.isSetMinor();
    if (this_present_Minor || that_present_Minor) {
      if (!(this_present_Minor && that_present_Minor))
        return false;
      if (this.Minor != that.Minor)
        return false;
    }

    boolean this_present_Sub = true && this.isSetSub();
    boolean that_present_Sub = true && that.isSetSub();
    if (this_present_Sub || that_present_Sub) {
      if (!(this_present_Sub && that_present_Sub))
        return false;
      if (this.Sub != that.Sub)
        return false;
    }

    boolean this_present_Subsub = true && this.isSetSubsub();
    boolean that_present_Subsub = true && that.isSetSubsub();
    if (this_present_Subsub || that_present_Subsub) {
      if (!(this_present_Subsub && that_present_Subsub))
        return false;
      if (this.Subsub != that.Subsub)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + Major;

    hashCode = hashCode * 8191 + ((isSetMinor()) ? 131071 : 524287);
    if (isSetMinor())
      hashCode = hashCode * 8191 + Minor;

    hashCode = hashCode * 8191 + ((isSetSub()) ? 131071 : 524287);
    if (isSetSub())
      hashCode = hashCode * 8191 + Sub;

    hashCode = hashCode * 8191 + ((isSetSubsub()) ? 131071 : 524287);
    if (isSetSubsub())
      hashCode = hashCode * 8191 + Subsub;

    return hashCode;
  }

  @Override
  public int compareTo(MVersion other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetMajor()).compareTo(other.isSetMajor());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMajor()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Major, other.Major);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetMinor()).compareTo(other.isSetMinor());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMinor()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Minor, other.Minor);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetSub()).compareTo(other.isSetSub());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSub()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Sub, other.Sub);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetSubsub()).compareTo(other.isSetSubsub());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSubsub()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Subsub, other.Subsub);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.thrift.annotation.Nullable
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MVersion(");
    boolean first = true;

    sb.append("Major:");
    sb.append(this.Major);
    first = false;
    if (isSetMinor()) {
      if (!first) sb.append(", ");
      sb.append("Minor:");
      sb.append(this.Minor);
      first = false;
    }
    if (isSetSub()) {
      if (!first) sb.append(", ");
      sb.append("Sub:");
      sb.append(this.Sub);
      first = false;
    }
    if (isSetSubsub()) {
      if (!first) sb.append(", ");
      sb.append("Subsub:");
      sb.append(this.Subsub);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'Major' because it's a primitive and you chose the non-beans generator.
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class MVersionStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MVersionStandardScheme getScheme() {
      return new MVersionStandardScheme();
    }
  }

  private static class MVersionStandardScheme extends org.apache.thrift.scheme.StandardScheme<MVersion> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MVersion struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // MAJOR
            if (schemeField.type == org.apache.thrift.protocol.TType.I16) {
              struct.Major = iprot.readI16();
              struct.setMajorIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // MINOR
            if (schemeField.type == org.apache.thrift.protocol.TType.I16) {
              struct.Minor = iprot.readI16();
              struct.setMinorIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // SUB
            if (schemeField.type == org.apache.thrift.protocol.TType.I16) {
              struct.Sub = iprot.readI16();
              struct.setSubIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // SUBSUB
            if (schemeField.type == org.apache.thrift.protocol.TType.I16) {
              struct.Subsub = iprot.readI16();
              struct.setSubsubIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      if (!struct.isSetMajor()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'Major' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, MVersion struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(MAJOR_FIELD_DESC);
      oprot.writeI16(struct.Major);
      oprot.writeFieldEnd();
      if (struct.isSetMinor()) {
        oprot.writeFieldBegin(MINOR_FIELD_DESC);
        oprot.writeI16(struct.Minor);
        oprot.writeFieldEnd();
      }
      if (struct.isSetSub()) {
        oprot.writeFieldBegin(SUB_FIELD_DESC);
        oprot.writeI16(struct.Sub);
        oprot.writeFieldEnd();
      }
      if (struct.isSetSubsub()) {
        oprot.writeFieldBegin(SUBSUB_FIELD_DESC);
        oprot.writeI16(struct.Subsub);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MVersionTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MVersionTupleScheme getScheme() {
      return new MVersionTupleScheme();
    }
  }

  private static class MVersionTupleScheme extends org.apache.thrift.scheme.TupleScheme<MVersion> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MVersion struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeI16(struct.Major);
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetMinor()) {
        optionals.set(0);
      }
      if (struct.isSetSub()) {
        optionals.set(1);
      }
      if (struct.isSetSubsub()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetMinor()) {
        oprot.writeI16(struct.Minor);
      }
      if (struct.isSetSub()) {
        oprot.writeI16(struct.Sub);
      }
      if (struct.isSetSubsub()) {
        oprot.writeI16(struct.Subsub);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MVersion struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.Major = iprot.readI16();
      struct.setMajorIsSet(true);
      java.util.BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.Minor = iprot.readI16();
        struct.setMinorIsSet(true);
      }
      if (incoming.get(1)) {
        struct.Sub = iprot.readI16();
        struct.setSubIsSet(true);
      }
      if (incoming.get(2)) {
        struct.Subsub = iprot.readI16();
        struct.setSubsubIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

