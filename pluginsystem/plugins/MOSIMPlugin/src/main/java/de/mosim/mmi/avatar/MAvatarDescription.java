/**
 * Autogenerated by Thrift Compiler (0.12.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.avatar;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.12.0)", date = "2021-06-29")
public class MAvatarDescription implements org.apache.thrift.TBase<MAvatarDescription, MAvatarDescription._Fields>, java.io.Serializable, Cloneable, Comparable<MAvatarDescription> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MAvatarDescription");

  private static final org.apache.thrift.protocol.TField AVATAR_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("AvatarID", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField ZERO_POSTURE_FIELD_DESC = new org.apache.thrift.protocol.TField("ZeroPosture", org.apache.thrift.protocol.TType.STRUCT, (short)2);
  private static final org.apache.thrift.protocol.TField PROPERTIES_FIELD_DESC = new org.apache.thrift.protocol.TField("Properties", org.apache.thrift.protocol.TType.MAP, (short)3);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MAvatarDescriptionStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MAvatarDescriptionTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.lang.String AvatarID; // required
  public @org.apache.thrift.annotation.Nullable MAvatarPosture ZeroPosture; // required
  public @org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,java.lang.String> Properties; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    AVATAR_ID((short)1, "AvatarID"),
    ZERO_POSTURE((short)2, "ZeroPosture"),
    PROPERTIES((short)3, "Properties");

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
        case 1: // AVATAR_ID
          return AVATAR_ID;
        case 2: // ZERO_POSTURE
          return ZERO_POSTURE;
        case 3: // PROPERTIES
          return PROPERTIES;
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
  private static final _Fields optionals[] = {_Fields.PROPERTIES};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.AVATAR_ID, new org.apache.thrift.meta_data.FieldMetaData("AvatarID", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.ZERO_POSTURE, new org.apache.thrift.meta_data.FieldMetaData("ZeroPosture", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, MAvatarPosture.class)));
    tmpMap.put(_Fields.PROPERTIES, new org.apache.thrift.meta_data.FieldMetaData("Properties", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MAvatarDescription.class, metaDataMap);
  }

  public MAvatarDescription() {
  }

  public MAvatarDescription(
    java.lang.String AvatarID,
    MAvatarPosture ZeroPosture)
  {
    this();
    this.AvatarID = AvatarID;
    this.ZeroPosture = ZeroPosture;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MAvatarDescription(MAvatarDescription other) {
    if (other.isSetAvatarID()) {
      this.AvatarID = other.AvatarID;
    }
    if (other.isSetZeroPosture()) {
      this.ZeroPosture = new MAvatarPosture(other.ZeroPosture);
    }
    if (other.isSetProperties()) {
      java.util.Map<java.lang.String,java.lang.String> __this__Properties = new java.util.HashMap<java.lang.String,java.lang.String>(other.Properties);
      this.Properties = __this__Properties;
    }
  }

  public MAvatarDescription deepCopy() {
    return new MAvatarDescription(this);
  }

  @Override
  public void clear() {
    this.AvatarID = null;
    this.ZeroPosture = null;
    this.Properties = null;
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getAvatarID() {
    return this.AvatarID;
  }

  public MAvatarDescription setAvatarID(@org.apache.thrift.annotation.Nullable java.lang.String AvatarID) {
    this.AvatarID = AvatarID;
    return this;
  }

  public void unsetAvatarID() {
    this.AvatarID = null;
  }

  /** Returns true if field AvatarID is set (has been assigned a value) and false otherwise */
  public boolean isSetAvatarID() {
    return this.AvatarID != null;
  }

  public void setAvatarIDIsSet(boolean value) {
    if (!value) {
      this.AvatarID = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public MAvatarPosture getZeroPosture() {
    return this.ZeroPosture;
  }

  public MAvatarDescription setZeroPosture(@org.apache.thrift.annotation.Nullable MAvatarPosture ZeroPosture) {
    this.ZeroPosture = ZeroPosture;
    return this;
  }

  public void unsetZeroPosture() {
    this.ZeroPosture = null;
  }

  /** Returns true if field ZeroPosture is set (has been assigned a value) and false otherwise */
  public boolean isSetZeroPosture() {
    return this.ZeroPosture != null;
  }

  public void setZeroPostureIsSet(boolean value) {
    if (!value) {
      this.ZeroPosture = null;
    }
  }

  public int getPropertiesSize() {
    return (this.Properties == null) ? 0 : this.Properties.size();
  }

  public void putToProperties(java.lang.String key, java.lang.String val) {
    if (this.Properties == null) {
      this.Properties = new java.util.HashMap<java.lang.String,java.lang.String>();
    }
    this.Properties.put(key, val);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Map<java.lang.String,java.lang.String> getProperties() {
    return this.Properties;
  }

  public MAvatarDescription setProperties(@org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,java.lang.String> Properties) {
    this.Properties = Properties;
    return this;
  }

  public void unsetProperties() {
    this.Properties = null;
  }

  /** Returns true if field Properties is set (has been assigned a value) and false otherwise */
  public boolean isSetProperties() {
    return this.Properties != null;
  }

  public void setPropertiesIsSet(boolean value) {
    if (!value) {
      this.Properties = null;
    }
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case AVATAR_ID:
      if (value == null) {
        unsetAvatarID();
      } else {
        setAvatarID((java.lang.String)value);
      }
      break;

    case ZERO_POSTURE:
      if (value == null) {
        unsetZeroPosture();
      } else {
        setZeroPosture((MAvatarPosture)value);
      }
      break;

    case PROPERTIES:
      if (value == null) {
        unsetProperties();
      } else {
        setProperties((java.util.Map<java.lang.String,java.lang.String>)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case AVATAR_ID:
      return getAvatarID();

    case ZERO_POSTURE:
      return getZeroPosture();

    case PROPERTIES:
      return getProperties();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case AVATAR_ID:
      return isSetAvatarID();
    case ZERO_POSTURE:
      return isSetZeroPosture();
    case PROPERTIES:
      return isSetProperties();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MAvatarDescription)
      return this.equals((MAvatarDescription)that);
    return false;
  }

  public boolean equals(MAvatarDescription that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_AvatarID = true && this.isSetAvatarID();
    boolean that_present_AvatarID = true && that.isSetAvatarID();
    if (this_present_AvatarID || that_present_AvatarID) {
      if (!(this_present_AvatarID && that_present_AvatarID))
        return false;
      if (!this.AvatarID.equals(that.AvatarID))
        return false;
    }

    boolean this_present_ZeroPosture = true && this.isSetZeroPosture();
    boolean that_present_ZeroPosture = true && that.isSetZeroPosture();
    if (this_present_ZeroPosture || that_present_ZeroPosture) {
      if (!(this_present_ZeroPosture && that_present_ZeroPosture))
        return false;
      if (!this.ZeroPosture.equals(that.ZeroPosture))
        return false;
    }

    boolean this_present_Properties = true && this.isSetProperties();
    boolean that_present_Properties = true && that.isSetProperties();
    if (this_present_Properties || that_present_Properties) {
      if (!(this_present_Properties && that_present_Properties))
        return false;
      if (!this.Properties.equals(that.Properties))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetAvatarID()) ? 131071 : 524287);
    if (isSetAvatarID())
      hashCode = hashCode * 8191 + AvatarID.hashCode();

    hashCode = hashCode * 8191 + ((isSetZeroPosture()) ? 131071 : 524287);
    if (isSetZeroPosture())
      hashCode = hashCode * 8191 + ZeroPosture.hashCode();

    hashCode = hashCode * 8191 + ((isSetProperties()) ? 131071 : 524287);
    if (isSetProperties())
      hashCode = hashCode * 8191 + Properties.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(MAvatarDescription other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetAvatarID()).compareTo(other.isSetAvatarID());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAvatarID()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.AvatarID, other.AvatarID);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetZeroPosture()).compareTo(other.isSetZeroPosture());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetZeroPosture()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ZeroPosture, other.ZeroPosture);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetProperties()).compareTo(other.isSetProperties());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetProperties()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Properties, other.Properties);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MAvatarDescription(");
    boolean first = true;

    sb.append("AvatarID:");
    if (this.AvatarID == null) {
      sb.append("null");
    } else {
      sb.append(this.AvatarID);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("ZeroPosture:");
    if (this.ZeroPosture == null) {
      sb.append("null");
    } else {
      sb.append(this.ZeroPosture);
    }
    first = false;
    if (isSetProperties()) {
      if (!first) sb.append(", ");
      sb.append("Properties:");
      if (this.Properties == null) {
        sb.append("null");
      } else {
        sb.append(this.Properties);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (AvatarID == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'AvatarID' was not present! Struct: " + toString());
    }
    if (ZeroPosture == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'ZeroPosture' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
    if (ZeroPosture != null) {
      ZeroPosture.validate();
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

  private static class MAvatarDescriptionStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MAvatarDescriptionStandardScheme getScheme() {
      return new MAvatarDescriptionStandardScheme();
    }
  }

  private static class MAvatarDescriptionStandardScheme extends org.apache.thrift.scheme.StandardScheme<MAvatarDescription> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MAvatarDescription struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // AVATAR_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.AvatarID = iprot.readString();
              struct.setAvatarIDIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // ZERO_POSTURE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.ZeroPosture = new MAvatarPosture();
              struct.ZeroPosture.read(iprot);
              struct.setZeroPostureIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // PROPERTIES
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map50 = iprot.readMapBegin();
                struct.Properties = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map50.size);
                @org.apache.thrift.annotation.Nullable java.lang.String _key51;
                @org.apache.thrift.annotation.Nullable java.lang.String _val52;
                for (int _i53 = 0; _i53 < _map50.size; ++_i53)
                {
                  _key51 = iprot.readString();
                  _val52 = iprot.readString();
                  struct.Properties.put(_key51, _val52);
                }
                iprot.readMapEnd();
              }
              struct.setPropertiesIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, MAvatarDescription struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.AvatarID != null) {
        oprot.writeFieldBegin(AVATAR_ID_FIELD_DESC);
        oprot.writeString(struct.AvatarID);
        oprot.writeFieldEnd();
      }
      if (struct.ZeroPosture != null) {
        oprot.writeFieldBegin(ZERO_POSTURE_FIELD_DESC);
        struct.ZeroPosture.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.Properties != null) {
        if (struct.isSetProperties()) {
          oprot.writeFieldBegin(PROPERTIES_FIELD_DESC);
          {
            oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.Properties.size()));
            for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter54 : struct.Properties.entrySet())
            {
              oprot.writeString(_iter54.getKey());
              oprot.writeString(_iter54.getValue());
            }
            oprot.writeMapEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MAvatarDescriptionTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MAvatarDescriptionTupleScheme getScheme() {
      return new MAvatarDescriptionTupleScheme();
    }
  }

  private static class MAvatarDescriptionTupleScheme extends org.apache.thrift.scheme.TupleScheme<MAvatarDescription> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MAvatarDescription struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeString(struct.AvatarID);
      struct.ZeroPosture.write(oprot);
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetProperties()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetProperties()) {
        {
          oprot.writeI32(struct.Properties.size());
          for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter55 : struct.Properties.entrySet())
          {
            oprot.writeString(_iter55.getKey());
            oprot.writeString(_iter55.getValue());
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MAvatarDescription struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.AvatarID = iprot.readString();
      struct.setAvatarIDIsSet(true);
      struct.ZeroPosture = new MAvatarPosture();
      struct.ZeroPosture.read(iprot);
      struct.setZeroPostureIsSet(true);
      java.util.BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TMap _map56 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.Properties = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map56.size);
          @org.apache.thrift.annotation.Nullable java.lang.String _key57;
          @org.apache.thrift.annotation.Nullable java.lang.String _val58;
          for (int _i59 = 0; _i59 < _map56.size; ++_i59)
          {
            _key57 = iprot.readString();
            _val58 = iprot.readString();
            struct.Properties.put(_key57, _val58);
          }
        }
        struct.setPropertiesIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

