/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.scene;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2023-06-29")
public class MDrawingCall implements org.apache.thrift.TBase<MDrawingCall, MDrawingCall._Fields>, java.io.Serializable, Cloneable, Comparable<MDrawingCall> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MDrawingCall");

  private static final org.apache.thrift.protocol.TField TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("Type", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField DATA_FIELD_DESC = new org.apache.thrift.protocol.TField("Data", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField PROPERTIES_FIELD_DESC = new org.apache.thrift.protocol.TField("Properties", org.apache.thrift.protocol.TType.MAP, (short)3);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MDrawingCallStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MDrawingCallTupleSchemeFactory();

  /**
   * 
   * @see MDrawingCallType
   */
  public @org.apache.thrift.annotation.Nullable MDrawingCallType Type; // required
  public @org.apache.thrift.annotation.Nullable java.util.List<java.lang.Double> Data; // optional
  public @org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,java.lang.String> Properties; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    /**
     * 
     * @see MDrawingCallType
     */
    TYPE((short)1, "Type"),
    DATA((short)2, "Data"),
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
        case 1: // TYPE
          return TYPE;
        case 2: // DATA
          return DATA;
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
  private static final _Fields optionals[] = {_Fields.DATA,_Fields.PROPERTIES};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TYPE, new org.apache.thrift.meta_data.FieldMetaData("Type", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, MDrawingCallType.class)));
    tmpMap.put(_Fields.DATA, new org.apache.thrift.meta_data.FieldMetaData("Data", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE))));
    tmpMap.put(_Fields.PROPERTIES, new org.apache.thrift.meta_data.FieldMetaData("Properties", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MDrawingCall.class, metaDataMap);
  }

  public MDrawingCall() {
  }

  public MDrawingCall(
    MDrawingCallType Type)
  {
    this();
    this.Type = Type;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MDrawingCall(MDrawingCall other) {
    if (other.isSetType()) {
      this.Type = other.Type;
    }
    if (other.isSetData()) {
      java.util.List<java.lang.Double> __this__Data = new java.util.ArrayList<java.lang.Double>(other.Data);
      this.Data = __this__Data;
    }
    if (other.isSetProperties()) {
      java.util.Map<java.lang.String,java.lang.String> __this__Properties = new java.util.HashMap<java.lang.String,java.lang.String>(other.Properties);
      this.Properties = __this__Properties;
    }
  }

  public MDrawingCall deepCopy() {
    return new MDrawingCall(this);
  }

  @Override
  public void clear() {
    this.Type = null;
    this.Data = null;
    this.Properties = null;
  }

  /**
   * 
   * @see MDrawingCallType
   */
  @org.apache.thrift.annotation.Nullable
  public MDrawingCallType getType() {
    return this.Type;
  }

  /**
   * 
   * @see MDrawingCallType
   */
  public MDrawingCall setType(@org.apache.thrift.annotation.Nullable MDrawingCallType Type) {
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

  public int getDataSize() {
    return (this.Data == null) ? 0 : this.Data.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<java.lang.Double> getDataIterator() {
    return (this.Data == null) ? null : this.Data.iterator();
  }

  public void addToData(double elem) {
    if (this.Data == null) {
      this.Data = new java.util.ArrayList<java.lang.Double>();
    }
    this.Data.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<java.lang.Double> getData() {
    return this.Data;
  }

  public MDrawingCall setData(@org.apache.thrift.annotation.Nullable java.util.List<java.lang.Double> Data) {
    this.Data = Data;
    return this;
  }

  public void unsetData() {
    this.Data = null;
  }

  /** Returns true if field Data is set (has been assigned a value) and false otherwise */
  public boolean isSetData() {
    return this.Data != null;
  }

  public void setDataIsSet(boolean value) {
    if (!value) {
      this.Data = null;
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

  public MDrawingCall setProperties(@org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,java.lang.String> Properties) {
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
    case TYPE:
      if (value == null) {
        unsetType();
      } else {
        setType((MDrawingCallType)value);
      }
      break;

    case DATA:
      if (value == null) {
        unsetData();
      } else {
        setData((java.util.List<java.lang.Double>)value);
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
    case TYPE:
      return getType();

    case DATA:
      return getData();

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
    case TYPE:
      return isSetType();
    case DATA:
      return isSetData();
    case PROPERTIES:
      return isSetProperties();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MDrawingCall)
      return this.equals((MDrawingCall)that);
    return false;
  }

  public boolean equals(MDrawingCall that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_Type = true && this.isSetType();
    boolean that_present_Type = true && that.isSetType();
    if (this_present_Type || that_present_Type) {
      if (!(this_present_Type && that_present_Type))
        return false;
      if (!this.Type.equals(that.Type))
        return false;
    }

    boolean this_present_Data = true && this.isSetData();
    boolean that_present_Data = true && that.isSetData();
    if (this_present_Data || that_present_Data) {
      if (!(this_present_Data && that_present_Data))
        return false;
      if (!this.Data.equals(that.Data))
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

    hashCode = hashCode * 8191 + ((isSetType()) ? 131071 : 524287);
    if (isSetType())
      hashCode = hashCode * 8191 + Type.getValue();

    hashCode = hashCode * 8191 + ((isSetData()) ? 131071 : 524287);
    if (isSetData())
      hashCode = hashCode * 8191 + Data.hashCode();

    hashCode = hashCode * 8191 + ((isSetProperties()) ? 131071 : 524287);
    if (isSetProperties())
      hashCode = hashCode * 8191 + Properties.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(MDrawingCall other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

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
    lastComparison = java.lang.Boolean.valueOf(isSetData()).compareTo(other.isSetData());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetData()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Data, other.Data);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MDrawingCall(");
    boolean first = true;

    sb.append("Type:");
    if (this.Type == null) {
      sb.append("null");
    } else {
      sb.append(this.Type);
    }
    first = false;
    if (isSetData()) {
      if (!first) sb.append(", ");
      sb.append("Data:");
      if (this.Data == null) {
        sb.append("null");
      } else {
        sb.append(this.Data);
      }
      first = false;
    }
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
    if (Type == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'Type' was not present! Struct: " + toString());
    }
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

  private static class MDrawingCallStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MDrawingCallStandardScheme getScheme() {
      return new MDrawingCallStandardScheme();
    }
  }

  private static class MDrawingCallStandardScheme extends org.apache.thrift.scheme.StandardScheme<MDrawingCall> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MDrawingCall struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TYPE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.Type = de.mosim.mmi.scene.MDrawingCallType.findByValue(iprot.readI32());
              struct.setTypeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // DATA
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list148 = iprot.readListBegin();
                struct.Data = new java.util.ArrayList<java.lang.Double>(_list148.size);
                double _elem149;
                for (int _i150 = 0; _i150 < _list148.size; ++_i150)
                {
                  _elem149 = iprot.readDouble();
                  struct.Data.add(_elem149);
                }
                iprot.readListEnd();
              }
              struct.setDataIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // PROPERTIES
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map151 = iprot.readMapBegin();
                struct.Properties = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map151.size);
                @org.apache.thrift.annotation.Nullable java.lang.String _key152;
                @org.apache.thrift.annotation.Nullable java.lang.String _val153;
                for (int _i154 = 0; _i154 < _map151.size; ++_i154)
                {
                  _key152 = iprot.readString();
                  _val153 = iprot.readString();
                  struct.Properties.put(_key152, _val153);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, MDrawingCall struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.Type != null) {
        oprot.writeFieldBegin(TYPE_FIELD_DESC);
        oprot.writeI32(struct.Type.getValue());
        oprot.writeFieldEnd();
      }
      if (struct.Data != null) {
        if (struct.isSetData()) {
          oprot.writeFieldBegin(DATA_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.DOUBLE, struct.Data.size()));
            for (double _iter155 : struct.Data)
            {
              oprot.writeDouble(_iter155);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.Properties != null) {
        if (struct.isSetProperties()) {
          oprot.writeFieldBegin(PROPERTIES_FIELD_DESC);
          {
            oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.Properties.size()));
            for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter156 : struct.Properties.entrySet())
            {
              oprot.writeString(_iter156.getKey());
              oprot.writeString(_iter156.getValue());
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

  private static class MDrawingCallTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MDrawingCallTupleScheme getScheme() {
      return new MDrawingCallTupleScheme();
    }
  }

  private static class MDrawingCallTupleScheme extends org.apache.thrift.scheme.TupleScheme<MDrawingCall> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MDrawingCall struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeI32(struct.Type.getValue());
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetData()) {
        optionals.set(0);
      }
      if (struct.isSetProperties()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetData()) {
        {
          oprot.writeI32(struct.Data.size());
          for (double _iter157 : struct.Data)
          {
            oprot.writeDouble(_iter157);
          }
        }
      }
      if (struct.isSetProperties()) {
        {
          oprot.writeI32(struct.Properties.size());
          for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter158 : struct.Properties.entrySet())
          {
            oprot.writeString(_iter158.getKey());
            oprot.writeString(_iter158.getValue());
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MDrawingCall struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.Type = de.mosim.mmi.scene.MDrawingCallType.findByValue(iprot.readI32());
      struct.setTypeIsSet(true);
      java.util.BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list159 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.DOUBLE, iprot.readI32());
          struct.Data = new java.util.ArrayList<java.lang.Double>(_list159.size);
          double _elem160;
          for (int _i161 = 0; _i161 < _list159.size; ++_i161)
          {
            _elem160 = iprot.readDouble();
            struct.Data.add(_elem160);
          }
        }
        struct.setDataIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TMap _map162 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.Properties = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map162.size);
          @org.apache.thrift.annotation.Nullable java.lang.String _key163;
          @org.apache.thrift.annotation.Nullable java.lang.String _val164;
          for (int _i165 = 0; _i165 < _map162.size; ++_i165)
          {
            _key163 = iprot.readString();
            _val164 = iprot.readString();
            struct.Properties.put(_key163, _val164);
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

