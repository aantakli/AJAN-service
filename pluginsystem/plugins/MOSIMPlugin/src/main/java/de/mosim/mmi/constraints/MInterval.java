/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.constraints;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2023-01-11")
public class MInterval implements org.apache.thrift.TBase<MInterval, MInterval._Fields>, java.io.Serializable, Cloneable, Comparable<MInterval> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MInterval");

  private static final org.apache.thrift.protocol.TField MIN_FIELD_DESC = new org.apache.thrift.protocol.TField("Min", org.apache.thrift.protocol.TType.DOUBLE, (short)1);
  private static final org.apache.thrift.protocol.TField MAX_FIELD_DESC = new org.apache.thrift.protocol.TField("Max", org.apache.thrift.protocol.TType.DOUBLE, (short)2);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MIntervalStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MIntervalTupleSchemeFactory();

  public double Min; // required
  public double Max; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    MIN((short)1, "Min"),
    MAX((short)2, "Max");

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
        case 1: // MIN
          return MIN;
        case 2: // MAX
          return MAX;
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
  private static final int __MIN_ISSET_ID = 0;
  private static final int __MAX_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.MIN, new org.apache.thrift.meta_data.FieldMetaData("Min", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    tmpMap.put(_Fields.MAX, new org.apache.thrift.meta_data.FieldMetaData("Max", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MInterval.class, metaDataMap);
  }

  public MInterval() {
  }

  public MInterval(
    double Min,
    double Max)
  {
    this();
    this.Min = Min;
    setMinIsSet(true);
    this.Max = Max;
    setMaxIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MInterval(MInterval other) {
    __isset_bitfield = other.__isset_bitfield;
    this.Min = other.Min;
    this.Max = other.Max;
  }

  public MInterval deepCopy() {
    return new MInterval(this);
  }

  @Override
  public void clear() {
    setMinIsSet(false);
    this.Min = 0.0;
    setMaxIsSet(false);
    this.Max = 0.0;
  }

  public double getMin() {
    return this.Min;
  }

  public MInterval setMin(double Min) {
    this.Min = Min;
    setMinIsSet(true);
    return this;
  }

  public void unsetMin() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __MIN_ISSET_ID);
  }

  /** Returns true if field Min is set (has been assigned a value) and false otherwise */
  public boolean isSetMin() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __MIN_ISSET_ID);
  }

  public void setMinIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __MIN_ISSET_ID, value);
  }

  public double getMax() {
    return this.Max;
  }

  public MInterval setMax(double Max) {
    this.Max = Max;
    setMaxIsSet(true);
    return this;
  }

  public void unsetMax() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __MAX_ISSET_ID);
  }

  /** Returns true if field Max is set (has been assigned a value) and false otherwise */
  public boolean isSetMax() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __MAX_ISSET_ID);
  }

  public void setMaxIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __MAX_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case MIN:
      if (value == null) {
        unsetMin();
      } else {
        setMin((java.lang.Double)value);
      }
      break;

    case MAX:
      if (value == null) {
        unsetMax();
      } else {
        setMax((java.lang.Double)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case MIN:
      return getMin();

    case MAX:
      return getMax();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case MIN:
      return isSetMin();
    case MAX:
      return isSetMax();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MInterval)
      return this.equals((MInterval)that);
    return false;
  }

  public boolean equals(MInterval that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_Min = true;
    boolean that_present_Min = true;
    if (this_present_Min || that_present_Min) {
      if (!(this_present_Min && that_present_Min))
        return false;
      if (this.Min != that.Min)
        return false;
    }

    boolean this_present_Max = true;
    boolean that_present_Max = true;
    if (this_present_Max || that_present_Max) {
      if (!(this_present_Max && that_present_Max))
        return false;
      if (this.Max != that.Max)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(Min);

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(Max);

    return hashCode;
  }

  @Override
  public int compareTo(MInterval other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetMin()).compareTo(other.isSetMin());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMin()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Min, other.Min);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetMax()).compareTo(other.isSetMax());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMax()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Max, other.Max);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MInterval(");
    boolean first = true;

    sb.append("Min:");
    sb.append(this.Min);
    first = false;
    if (!first) sb.append(", ");
    sb.append("Max:");
    sb.append(this.Max);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'Min' because it's a primitive and you chose the non-beans generator.
    // alas, we cannot check 'Max' because it's a primitive and you chose the non-beans generator.
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

  private static class MIntervalStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MIntervalStandardScheme getScheme() {
      return new MIntervalStandardScheme();
    }
  }

  private static class MIntervalStandardScheme extends org.apache.thrift.scheme.StandardScheme<MInterval> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MInterval struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // MIN
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.Min = iprot.readDouble();
              struct.setMinIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // MAX
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.Max = iprot.readDouble();
              struct.setMaxIsSet(true);
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
      if (!struct.isSetMin()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'Min' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetMax()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'Max' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, MInterval struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(MIN_FIELD_DESC);
      oprot.writeDouble(struct.Min);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(MAX_FIELD_DESC);
      oprot.writeDouble(struct.Max);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MIntervalTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MIntervalTupleScheme getScheme() {
      return new MIntervalTupleScheme();
    }
  }

  private static class MIntervalTupleScheme extends org.apache.thrift.scheme.TupleScheme<MInterval> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MInterval struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeDouble(struct.Min);
      oprot.writeDouble(struct.Max);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MInterval struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.Min = iprot.readDouble();
      struct.setMinIsSet(true);
      struct.Max = iprot.readDouble();
      struct.setMaxIsSet(true);
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

