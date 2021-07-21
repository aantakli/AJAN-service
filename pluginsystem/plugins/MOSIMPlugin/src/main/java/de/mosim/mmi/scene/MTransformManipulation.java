/**
 * Autogenerated by Thrift Compiler (0.12.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.scene;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.12.0)", date = "2021-06-29")
public class MTransformManipulation implements org.apache.thrift.TBase<MTransformManipulation, MTransformManipulation._Fields>, java.io.Serializable, Cloneable, Comparable<MTransformManipulation> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MTransformManipulation");

  private static final org.apache.thrift.protocol.TField TARGET_FIELD_DESC = new org.apache.thrift.protocol.TField("Target", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField POSITION_FIELD_DESC = new org.apache.thrift.protocol.TField("Position", org.apache.thrift.protocol.TType.STRUCT, (short)2);
  private static final org.apache.thrift.protocol.TField ROTATION_FIELD_DESC = new org.apache.thrift.protocol.TField("Rotation", org.apache.thrift.protocol.TType.STRUCT, (short)3);
  private static final org.apache.thrift.protocol.TField PARENT_FIELD_DESC = new org.apache.thrift.protocol.TField("Parent", org.apache.thrift.protocol.TType.STRING, (short)4);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MTransformManipulationStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MTransformManipulationTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.lang.String Target; // required
  public @org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MVector3 Position; // optional
  public @org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MQuaternion Rotation; // optional
  public @org.apache.thrift.annotation.Nullable java.lang.String Parent; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    TARGET((short)1, "Target"),
    POSITION((short)2, "Position"),
    ROTATION((short)3, "Rotation"),
    PARENT((short)4, "Parent");

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
        case 1: // TARGET
          return TARGET;
        case 2: // POSITION
          return POSITION;
        case 3: // ROTATION
          return ROTATION;
        case 4: // PARENT
          return PARENT;
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
  private static final _Fields optionals[] = {_Fields.POSITION,_Fields.ROTATION,_Fields.PARENT};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TARGET, new org.apache.thrift.meta_data.FieldMetaData("Target", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.POSITION, new org.apache.thrift.meta_data.FieldMetaData("Position", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.math.MVector3.class)));
    tmpMap.put(_Fields.ROTATION, new org.apache.thrift.meta_data.FieldMetaData("Rotation", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.math.MQuaternion.class)));
    tmpMap.put(_Fields.PARENT, new org.apache.thrift.meta_data.FieldMetaData("Parent", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MTransformManipulation.class, metaDataMap);
  }

  public MTransformManipulation() {
  }

  public MTransformManipulation(
    java.lang.String Target)
  {
    this();
    this.Target = Target;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MTransformManipulation(MTransformManipulation other) {
    if (other.isSetTarget()) {
      this.Target = other.Target;
    }
    if (other.isSetPosition()) {
      this.Position = new de.mosim.mmi.math.MVector3(other.Position);
    }
    if (other.isSetRotation()) {
      this.Rotation = new de.mosim.mmi.math.MQuaternion(other.Rotation);
    }
    if (other.isSetParent()) {
      this.Parent = other.Parent;
    }
  }

  public MTransformManipulation deepCopy() {
    return new MTransformManipulation(this);
  }

  @Override
  public void clear() {
    this.Target = null;
    this.Position = null;
    this.Rotation = null;
    this.Parent = null;
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getTarget() {
    return this.Target;
  }

  public MTransformManipulation setTarget(@org.apache.thrift.annotation.Nullable java.lang.String Target) {
    this.Target = Target;
    return this;
  }

  public void unsetTarget() {
    this.Target = null;
  }

  /** Returns true if field Target is set (has been assigned a value) and false otherwise */
  public boolean isSetTarget() {
    return this.Target != null;
  }

  public void setTargetIsSet(boolean value) {
    if (!value) {
      this.Target = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public de.mosim.mmi.math.MVector3 getPosition() {
    return this.Position;
  }

  public MTransformManipulation setPosition(@org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MVector3 Position) {
    this.Position = Position;
    return this;
  }

  public void unsetPosition() {
    this.Position = null;
  }

  /** Returns true if field Position is set (has been assigned a value) and false otherwise */
  public boolean isSetPosition() {
    return this.Position != null;
  }

  public void setPositionIsSet(boolean value) {
    if (!value) {
      this.Position = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public de.mosim.mmi.math.MQuaternion getRotation() {
    return this.Rotation;
  }

  public MTransformManipulation setRotation(@org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MQuaternion Rotation) {
    this.Rotation = Rotation;
    return this;
  }

  public void unsetRotation() {
    this.Rotation = null;
  }

  /** Returns true if field Rotation is set (has been assigned a value) and false otherwise */
  public boolean isSetRotation() {
    return this.Rotation != null;
  }

  public void setRotationIsSet(boolean value) {
    if (!value) {
      this.Rotation = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getParent() {
    return this.Parent;
  }

  public MTransformManipulation setParent(@org.apache.thrift.annotation.Nullable java.lang.String Parent) {
    this.Parent = Parent;
    return this;
  }

  public void unsetParent() {
    this.Parent = null;
  }

  /** Returns true if field Parent is set (has been assigned a value) and false otherwise */
  public boolean isSetParent() {
    return this.Parent != null;
  }

  public void setParentIsSet(boolean value) {
    if (!value) {
      this.Parent = null;
    }
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case TARGET:
      if (value == null) {
        unsetTarget();
      } else {
        setTarget((java.lang.String)value);
      }
      break;

    case POSITION:
      if (value == null) {
        unsetPosition();
      } else {
        setPosition((de.mosim.mmi.math.MVector3)value);
      }
      break;

    case ROTATION:
      if (value == null) {
        unsetRotation();
      } else {
        setRotation((de.mosim.mmi.math.MQuaternion)value);
      }
      break;

    case PARENT:
      if (value == null) {
        unsetParent();
      } else {
        setParent((java.lang.String)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case TARGET:
      return getTarget();

    case POSITION:
      return getPosition();

    case ROTATION:
      return getRotation();

    case PARENT:
      return getParent();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case TARGET:
      return isSetTarget();
    case POSITION:
      return isSetPosition();
    case ROTATION:
      return isSetRotation();
    case PARENT:
      return isSetParent();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MTransformManipulation)
      return this.equals((MTransformManipulation)that);
    return false;
  }

  public boolean equals(MTransformManipulation that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_Target = true && this.isSetTarget();
    boolean that_present_Target = true && that.isSetTarget();
    if (this_present_Target || that_present_Target) {
      if (!(this_present_Target && that_present_Target))
        return false;
      if (!this.Target.equals(that.Target))
        return false;
    }

    boolean this_present_Position = true && this.isSetPosition();
    boolean that_present_Position = true && that.isSetPosition();
    if (this_present_Position || that_present_Position) {
      if (!(this_present_Position && that_present_Position))
        return false;
      if (!this.Position.equals(that.Position))
        return false;
    }

    boolean this_present_Rotation = true && this.isSetRotation();
    boolean that_present_Rotation = true && that.isSetRotation();
    if (this_present_Rotation || that_present_Rotation) {
      if (!(this_present_Rotation && that_present_Rotation))
        return false;
      if (!this.Rotation.equals(that.Rotation))
        return false;
    }

    boolean this_present_Parent = true && this.isSetParent();
    boolean that_present_Parent = true && that.isSetParent();
    if (this_present_Parent || that_present_Parent) {
      if (!(this_present_Parent && that_present_Parent))
        return false;
      if (!this.Parent.equals(that.Parent))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetTarget()) ? 131071 : 524287);
    if (isSetTarget())
      hashCode = hashCode * 8191 + Target.hashCode();

    hashCode = hashCode * 8191 + ((isSetPosition()) ? 131071 : 524287);
    if (isSetPosition())
      hashCode = hashCode * 8191 + Position.hashCode();

    hashCode = hashCode * 8191 + ((isSetRotation()) ? 131071 : 524287);
    if (isSetRotation())
      hashCode = hashCode * 8191 + Rotation.hashCode();

    hashCode = hashCode * 8191 + ((isSetParent()) ? 131071 : 524287);
    if (isSetParent())
      hashCode = hashCode * 8191 + Parent.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(MTransformManipulation other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetTarget()).compareTo(other.isSetTarget());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTarget()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Target, other.Target);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetPosition()).compareTo(other.isSetPosition());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPosition()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Position, other.Position);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetRotation()).compareTo(other.isSetRotation());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRotation()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Rotation, other.Rotation);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetParent()).compareTo(other.isSetParent());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetParent()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Parent, other.Parent);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MTransformManipulation(");
    boolean first = true;

    sb.append("Target:");
    if (this.Target == null) {
      sb.append("null");
    } else {
      sb.append(this.Target);
    }
    first = false;
    if (isSetPosition()) {
      if (!first) sb.append(", ");
      sb.append("Position:");
      if (this.Position == null) {
        sb.append("null");
      } else {
        sb.append(this.Position);
      }
      first = false;
    }
    if (isSetRotation()) {
      if (!first) sb.append(", ");
      sb.append("Rotation:");
      if (this.Rotation == null) {
        sb.append("null");
      } else {
        sb.append(this.Rotation);
      }
      first = false;
    }
    if (isSetParent()) {
      if (!first) sb.append(", ");
      sb.append("Parent:");
      if (this.Parent == null) {
        sb.append("null");
      } else {
        sb.append(this.Parent);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (Target == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'Target' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
    if (Position != null) {
      Position.validate();
    }
    if (Rotation != null) {
      Rotation.validate();
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

  private static class MTransformManipulationStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MTransformManipulationStandardScheme getScheme() {
      return new MTransformManipulationStandardScheme();
    }
  }

  private static class MTransformManipulationStandardScheme extends org.apache.thrift.scheme.StandardScheme<MTransformManipulation> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MTransformManipulation struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TARGET
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.Target = iprot.readString();
              struct.setTargetIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // POSITION
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.Position = new de.mosim.mmi.math.MVector3();
              struct.Position.read(iprot);
              struct.setPositionIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // ROTATION
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.Rotation = new de.mosim.mmi.math.MQuaternion();
              struct.Rotation.read(iprot);
              struct.setRotationIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // PARENT
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.Parent = iprot.readString();
              struct.setParentIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, MTransformManipulation struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.Target != null) {
        oprot.writeFieldBegin(TARGET_FIELD_DESC);
        oprot.writeString(struct.Target);
        oprot.writeFieldEnd();
      }
      if (struct.Position != null) {
        if (struct.isSetPosition()) {
          oprot.writeFieldBegin(POSITION_FIELD_DESC);
          struct.Position.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      if (struct.Rotation != null) {
        if (struct.isSetRotation()) {
          oprot.writeFieldBegin(ROTATION_FIELD_DESC);
          struct.Rotation.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      if (struct.Parent != null) {
        if (struct.isSetParent()) {
          oprot.writeFieldBegin(PARENT_FIELD_DESC);
          oprot.writeString(struct.Parent);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MTransformManipulationTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MTransformManipulationTupleScheme getScheme() {
      return new MTransformManipulationTupleScheme();
    }
  }

  private static class MTransformManipulationTupleScheme extends org.apache.thrift.scheme.TupleScheme<MTransformManipulation> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MTransformManipulation struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeString(struct.Target);
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetPosition()) {
        optionals.set(0);
      }
      if (struct.isSetRotation()) {
        optionals.set(1);
      }
      if (struct.isSetParent()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetPosition()) {
        struct.Position.write(oprot);
      }
      if (struct.isSetRotation()) {
        struct.Rotation.write(oprot);
      }
      if (struct.isSetParent()) {
        oprot.writeString(struct.Parent);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MTransformManipulation struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.Target = iprot.readString();
      struct.setTargetIsSet(true);
      java.util.BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.Position = new de.mosim.mmi.math.MVector3();
        struct.Position.read(iprot);
        struct.setPositionIsSet(true);
      }
      if (incoming.get(1)) {
        struct.Rotation = new de.mosim.mmi.math.MQuaternion();
        struct.Rotation.read(iprot);
        struct.setRotationIsSet(true);
      }
      if (incoming.get(2)) {
        struct.Parent = iprot.readString();
        struct.setParentIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

