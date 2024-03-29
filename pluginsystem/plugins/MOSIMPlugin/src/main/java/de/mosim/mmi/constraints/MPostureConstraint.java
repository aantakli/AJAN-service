/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.constraints;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2023-06-29")
public class MPostureConstraint implements org.apache.thrift.TBase<MPostureConstraint, MPostureConstraint._Fields>, java.io.Serializable, Cloneable, Comparable<MPostureConstraint> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MPostureConstraint");

  private static final org.apache.thrift.protocol.TField POSTURE_FIELD_DESC = new org.apache.thrift.protocol.TField("posture", org.apache.thrift.protocol.TType.STRUCT, (short)1);
  private static final org.apache.thrift.protocol.TField JOINT_CONSTRAINTS_FIELD_DESC = new org.apache.thrift.protocol.TField("JointConstraints", org.apache.thrift.protocol.TType.LIST, (short)2);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MPostureConstraintStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MPostureConstraintTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable de.mosim.mmi.avatar.MAvatarPostureValues posture; // required
  public @org.apache.thrift.annotation.Nullable java.util.List<MJointConstraint> JointConstraints; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    POSTURE((short)1, "posture"),
    JOINT_CONSTRAINTS((short)2, "JointConstraints");

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
        case 1: // POSTURE
          return POSTURE;
        case 2: // JOINT_CONSTRAINTS
          return JOINT_CONSTRAINTS;
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
  private static final _Fields optionals[] = {_Fields.JOINT_CONSTRAINTS};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.POSTURE, new org.apache.thrift.meta_data.FieldMetaData("posture", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.avatar.MAvatarPostureValues.class)));
    tmpMap.put(_Fields.JOINT_CONSTRAINTS, new org.apache.thrift.meta_data.FieldMetaData("JointConstraints", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, MJointConstraint.class))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MPostureConstraint.class, metaDataMap);
  }

  public MPostureConstraint() {
  }

  public MPostureConstraint(
    de.mosim.mmi.avatar.MAvatarPostureValues posture)
  {
    this();
    this.posture = posture;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MPostureConstraint(MPostureConstraint other) {
    if (other.isSetPosture()) {
      this.posture = new de.mosim.mmi.avatar.MAvatarPostureValues(other.posture);
    }
    if (other.isSetJointConstraints()) {
      java.util.List<MJointConstraint> __this__JointConstraints = new java.util.ArrayList<MJointConstraint>(other.JointConstraints.size());
      for (MJointConstraint other_element : other.JointConstraints) {
        __this__JointConstraints.add(new MJointConstraint(other_element));
      }
      this.JointConstraints = __this__JointConstraints;
    }
  }

  public MPostureConstraint deepCopy() {
    return new MPostureConstraint(this);
  }

  @Override
  public void clear() {
    this.posture = null;
    this.JointConstraints = null;
  }

  @org.apache.thrift.annotation.Nullable
  public de.mosim.mmi.avatar.MAvatarPostureValues getPosture() {
    return this.posture;
  }

  public MPostureConstraint setPosture(@org.apache.thrift.annotation.Nullable de.mosim.mmi.avatar.MAvatarPostureValues posture) {
    this.posture = posture;
    return this;
  }

  public void unsetPosture() {
    this.posture = null;
  }

  /** Returns true if field posture is set (has been assigned a value) and false otherwise */
  public boolean isSetPosture() {
    return this.posture != null;
  }

  public void setPostureIsSet(boolean value) {
    if (!value) {
      this.posture = null;
    }
  }

  public int getJointConstraintsSize() {
    return (this.JointConstraints == null) ? 0 : this.JointConstraints.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<MJointConstraint> getJointConstraintsIterator() {
    return (this.JointConstraints == null) ? null : this.JointConstraints.iterator();
  }

  public void addToJointConstraints(MJointConstraint elem) {
    if (this.JointConstraints == null) {
      this.JointConstraints = new java.util.ArrayList<MJointConstraint>();
    }
    this.JointConstraints.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<MJointConstraint> getJointConstraints() {
    return this.JointConstraints;
  }

  public MPostureConstraint setJointConstraints(@org.apache.thrift.annotation.Nullable java.util.List<MJointConstraint> JointConstraints) {
    this.JointConstraints = JointConstraints;
    return this;
  }

  public void unsetJointConstraints() {
    this.JointConstraints = null;
  }

  /** Returns true if field JointConstraints is set (has been assigned a value) and false otherwise */
  public boolean isSetJointConstraints() {
    return this.JointConstraints != null;
  }

  public void setJointConstraintsIsSet(boolean value) {
    if (!value) {
      this.JointConstraints = null;
    }
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case POSTURE:
      if (value == null) {
        unsetPosture();
      } else {
        setPosture((de.mosim.mmi.avatar.MAvatarPostureValues)value);
      }
      break;

    case JOINT_CONSTRAINTS:
      if (value == null) {
        unsetJointConstraints();
      } else {
        setJointConstraints((java.util.List<MJointConstraint>)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case POSTURE:
      return getPosture();

    case JOINT_CONSTRAINTS:
      return getJointConstraints();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case POSTURE:
      return isSetPosture();
    case JOINT_CONSTRAINTS:
      return isSetJointConstraints();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MPostureConstraint)
      return this.equals((MPostureConstraint)that);
    return false;
  }

  public boolean equals(MPostureConstraint that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_posture = true && this.isSetPosture();
    boolean that_present_posture = true && that.isSetPosture();
    if (this_present_posture || that_present_posture) {
      if (!(this_present_posture && that_present_posture))
        return false;
      if (!this.posture.equals(that.posture))
        return false;
    }

    boolean this_present_JointConstraints = true && this.isSetJointConstraints();
    boolean that_present_JointConstraints = true && that.isSetJointConstraints();
    if (this_present_JointConstraints || that_present_JointConstraints) {
      if (!(this_present_JointConstraints && that_present_JointConstraints))
        return false;
      if (!this.JointConstraints.equals(that.JointConstraints))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetPosture()) ? 131071 : 524287);
    if (isSetPosture())
      hashCode = hashCode * 8191 + posture.hashCode();

    hashCode = hashCode * 8191 + ((isSetJointConstraints()) ? 131071 : 524287);
    if (isSetJointConstraints())
      hashCode = hashCode * 8191 + JointConstraints.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(MPostureConstraint other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetPosture()).compareTo(other.isSetPosture());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPosture()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.posture, other.posture);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetJointConstraints()).compareTo(other.isSetJointConstraints());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetJointConstraints()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.JointConstraints, other.JointConstraints);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MPostureConstraint(");
    boolean first = true;

    sb.append("posture:");
    if (this.posture == null) {
      sb.append("null");
    } else {
      sb.append(this.posture);
    }
    first = false;
    if (isSetJointConstraints()) {
      if (!first) sb.append(", ");
      sb.append("JointConstraints:");
      if (this.JointConstraints == null) {
        sb.append("null");
      } else {
        sb.append(this.JointConstraints);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (posture == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'posture' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
    if (posture != null) {
      posture.validate();
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
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class MPostureConstraintStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MPostureConstraintStandardScheme getScheme() {
      return new MPostureConstraintStandardScheme();
    }
  }

  private static class MPostureConstraintStandardScheme extends org.apache.thrift.scheme.StandardScheme<MPostureConstraint> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MPostureConstraint struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // POSTURE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.posture = new de.mosim.mmi.avatar.MAvatarPostureValues();
              struct.posture.read(iprot);
              struct.setPostureIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // JOINT_CONSTRAINTS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list8 = iprot.readListBegin();
                struct.JointConstraints = new java.util.ArrayList<MJointConstraint>(_list8.size);
                @org.apache.thrift.annotation.Nullable MJointConstraint _elem9;
                for (int _i10 = 0; _i10 < _list8.size; ++_i10)
                {
                  _elem9 = new MJointConstraint();
                  _elem9.read(iprot);
                  struct.JointConstraints.add(_elem9);
                }
                iprot.readListEnd();
              }
              struct.setJointConstraintsIsSet(true);
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
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, MPostureConstraint struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.posture != null) {
        oprot.writeFieldBegin(POSTURE_FIELD_DESC);
        struct.posture.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.JointConstraints != null) {
        if (struct.isSetJointConstraints()) {
          oprot.writeFieldBegin(JOINT_CONSTRAINTS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.JointConstraints.size()));
            for (MJointConstraint _iter11 : struct.JointConstraints)
            {
              _iter11.write(oprot);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MPostureConstraintTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MPostureConstraintTupleScheme getScheme() {
      return new MPostureConstraintTupleScheme();
    }
  }

  private static class MPostureConstraintTupleScheme extends org.apache.thrift.scheme.TupleScheme<MPostureConstraint> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MPostureConstraint struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.posture.write(oprot);
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetJointConstraints()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetJointConstraints()) {
        {
          oprot.writeI32(struct.JointConstraints.size());
          for (MJointConstraint _iter12 : struct.JointConstraints)
          {
            _iter12.write(oprot);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MPostureConstraint struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.posture = new de.mosim.mmi.avatar.MAvatarPostureValues();
      struct.posture.read(iprot);
      struct.setPostureIsSet(true);
      java.util.BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list13 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.JointConstraints = new java.util.ArrayList<MJointConstraint>(_list13.size);
          @org.apache.thrift.annotation.Nullable MJointConstraint _elem14;
          for (int _i15 = 0; _i15 < _list13.size; ++_i15)
          {
            _elem14 = new MJointConstraint();
            _elem14.read(iprot);
            struct.JointConstraints.add(_elem14);
          }
        }
        struct.setJointConstraintsIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

