/**
 * Autogenerated by Thrift Compiler (0.12.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.scene;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.12.0)", date = "2021-06-29")
public class MTransformUpdate implements org.apache.thrift.TBase<MTransformUpdate, MTransformUpdate._Fields>, java.io.Serializable, Cloneable, Comparable<MTransformUpdate> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MTransformUpdate");

  private static final org.apache.thrift.protocol.TField POSITION_FIELD_DESC = new org.apache.thrift.protocol.TField("Position", org.apache.thrift.protocol.TType.LIST, (short)1);
  private static final org.apache.thrift.protocol.TField ROTATION_FIELD_DESC = new org.apache.thrift.protocol.TField("Rotation", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField PARENT_FIELD_DESC = new org.apache.thrift.protocol.TField("Parent", org.apache.thrift.protocol.TType.STRING, (short)3);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MTransformUpdateStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MTransformUpdateTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.util.List<java.lang.Double> Position; // optional
  public @org.apache.thrift.annotation.Nullable java.util.List<java.lang.Double> Rotation; // optional
  public @org.apache.thrift.annotation.Nullable java.lang.String Parent; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    POSITION((short)1, "Position"),
    ROTATION((short)2, "Rotation"),
    PARENT((short)3, "Parent");

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
        case 1: // POSITION
          return POSITION;
        case 2: // ROTATION
          return ROTATION;
        case 3: // PARENT
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
    tmpMap.put(_Fields.POSITION, new org.apache.thrift.meta_data.FieldMetaData("Position", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE))));
    tmpMap.put(_Fields.ROTATION, new org.apache.thrift.meta_data.FieldMetaData("Rotation", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE))));
    tmpMap.put(_Fields.PARENT, new org.apache.thrift.meta_data.FieldMetaData("Parent", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MTransformUpdate.class, metaDataMap);
  }

  public MTransformUpdate() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MTransformUpdate(MTransformUpdate other) {
    if (other.isSetPosition()) {
      java.util.List<java.lang.Double> __this__Position = new java.util.ArrayList<java.lang.Double>(other.Position);
      this.Position = __this__Position;
    }
    if (other.isSetRotation()) {
      java.util.List<java.lang.Double> __this__Rotation = new java.util.ArrayList<java.lang.Double>(other.Rotation);
      this.Rotation = __this__Rotation;
    }
    if (other.isSetParent()) {
      this.Parent = other.Parent;
    }
  }

  public MTransformUpdate deepCopy() {
    return new MTransformUpdate(this);
  }

  @Override
  public void clear() {
    this.Position = null;
    this.Rotation = null;
    this.Parent = null;
  }

  public int getPositionSize() {
    return (this.Position == null) ? 0 : this.Position.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<java.lang.Double> getPositionIterator() {
    return (this.Position == null) ? null : this.Position.iterator();
  }

  public void addToPosition(double elem) {
    if (this.Position == null) {
      this.Position = new java.util.ArrayList<java.lang.Double>();
    }
    this.Position.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<java.lang.Double> getPosition() {
    return this.Position;
  }

  public MTransformUpdate setPosition(@org.apache.thrift.annotation.Nullable java.util.List<java.lang.Double> Position) {
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

  public int getRotationSize() {
    return (this.Rotation == null) ? 0 : this.Rotation.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<java.lang.Double> getRotationIterator() {
    return (this.Rotation == null) ? null : this.Rotation.iterator();
  }

  public void addToRotation(double elem) {
    if (this.Rotation == null) {
      this.Rotation = new java.util.ArrayList<java.lang.Double>();
    }
    this.Rotation.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<java.lang.Double> getRotation() {
    return this.Rotation;
  }

  public MTransformUpdate setRotation(@org.apache.thrift.annotation.Nullable java.util.List<java.lang.Double> Rotation) {
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

  public MTransformUpdate setParent(@org.apache.thrift.annotation.Nullable java.lang.String Parent) {
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
    case POSITION:
      if (value == null) {
        unsetPosition();
      } else {
        setPosition((java.util.List<java.lang.Double>)value);
      }
      break;

    case ROTATION:
      if (value == null) {
        unsetRotation();
      } else {
        setRotation((java.util.List<java.lang.Double>)value);
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
    if (that instanceof MTransformUpdate)
      return this.equals((MTransformUpdate)that);
    return false;
  }

  public boolean equals(MTransformUpdate that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

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
  public int compareTo(MTransformUpdate other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MTransformUpdate(");
    boolean first = true;

    if (isSetPosition()) {
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
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class MTransformUpdateStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MTransformUpdateStandardScheme getScheme() {
      return new MTransformUpdateStandardScheme();
    }
  }

  private static class MTransformUpdateStandardScheme extends org.apache.thrift.scheme.StandardScheme<MTransformUpdate> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MTransformUpdate struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // POSITION
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                struct.Position = new java.util.ArrayList<java.lang.Double>(_list0.size);
                double _elem1;
                for (int _i2 = 0; _i2 < _list0.size; ++_i2)
                {
                  _elem1 = iprot.readDouble();
                  struct.Position.add(_elem1);
                }
                iprot.readListEnd();
              }
              struct.setPositionIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // ROTATION
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list3 = iprot.readListBegin();
                struct.Rotation = new java.util.ArrayList<java.lang.Double>(_list3.size);
                double _elem4;
                for (int _i5 = 0; _i5 < _list3.size; ++_i5)
                {
                  _elem4 = iprot.readDouble();
                  struct.Rotation.add(_elem4);
                }
                iprot.readListEnd();
              }
              struct.setRotationIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // PARENT
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, MTransformUpdate struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.Position != null) {
        if (struct.isSetPosition()) {
          oprot.writeFieldBegin(POSITION_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.DOUBLE, struct.Position.size()));
            for (double _iter6 : struct.Position)
            {
              oprot.writeDouble(_iter6);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.Rotation != null) {
        if (struct.isSetRotation()) {
          oprot.writeFieldBegin(ROTATION_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.DOUBLE, struct.Rotation.size()));
            for (double _iter7 : struct.Rotation)
            {
              oprot.writeDouble(_iter7);
            }
            oprot.writeListEnd();
          }
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

  private static class MTransformUpdateTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MTransformUpdateTupleScheme getScheme() {
      return new MTransformUpdateTupleScheme();
    }
  }

  private static class MTransformUpdateTupleScheme extends org.apache.thrift.scheme.TupleScheme<MTransformUpdate> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MTransformUpdate struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
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
        {
          oprot.writeI32(struct.Position.size());
          for (double _iter8 : struct.Position)
          {
            oprot.writeDouble(_iter8);
          }
        }
      }
      if (struct.isSetRotation()) {
        {
          oprot.writeI32(struct.Rotation.size());
          for (double _iter9 : struct.Rotation)
          {
            oprot.writeDouble(_iter9);
          }
        }
      }
      if (struct.isSetParent()) {
        oprot.writeString(struct.Parent);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MTransformUpdate struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list10 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.DOUBLE, iprot.readI32());
          struct.Position = new java.util.ArrayList<java.lang.Double>(_list10.size);
          double _elem11;
          for (int _i12 = 0; _i12 < _list10.size; ++_i12)
          {
            _elem11 = iprot.readDouble();
            struct.Position.add(_elem11);
          }
        }
        struct.setPositionIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list13 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.DOUBLE, iprot.readI32());
          struct.Rotation = new java.util.ArrayList<java.lang.Double>(_list13.size);
          double _elem14;
          for (int _i15 = 0; _i15 < _list13.size; ++_i15)
          {
            _elem14 = iprot.readDouble();
            struct.Rotation.add(_elem14);
          }
        }
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

