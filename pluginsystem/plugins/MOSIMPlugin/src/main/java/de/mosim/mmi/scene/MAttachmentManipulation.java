/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.scene;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2023-01-11")
public class MAttachmentManipulation implements org.apache.thrift.TBase<MAttachmentManipulation, MAttachmentManipulation._Fields>, java.io.Serializable, Cloneable, Comparable<MAttachmentManipulation> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MAttachmentManipulation");

  private static final org.apache.thrift.protocol.TField PARENT_FIELD_DESC = new org.apache.thrift.protocol.TField("Parent", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField CHILD_FIELD_DESC = new org.apache.thrift.protocol.TField("Child", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField ADD_REMOVE_FIELD_DESC = new org.apache.thrift.protocol.TField("AddRemove", org.apache.thrift.protocol.TType.BOOL, (short)3);
  private static final org.apache.thrift.protocol.TField TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("Type", org.apache.thrift.protocol.TType.STRING, (short)4);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MAttachmentManipulationStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MAttachmentManipulationTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.lang.String Parent; // required
  public @org.apache.thrift.annotation.Nullable java.lang.String Child; // required
  public boolean AddRemove; // required
  public @org.apache.thrift.annotation.Nullable java.lang.String Type; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    PARENT((short)1, "Parent"),
    CHILD((short)2, "Child"),
    ADD_REMOVE((short)3, "AddRemove"),
    TYPE((short)4, "Type");

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
        case 1: // PARENT
          return PARENT;
        case 2: // CHILD
          return CHILD;
        case 3: // ADD_REMOVE
          return ADD_REMOVE;
        case 4: // TYPE
          return TYPE;
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
  private static final int __ADDREMOVE_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.TYPE};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.PARENT, new org.apache.thrift.meta_data.FieldMetaData("Parent", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.CHILD, new org.apache.thrift.meta_data.FieldMetaData("Child", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.ADD_REMOVE, new org.apache.thrift.meta_data.FieldMetaData("AddRemove", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    tmpMap.put(_Fields.TYPE, new org.apache.thrift.meta_data.FieldMetaData("Type", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MAttachmentManipulation.class, metaDataMap);
  }

  public MAttachmentManipulation() {
  }

  public MAttachmentManipulation(
    java.lang.String Parent,
    java.lang.String Child,
    boolean AddRemove)
  {
    this();
    this.Parent = Parent;
    this.Child = Child;
    this.AddRemove = AddRemove;
    setAddRemoveIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MAttachmentManipulation(MAttachmentManipulation other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetParent()) {
      this.Parent = other.Parent;
    }
    if (other.isSetChild()) {
      this.Child = other.Child;
    }
    this.AddRemove = other.AddRemove;
    if (other.isSetType()) {
      this.Type = other.Type;
    }
  }

  public MAttachmentManipulation deepCopy() {
    return new MAttachmentManipulation(this);
  }

  @Override
  public void clear() {
    this.Parent = null;
    this.Child = null;
    setAddRemoveIsSet(false);
    this.AddRemove = false;
    this.Type = null;
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getParent() {
    return this.Parent;
  }

  public MAttachmentManipulation setParent(@org.apache.thrift.annotation.Nullable java.lang.String Parent) {
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

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getChild() {
    return this.Child;
  }

  public MAttachmentManipulation setChild(@org.apache.thrift.annotation.Nullable java.lang.String Child) {
    this.Child = Child;
    return this;
  }

  public void unsetChild() {
    this.Child = null;
  }

  /** Returns true if field Child is set (has been assigned a value) and false otherwise */
  public boolean isSetChild() {
    return this.Child != null;
  }

  public void setChildIsSet(boolean value) {
    if (!value) {
      this.Child = null;
    }
  }

  public boolean isAddRemove() {
    return this.AddRemove;
  }

  public MAttachmentManipulation setAddRemove(boolean AddRemove) {
    this.AddRemove = AddRemove;
    setAddRemoveIsSet(true);
    return this;
  }

  public void unsetAddRemove() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __ADDREMOVE_ISSET_ID);
  }

  /** Returns true if field AddRemove is set (has been assigned a value) and false otherwise */
  public boolean isSetAddRemove() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __ADDREMOVE_ISSET_ID);
  }

  public void setAddRemoveIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __ADDREMOVE_ISSET_ID, value);
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getType() {
    return this.Type;
  }

  public MAttachmentManipulation setType(@org.apache.thrift.annotation.Nullable java.lang.String Type) {
    this.Type = Type;
    return this;
  }

  public void unsetType() {
    this.Type = null;
  }

  /** Returns true if field Type is set (has been assigned a value) and false otherwise */
  public boolean isSetType() {
    return this.Type != null;
  }

  public void setTypeIsSet(boolean value) {
    if (!value) {
      this.Type = null;
    }
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case PARENT:
      if (value == null) {
        unsetParent();
      } else {
        setParent((java.lang.String)value);
      }
      break;

    case CHILD:
      if (value == null) {
        unsetChild();
      } else {
        setChild((java.lang.String)value);
      }
      break;

    case ADD_REMOVE:
      if (value == null) {
        unsetAddRemove();
      } else {
        setAddRemove((java.lang.Boolean)value);
      }
      break;

    case TYPE:
      if (value == null) {
        unsetType();
      } else {
        setType((java.lang.String)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case PARENT:
      return getParent();

    case CHILD:
      return getChild();

    case ADD_REMOVE:
      return isAddRemove();

    case TYPE:
      return getType();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case PARENT:
      return isSetParent();
    case CHILD:
      return isSetChild();
    case ADD_REMOVE:
      return isSetAddRemove();
    case TYPE:
      return isSetType();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MAttachmentManipulation)
      return this.equals((MAttachmentManipulation)that);
    return false;
  }

  public boolean equals(MAttachmentManipulation that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_Parent = true && this.isSetParent();
    boolean that_present_Parent = true && that.isSetParent();
    if (this_present_Parent || that_present_Parent) {
      if (!(this_present_Parent && that_present_Parent))
        return false;
      if (!this.Parent.equals(that.Parent))
        return false;
    }

    boolean this_present_Child = true && this.isSetChild();
    boolean that_present_Child = true && that.isSetChild();
    if (this_present_Child || that_present_Child) {
      if (!(this_present_Child && that_present_Child))
        return false;
      if (!this.Child.equals(that.Child))
        return false;
    }

    boolean this_present_AddRemove = true;
    boolean that_present_AddRemove = true;
    if (this_present_AddRemove || that_present_AddRemove) {
      if (!(this_present_AddRemove && that_present_AddRemove))
        return false;
      if (this.AddRemove != that.AddRemove)
        return false;
    }

    boolean this_present_Type = true && this.isSetType();
    boolean that_present_Type = true && that.isSetType();
    if (this_present_Type || that_present_Type) {
      if (!(this_present_Type && that_present_Type))
        return false;
      if (!this.Type.equals(that.Type))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetParent()) ? 131071 : 524287);
    if (isSetParent())
      hashCode = hashCode * 8191 + Parent.hashCode();

    hashCode = hashCode * 8191 + ((isSetChild()) ? 131071 : 524287);
    if (isSetChild())
      hashCode = hashCode * 8191 + Child.hashCode();

    hashCode = hashCode * 8191 + ((AddRemove) ? 131071 : 524287);

    hashCode = hashCode * 8191 + ((isSetType()) ? 131071 : 524287);
    if (isSetType())
      hashCode = hashCode * 8191 + Type.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(MAttachmentManipulation other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

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
    lastComparison = java.lang.Boolean.valueOf(isSetChild()).compareTo(other.isSetChild());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetChild()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Child, other.Child);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetAddRemove()).compareTo(other.isSetAddRemove());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAddRemove()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.AddRemove, other.AddRemove);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetType()).compareTo(other.isSetType());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetType()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Type, other.Type);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MAttachmentManipulation(");
    boolean first = true;

    sb.append("Parent:");
    if (this.Parent == null) {
      sb.append("null");
    } else {
      sb.append(this.Parent);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("Child:");
    if (this.Child == null) {
      sb.append("null");
    } else {
      sb.append(this.Child);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("AddRemove:");
    sb.append(this.AddRemove);
    first = false;
    if (isSetType()) {
      if (!first) sb.append(", ");
      sb.append("Type:");
      if (this.Type == null) {
        sb.append("null");
      } else {
        sb.append(this.Type);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (Parent == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'Parent' was not present! Struct: " + toString());
    }
    if (Child == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'Child' was not present! Struct: " + toString());
    }
    // alas, we cannot check 'AddRemove' because it's a primitive and you chose the non-beans generator.
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

  private static class MAttachmentManipulationStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MAttachmentManipulationStandardScheme getScheme() {
      return new MAttachmentManipulationStandardScheme();
    }
  }

  private static class MAttachmentManipulationStandardScheme extends org.apache.thrift.scheme.StandardScheme<MAttachmentManipulation> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MAttachmentManipulation struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // PARENT
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.Parent = iprot.readString();
              struct.setParentIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // CHILD
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.Child = iprot.readString();
              struct.setChildIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // ADD_REMOVE
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.AddRemove = iprot.readBool();
              struct.setAddRemoveIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // TYPE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.Type = iprot.readString();
              struct.setTypeIsSet(true);
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
      if (!struct.isSetAddRemove()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'AddRemove' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, MAttachmentManipulation struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.Parent != null) {
        oprot.writeFieldBegin(PARENT_FIELD_DESC);
        oprot.writeString(struct.Parent);
        oprot.writeFieldEnd();
      }
      if (struct.Child != null) {
        oprot.writeFieldBegin(CHILD_FIELD_DESC);
        oprot.writeString(struct.Child);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(ADD_REMOVE_FIELD_DESC);
      oprot.writeBool(struct.AddRemove);
      oprot.writeFieldEnd();
      if (struct.Type != null) {
        if (struct.isSetType()) {
          oprot.writeFieldBegin(TYPE_FIELD_DESC);
          oprot.writeString(struct.Type);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MAttachmentManipulationTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MAttachmentManipulationTupleScheme getScheme() {
      return new MAttachmentManipulationTupleScheme();
    }
  }

  private static class MAttachmentManipulationTupleScheme extends org.apache.thrift.scheme.TupleScheme<MAttachmentManipulation> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MAttachmentManipulation struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeString(struct.Parent);
      oprot.writeString(struct.Child);
      oprot.writeBool(struct.AddRemove);
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetType()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetType()) {
        oprot.writeString(struct.Type);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MAttachmentManipulation struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.Parent = iprot.readString();
      struct.setParentIsSet(true);
      struct.Child = iprot.readString();
      struct.setChildIsSet(true);
      struct.AddRemove = iprot.readBool();
      struct.setAddRemoveIsSet(true);
      java.util.BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        struct.Type = iprot.readString();
        struct.setTypeIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

