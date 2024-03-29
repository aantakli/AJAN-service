/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.services;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2023-06-29")
public class MWalkPoint implements org.apache.thrift.TBase<MWalkPoint, MWalkPoint._Fields>, java.io.Serializable, Cloneable, Comparable<MWalkPoint> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MWalkPoint");

  private static final org.apache.thrift.protocol.TField POSITION_CONSTRAINT_FIELD_DESC = new org.apache.thrift.protocol.TField("PositionConstraint", org.apache.thrift.protocol.TType.STRUCT, (short)1);
  private static final org.apache.thrift.protocol.TField SUITABILITY_FIELD_DESC = new org.apache.thrift.protocol.TField("Suitability", org.apache.thrift.protocol.TType.DOUBLE, (short)2);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MWalkPointStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MWalkPointTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable de.mosim.mmi.constraints.MGeometryConstraint PositionConstraint; // required
  public double Suitability; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    POSITION_CONSTRAINT((short)1, "PositionConstraint"),
    SUITABILITY((short)2, "Suitability");

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
        case 1: // POSITION_CONSTRAINT
          return POSITION_CONSTRAINT;
        case 2: // SUITABILITY
          return SUITABILITY;
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
  private static final int __SUITABILITY_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.POSITION_CONSTRAINT, new org.apache.thrift.meta_data.FieldMetaData("PositionConstraint", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.constraints.MGeometryConstraint.class)));
    tmpMap.put(_Fields.SUITABILITY, new org.apache.thrift.meta_data.FieldMetaData("Suitability", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MWalkPoint.class, metaDataMap);
  }

  public MWalkPoint() {
  }

  public MWalkPoint(
    de.mosim.mmi.constraints.MGeometryConstraint PositionConstraint,
    double Suitability)
  {
    this();
    this.PositionConstraint = PositionConstraint;
    this.Suitability = Suitability;
    setSuitabilityIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MWalkPoint(MWalkPoint other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetPositionConstraint()) {
      this.PositionConstraint = new de.mosim.mmi.constraints.MGeometryConstraint(other.PositionConstraint);
    }
    this.Suitability = other.Suitability;
  }

  public MWalkPoint deepCopy() {
    return new MWalkPoint(this);
  }

  @Override
  public void clear() {
    this.PositionConstraint = null;
    setSuitabilityIsSet(false);
    this.Suitability = 0.0;
  }

  @org.apache.thrift.annotation.Nullable
  public de.mosim.mmi.constraints.MGeometryConstraint getPositionConstraint() {
    return this.PositionConstraint;
  }

  public MWalkPoint setPositionConstraint(@org.apache.thrift.annotation.Nullable de.mosim.mmi.constraints.MGeometryConstraint PositionConstraint) {
    this.PositionConstraint = PositionConstraint;
    return this;
  }

  public void unsetPositionConstraint() {
    this.PositionConstraint = null;
  }

  /** Returns true if field PositionConstraint is set (has been assigned a value) and false otherwise */
  public boolean isSetPositionConstraint() {
    return this.PositionConstraint != null;
  }

  public void setPositionConstraintIsSet(boolean value) {
    if (!value) {
      this.PositionConstraint = null;
    }
  }

  public double getSuitability() {
    return this.Suitability;
  }

  public MWalkPoint setSuitability(double Suitability) {
    this.Suitability = Suitability;
    setSuitabilityIsSet(true);
    return this;
  }

  public void unsetSuitability() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __SUITABILITY_ISSET_ID);
  }

  /** Returns true if field Suitability is set (has been assigned a value) and false otherwise */
  public boolean isSetSuitability() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __SUITABILITY_ISSET_ID);
  }

  public void setSuitabilityIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __SUITABILITY_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case POSITION_CONSTRAINT:
      if (value == null) {
        unsetPositionConstraint();
      } else {
        setPositionConstraint((de.mosim.mmi.constraints.MGeometryConstraint)value);
      }
      break;

    case SUITABILITY:
      if (value == null) {
        unsetSuitability();
      } else {
        setSuitability((java.lang.Double)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case POSITION_CONSTRAINT:
      return getPositionConstraint();

    case SUITABILITY:
      return getSuitability();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case POSITION_CONSTRAINT:
      return isSetPositionConstraint();
    case SUITABILITY:
      return isSetSuitability();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MWalkPoint)
      return this.equals((MWalkPoint)that);
    return false;
  }

  public boolean equals(MWalkPoint that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_PositionConstraint = true && this.isSetPositionConstraint();
    boolean that_present_PositionConstraint = true && that.isSetPositionConstraint();
    if (this_present_PositionConstraint || that_present_PositionConstraint) {
      if (!(this_present_PositionConstraint && that_present_PositionConstraint))
        return false;
      if (!this.PositionConstraint.equals(that.PositionConstraint))
        return false;
    }

    boolean this_present_Suitability = true;
    boolean that_present_Suitability = true;
    if (this_present_Suitability || that_present_Suitability) {
      if (!(this_present_Suitability && that_present_Suitability))
        return false;
      if (this.Suitability != that.Suitability)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetPositionConstraint()) ? 131071 : 524287);
    if (isSetPositionConstraint())
      hashCode = hashCode * 8191 + PositionConstraint.hashCode();

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(Suitability);

    return hashCode;
  }

  @Override
  public int compareTo(MWalkPoint other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetPositionConstraint()).compareTo(other.isSetPositionConstraint());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPositionConstraint()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.PositionConstraint, other.PositionConstraint);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetSuitability()).compareTo(other.isSetSuitability());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSuitability()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Suitability, other.Suitability);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MWalkPoint(");
    boolean first = true;

    sb.append("PositionConstraint:");
    if (this.PositionConstraint == null) {
      sb.append("null");
    } else {
      sb.append(this.PositionConstraint);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("Suitability:");
    sb.append(this.Suitability);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (PositionConstraint == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'PositionConstraint' was not present! Struct: " + toString());
    }
    // alas, we cannot check 'Suitability' because it's a primitive and you chose the non-beans generator.
    // check for sub-struct validity
    if (PositionConstraint != null) {
      PositionConstraint.validate();
    }
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

  private static class MWalkPointStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MWalkPointStandardScheme getScheme() {
      return new MWalkPointStandardScheme();
    }
  }

  private static class MWalkPointStandardScheme extends org.apache.thrift.scheme.StandardScheme<MWalkPoint> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MWalkPoint struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // POSITION_CONSTRAINT
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.PositionConstraint = new de.mosim.mmi.constraints.MGeometryConstraint();
              struct.PositionConstraint.read(iprot);
              struct.setPositionConstraintIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // SUITABILITY
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.Suitability = iprot.readDouble();
              struct.setSuitabilityIsSet(true);
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
      if (!struct.isSetSuitability()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'Suitability' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, MWalkPoint struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.PositionConstraint != null) {
        oprot.writeFieldBegin(POSITION_CONSTRAINT_FIELD_DESC);
        struct.PositionConstraint.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(SUITABILITY_FIELD_DESC);
      oprot.writeDouble(struct.Suitability);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MWalkPointTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MWalkPointTupleScheme getScheme() {
      return new MWalkPointTupleScheme();
    }
  }

  private static class MWalkPointTupleScheme extends org.apache.thrift.scheme.TupleScheme<MWalkPoint> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MWalkPoint struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.PositionConstraint.write(oprot);
      oprot.writeDouble(struct.Suitability);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MWalkPoint struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.PositionConstraint = new de.mosim.mmi.constraints.MGeometryConstraint();
      struct.PositionConstraint.read(iprot);
      struct.setPositionConstraintIsSet(true);
      struct.Suitability = iprot.readDouble();
      struct.setSuitabilityIsSet(true);
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

