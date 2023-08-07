/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.constraints;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2023-06-29")
public class MAccelerationConstraint implements org.apache.thrift.TBase<MAccelerationConstraint, MAccelerationConstraint._Fields>, java.io.Serializable, Cloneable, Comparable<MAccelerationConstraint> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MAccelerationConstraint");

  private static final org.apache.thrift.protocol.TField PARENT_OBJECT_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("ParentObjectID", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField PARENT_TO_CONSTRAINT_FIELD_DESC = new org.apache.thrift.protocol.TField("ParentToConstraint", org.apache.thrift.protocol.TType.STRUCT, (short)2);
  private static final org.apache.thrift.protocol.TField TRANSLATIONAL_ACCELERATION_FIELD_DESC = new org.apache.thrift.protocol.TField("TranslationalAcceleration", org.apache.thrift.protocol.TType.STRUCT, (short)3);
  private static final org.apache.thrift.protocol.TField ROTATIONAL_ACCELERATION_FIELD_DESC = new org.apache.thrift.protocol.TField("RotationalAcceleration", org.apache.thrift.protocol.TType.STRUCT, (short)4);
  private static final org.apache.thrift.protocol.TField WEIGHTING_FACTOR_FIELD_DESC = new org.apache.thrift.protocol.TField("WeightingFactor", org.apache.thrift.protocol.TType.DOUBLE, (short)5);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MAccelerationConstraintStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MAccelerationConstraintTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.lang.String ParentObjectID; // required
  public @org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MTransform ParentToConstraint; // optional
  public @org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MVector3 TranslationalAcceleration; // optional
  public @org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MVector3 RotationalAcceleration; // optional
  public double WeightingFactor; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    PARENT_OBJECT_ID((short)1, "ParentObjectID"),
    PARENT_TO_CONSTRAINT((short)2, "ParentToConstraint"),
    TRANSLATIONAL_ACCELERATION((short)3, "TranslationalAcceleration"),
    ROTATIONAL_ACCELERATION((short)4, "RotationalAcceleration"),
    WEIGHTING_FACTOR((short)5, "WeightingFactor");

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
        case 1: // PARENT_OBJECT_ID
          return PARENT_OBJECT_ID;
        case 2: // PARENT_TO_CONSTRAINT
          return PARENT_TO_CONSTRAINT;
        case 3: // TRANSLATIONAL_ACCELERATION
          return TRANSLATIONAL_ACCELERATION;
        case 4: // ROTATIONAL_ACCELERATION
          return ROTATIONAL_ACCELERATION;
        case 5: // WEIGHTING_FACTOR
          return WEIGHTING_FACTOR;
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
  private static final int __WEIGHTINGFACTOR_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.PARENT_TO_CONSTRAINT,_Fields.TRANSLATIONAL_ACCELERATION,_Fields.ROTATIONAL_ACCELERATION,_Fields.WEIGHTING_FACTOR};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.PARENT_OBJECT_ID, new org.apache.thrift.meta_data.FieldMetaData("ParentObjectID", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.PARENT_TO_CONSTRAINT, new org.apache.thrift.meta_data.FieldMetaData("ParentToConstraint", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.math.MTransform.class)));
    tmpMap.put(_Fields.TRANSLATIONAL_ACCELERATION, new org.apache.thrift.meta_data.FieldMetaData("TranslationalAcceleration", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.math.MVector3.class)));
    tmpMap.put(_Fields.ROTATIONAL_ACCELERATION, new org.apache.thrift.meta_data.FieldMetaData("RotationalAcceleration", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.math.MVector3.class)));
    tmpMap.put(_Fields.WEIGHTING_FACTOR, new org.apache.thrift.meta_data.FieldMetaData("WeightingFactor", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MAccelerationConstraint.class, metaDataMap);
  }

  public MAccelerationConstraint() {
  }

  public MAccelerationConstraint(
    java.lang.String ParentObjectID)
  {
    this();
    this.ParentObjectID = ParentObjectID;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MAccelerationConstraint(MAccelerationConstraint other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetParentObjectID()) {
      this.ParentObjectID = other.ParentObjectID;
    }
    if (other.isSetParentToConstraint()) {
      this.ParentToConstraint = new de.mosim.mmi.math.MTransform(other.ParentToConstraint);
    }
    if (other.isSetTranslationalAcceleration()) {
      this.TranslationalAcceleration = new de.mosim.mmi.math.MVector3(other.TranslationalAcceleration);
    }
    if (other.isSetRotationalAcceleration()) {
      this.RotationalAcceleration = new de.mosim.mmi.math.MVector3(other.RotationalAcceleration);
    }
    this.WeightingFactor = other.WeightingFactor;
  }

  public MAccelerationConstraint deepCopy() {
    return new MAccelerationConstraint(this);
  }

  @Override
  public void clear() {
    this.ParentObjectID = null;
    this.ParentToConstraint = null;
    this.TranslationalAcceleration = null;
    this.RotationalAcceleration = null;
    setWeightingFactorIsSet(false);
    this.WeightingFactor = 0.0;
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getParentObjectID() {
    return this.ParentObjectID;
  }

  public MAccelerationConstraint setParentObjectID(@org.apache.thrift.annotation.Nullable java.lang.String ParentObjectID) {
    this.ParentObjectID = ParentObjectID;
    return this;
  }

  public void unsetParentObjectID() {
    this.ParentObjectID = null;
  }

  /** Returns true if field ParentObjectID is set (has been assigned a value) and false otherwise */
  public boolean isSetParentObjectID() {
    return this.ParentObjectID != null;
  }

  public void setParentObjectIDIsSet(boolean value) {
    if (!value) {
      this.ParentObjectID = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public de.mosim.mmi.math.MTransform getParentToConstraint() {
    return this.ParentToConstraint;
  }

  public MAccelerationConstraint setParentToConstraint(@org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MTransform ParentToConstraint) {
    this.ParentToConstraint = ParentToConstraint;
    return this;
  }

  public void unsetParentToConstraint() {
    this.ParentToConstraint = null;
  }

  /** Returns true if field ParentToConstraint is set (has been assigned a value) and false otherwise */
  public boolean isSetParentToConstraint() {
    return this.ParentToConstraint != null;
  }

  public void setParentToConstraintIsSet(boolean value) {
    if (!value) {
      this.ParentToConstraint = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public de.mosim.mmi.math.MVector3 getTranslationalAcceleration() {
    return this.TranslationalAcceleration;
  }

  public MAccelerationConstraint setTranslationalAcceleration(@org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MVector3 TranslationalAcceleration) {
    this.TranslationalAcceleration = TranslationalAcceleration;
    return this;
  }

  public void unsetTranslationalAcceleration() {
    this.TranslationalAcceleration = null;
  }

  /** Returns true if field TranslationalAcceleration is set (has been assigned a value) and false otherwise */
  public boolean isSetTranslationalAcceleration() {
    return this.TranslationalAcceleration != null;
  }

  public void setTranslationalAccelerationIsSet(boolean value) {
    if (!value) {
      this.TranslationalAcceleration = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public de.mosim.mmi.math.MVector3 getRotationalAcceleration() {
    return this.RotationalAcceleration;
  }

  public MAccelerationConstraint setRotationalAcceleration(@org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MVector3 RotationalAcceleration) {
    this.RotationalAcceleration = RotationalAcceleration;
    return this;
  }

  public void unsetRotationalAcceleration() {
    this.RotationalAcceleration = null;
  }

  /** Returns true if field RotationalAcceleration is set (has been assigned a value) and false otherwise */
  public boolean isSetRotationalAcceleration() {
    return this.RotationalAcceleration != null;
  }

  public void setRotationalAccelerationIsSet(boolean value) {
    if (!value) {
      this.RotationalAcceleration = null;
    }
  }

  public double getWeightingFactor() {
    return this.WeightingFactor;
  }

  public MAccelerationConstraint setWeightingFactor(double WeightingFactor) {
    this.WeightingFactor = WeightingFactor;
    setWeightingFactorIsSet(true);
    return this;
  }

  public void unsetWeightingFactor() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __WEIGHTINGFACTOR_ISSET_ID);
  }

  /** Returns true if field WeightingFactor is set (has been assigned a value) and false otherwise */
  public boolean isSetWeightingFactor() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __WEIGHTINGFACTOR_ISSET_ID);
  }

  public void setWeightingFactorIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __WEIGHTINGFACTOR_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case PARENT_OBJECT_ID:
      if (value == null) {
        unsetParentObjectID();
      } else {
        setParentObjectID((java.lang.String)value);
      }
      break;

    case PARENT_TO_CONSTRAINT:
      if (value == null) {
        unsetParentToConstraint();
      } else {
        setParentToConstraint((de.mosim.mmi.math.MTransform)value);
      }
      break;

    case TRANSLATIONAL_ACCELERATION:
      if (value == null) {
        unsetTranslationalAcceleration();
      } else {
        setTranslationalAcceleration((de.mosim.mmi.math.MVector3)value);
      }
      break;

    case ROTATIONAL_ACCELERATION:
      if (value == null) {
        unsetRotationalAcceleration();
      } else {
        setRotationalAcceleration((de.mosim.mmi.math.MVector3)value);
      }
      break;

    case WEIGHTING_FACTOR:
      if (value == null) {
        unsetWeightingFactor();
      } else {
        setWeightingFactor((java.lang.Double)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case PARENT_OBJECT_ID:
      return getParentObjectID();

    case PARENT_TO_CONSTRAINT:
      return getParentToConstraint();

    case TRANSLATIONAL_ACCELERATION:
      return getTranslationalAcceleration();

    case ROTATIONAL_ACCELERATION:
      return getRotationalAcceleration();

    case WEIGHTING_FACTOR:
      return getWeightingFactor();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case PARENT_OBJECT_ID:
      return isSetParentObjectID();
    case PARENT_TO_CONSTRAINT:
      return isSetParentToConstraint();
    case TRANSLATIONAL_ACCELERATION:
      return isSetTranslationalAcceleration();
    case ROTATIONAL_ACCELERATION:
      return isSetRotationalAcceleration();
    case WEIGHTING_FACTOR:
      return isSetWeightingFactor();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MAccelerationConstraint)
      return this.equals((MAccelerationConstraint)that);
    return false;
  }

  public boolean equals(MAccelerationConstraint that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_ParentObjectID = true && this.isSetParentObjectID();
    boolean that_present_ParentObjectID = true && that.isSetParentObjectID();
    if (this_present_ParentObjectID || that_present_ParentObjectID) {
      if (!(this_present_ParentObjectID && that_present_ParentObjectID))
        return false;
      if (!this.ParentObjectID.equals(that.ParentObjectID))
        return false;
    }

    boolean this_present_ParentToConstraint = true && this.isSetParentToConstraint();
    boolean that_present_ParentToConstraint = true && that.isSetParentToConstraint();
    if (this_present_ParentToConstraint || that_present_ParentToConstraint) {
      if (!(this_present_ParentToConstraint && that_present_ParentToConstraint))
        return false;
      if (!this.ParentToConstraint.equals(that.ParentToConstraint))
        return false;
    }

    boolean this_present_TranslationalAcceleration = true && this.isSetTranslationalAcceleration();
    boolean that_present_TranslationalAcceleration = true && that.isSetTranslationalAcceleration();
    if (this_present_TranslationalAcceleration || that_present_TranslationalAcceleration) {
      if (!(this_present_TranslationalAcceleration && that_present_TranslationalAcceleration))
        return false;
      if (!this.TranslationalAcceleration.equals(that.TranslationalAcceleration))
        return false;
    }

    boolean this_present_RotationalAcceleration = true && this.isSetRotationalAcceleration();
    boolean that_present_RotationalAcceleration = true && that.isSetRotationalAcceleration();
    if (this_present_RotationalAcceleration || that_present_RotationalAcceleration) {
      if (!(this_present_RotationalAcceleration && that_present_RotationalAcceleration))
        return false;
      if (!this.RotationalAcceleration.equals(that.RotationalAcceleration))
        return false;
    }

    boolean this_present_WeightingFactor = true && this.isSetWeightingFactor();
    boolean that_present_WeightingFactor = true && that.isSetWeightingFactor();
    if (this_present_WeightingFactor || that_present_WeightingFactor) {
      if (!(this_present_WeightingFactor && that_present_WeightingFactor))
        return false;
      if (this.WeightingFactor != that.WeightingFactor)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetParentObjectID()) ? 131071 : 524287);
    if (isSetParentObjectID())
      hashCode = hashCode * 8191 + ParentObjectID.hashCode();

    hashCode = hashCode * 8191 + ((isSetParentToConstraint()) ? 131071 : 524287);
    if (isSetParentToConstraint())
      hashCode = hashCode * 8191 + ParentToConstraint.hashCode();

    hashCode = hashCode * 8191 + ((isSetTranslationalAcceleration()) ? 131071 : 524287);
    if (isSetTranslationalAcceleration())
      hashCode = hashCode * 8191 + TranslationalAcceleration.hashCode();

    hashCode = hashCode * 8191 + ((isSetRotationalAcceleration()) ? 131071 : 524287);
    if (isSetRotationalAcceleration())
      hashCode = hashCode * 8191 + RotationalAcceleration.hashCode();

    hashCode = hashCode * 8191 + ((isSetWeightingFactor()) ? 131071 : 524287);
    if (isSetWeightingFactor())
      hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(WeightingFactor);

    return hashCode;
  }

  @Override
  public int compareTo(MAccelerationConstraint other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetParentObjectID()).compareTo(other.isSetParentObjectID());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetParentObjectID()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ParentObjectID, other.ParentObjectID);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetParentToConstraint()).compareTo(other.isSetParentToConstraint());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetParentToConstraint()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ParentToConstraint, other.ParentToConstraint);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetTranslationalAcceleration()).compareTo(other.isSetTranslationalAcceleration());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTranslationalAcceleration()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.TranslationalAcceleration, other.TranslationalAcceleration);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetRotationalAcceleration()).compareTo(other.isSetRotationalAcceleration());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRotationalAcceleration()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.RotationalAcceleration, other.RotationalAcceleration);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetWeightingFactor()).compareTo(other.isSetWeightingFactor());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetWeightingFactor()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.WeightingFactor, other.WeightingFactor);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MAccelerationConstraint(");
    boolean first = true;

    sb.append("ParentObjectID:");
    if (this.ParentObjectID == null) {
      sb.append("null");
    } else {
      sb.append(this.ParentObjectID);
    }
    first = false;
    if (isSetParentToConstraint()) {
      if (!first) sb.append(", ");
      sb.append("ParentToConstraint:");
      if (this.ParentToConstraint == null) {
        sb.append("null");
      } else {
        sb.append(this.ParentToConstraint);
      }
      first = false;
    }
    if (isSetTranslationalAcceleration()) {
      if (!first) sb.append(", ");
      sb.append("TranslationalAcceleration:");
      if (this.TranslationalAcceleration == null) {
        sb.append("null");
      } else {
        sb.append(this.TranslationalAcceleration);
      }
      first = false;
    }
    if (isSetRotationalAcceleration()) {
      if (!first) sb.append(", ");
      sb.append("RotationalAcceleration:");
      if (this.RotationalAcceleration == null) {
        sb.append("null");
      } else {
        sb.append(this.RotationalAcceleration);
      }
      first = false;
    }
    if (isSetWeightingFactor()) {
      if (!first) sb.append(", ");
      sb.append("WeightingFactor:");
      sb.append(this.WeightingFactor);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (ParentObjectID == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'ParentObjectID' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
    if (ParentToConstraint != null) {
      ParentToConstraint.validate();
    }
    if (TranslationalAcceleration != null) {
      TranslationalAcceleration.validate();
    }
    if (RotationalAcceleration != null) {
      RotationalAcceleration.validate();
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

  private static class MAccelerationConstraintStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MAccelerationConstraintStandardScheme getScheme() {
      return new MAccelerationConstraintStandardScheme();
    }
  }

  private static class MAccelerationConstraintStandardScheme extends org.apache.thrift.scheme.StandardScheme<MAccelerationConstraint> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MAccelerationConstraint struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // PARENT_OBJECT_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.ParentObjectID = iprot.readString();
              struct.setParentObjectIDIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // PARENT_TO_CONSTRAINT
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.ParentToConstraint = new de.mosim.mmi.math.MTransform();
              struct.ParentToConstraint.read(iprot);
              struct.setParentToConstraintIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // TRANSLATIONAL_ACCELERATION
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.TranslationalAcceleration = new de.mosim.mmi.math.MVector3();
              struct.TranslationalAcceleration.read(iprot);
              struct.setTranslationalAccelerationIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // ROTATIONAL_ACCELERATION
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.RotationalAcceleration = new de.mosim.mmi.math.MVector3();
              struct.RotationalAcceleration.read(iprot);
              struct.setRotationalAccelerationIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // WEIGHTING_FACTOR
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.WeightingFactor = iprot.readDouble();
              struct.setWeightingFactorIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, MAccelerationConstraint struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.ParentObjectID != null) {
        oprot.writeFieldBegin(PARENT_OBJECT_ID_FIELD_DESC);
        oprot.writeString(struct.ParentObjectID);
        oprot.writeFieldEnd();
      }
      if (struct.ParentToConstraint != null) {
        if (struct.isSetParentToConstraint()) {
          oprot.writeFieldBegin(PARENT_TO_CONSTRAINT_FIELD_DESC);
          struct.ParentToConstraint.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      if (struct.TranslationalAcceleration != null) {
        if (struct.isSetTranslationalAcceleration()) {
          oprot.writeFieldBegin(TRANSLATIONAL_ACCELERATION_FIELD_DESC);
          struct.TranslationalAcceleration.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      if (struct.RotationalAcceleration != null) {
        if (struct.isSetRotationalAcceleration()) {
          oprot.writeFieldBegin(ROTATIONAL_ACCELERATION_FIELD_DESC);
          struct.RotationalAcceleration.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      if (struct.isSetWeightingFactor()) {
        oprot.writeFieldBegin(WEIGHTING_FACTOR_FIELD_DESC);
        oprot.writeDouble(struct.WeightingFactor);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MAccelerationConstraintTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MAccelerationConstraintTupleScheme getScheme() {
      return new MAccelerationConstraintTupleScheme();
    }
  }

  private static class MAccelerationConstraintTupleScheme extends org.apache.thrift.scheme.TupleScheme<MAccelerationConstraint> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MAccelerationConstraint struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeString(struct.ParentObjectID);
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetParentToConstraint()) {
        optionals.set(0);
      }
      if (struct.isSetTranslationalAcceleration()) {
        optionals.set(1);
      }
      if (struct.isSetRotationalAcceleration()) {
        optionals.set(2);
      }
      if (struct.isSetWeightingFactor()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetParentToConstraint()) {
        struct.ParentToConstraint.write(oprot);
      }
      if (struct.isSetTranslationalAcceleration()) {
        struct.TranslationalAcceleration.write(oprot);
      }
      if (struct.isSetRotationalAcceleration()) {
        struct.RotationalAcceleration.write(oprot);
      }
      if (struct.isSetWeightingFactor()) {
        oprot.writeDouble(struct.WeightingFactor);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MAccelerationConstraint struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.ParentObjectID = iprot.readString();
      struct.setParentObjectIDIsSet(true);
      java.util.BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.ParentToConstraint = new de.mosim.mmi.math.MTransform();
        struct.ParentToConstraint.read(iprot);
        struct.setParentToConstraintIsSet(true);
      }
      if (incoming.get(1)) {
        struct.TranslationalAcceleration = new de.mosim.mmi.math.MVector3();
        struct.TranslationalAcceleration.read(iprot);
        struct.setTranslationalAccelerationIsSet(true);
      }
      if (incoming.get(2)) {
        struct.RotationalAcceleration = new de.mosim.mmi.math.MVector3();
        struct.RotationalAcceleration.read(iprot);
        struct.setRotationalAccelerationIsSet(true);
      }
      if (incoming.get(3)) {
        struct.WeightingFactor = iprot.readDouble();
        struct.setWeightingFactorIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

