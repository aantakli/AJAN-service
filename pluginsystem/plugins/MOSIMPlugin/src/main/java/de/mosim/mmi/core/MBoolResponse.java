/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.core;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2023-06-29")
public class MBoolResponse implements org.apache.thrift.TBase<MBoolResponse, MBoolResponse._Fields>, java.io.Serializable, Cloneable, Comparable<MBoolResponse> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MBoolResponse");

  private static final org.apache.thrift.protocol.TField SUCCESSFUL_FIELD_DESC = new org.apache.thrift.protocol.TField("Successful", org.apache.thrift.protocol.TType.BOOL, (short)1);
  private static final org.apache.thrift.protocol.TField LOG_DATA_FIELD_DESC = new org.apache.thrift.protocol.TField("LogData", org.apache.thrift.protocol.TType.LIST, (short)2);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MBoolResponseStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MBoolResponseTupleSchemeFactory();

  public boolean Successful; // required
  public @org.apache.thrift.annotation.Nullable java.util.List<java.lang.String> LogData; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    SUCCESSFUL((short)1, "Successful"),
    LOG_DATA((short)2, "LogData");

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
        case 1: // SUCCESSFUL
          return SUCCESSFUL;
        case 2: // LOG_DATA
          return LOG_DATA;
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
  private static final int __SUCCESSFUL_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.LOG_DATA};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.SUCCESSFUL, new org.apache.thrift.meta_data.FieldMetaData("Successful", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    tmpMap.put(_Fields.LOG_DATA, new org.apache.thrift.meta_data.FieldMetaData("LogData", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MBoolResponse.class, metaDataMap);
  }

  public MBoolResponse() {
  }

  public MBoolResponse(
    boolean Successful)
  {
    this();
    this.Successful = Successful;
    setSuccessfulIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MBoolResponse(MBoolResponse other) {
    __isset_bitfield = other.__isset_bitfield;
    this.Successful = other.Successful;
    if (other.isSetLogData()) {
      java.util.List<java.lang.String> __this__LogData = new java.util.ArrayList<java.lang.String>(other.LogData);
      this.LogData = __this__LogData;
    }
  }

  public MBoolResponse deepCopy() {
    return new MBoolResponse(this);
  }

  @Override
  public void clear() {
    setSuccessfulIsSet(false);
    this.Successful = false;
    this.LogData = null;
  }

  public boolean isSuccessful() {
    return this.Successful;
  }

  public MBoolResponse setSuccessful(boolean Successful) {
    this.Successful = Successful;
    setSuccessfulIsSet(true);
    return this;
  }

  public void unsetSuccessful() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __SUCCESSFUL_ISSET_ID);
  }

  /** Returns true if field Successful is set (has been assigned a value) and false otherwise */
  public boolean isSetSuccessful() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __SUCCESSFUL_ISSET_ID);
  }

  public void setSuccessfulIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __SUCCESSFUL_ISSET_ID, value);
  }

  public int getLogDataSize() {
    return (this.LogData == null) ? 0 : this.LogData.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<java.lang.String> getLogDataIterator() {
    return (this.LogData == null) ? null : this.LogData.iterator();
  }

  public void addToLogData(java.lang.String elem) {
    if (this.LogData == null) {
      this.LogData = new java.util.ArrayList<java.lang.String>();
    }
    this.LogData.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<java.lang.String> getLogData() {
    return this.LogData;
  }

  public MBoolResponse setLogData(@org.apache.thrift.annotation.Nullable java.util.List<java.lang.String> LogData) {
    this.LogData = LogData;
    return this;
  }

  public void unsetLogData() {
    this.LogData = null;
  }

  /** Returns true if field LogData is set (has been assigned a value) and false otherwise */
  public boolean isSetLogData() {
    return this.LogData != null;
  }

  public void setLogDataIsSet(boolean value) {
    if (!value) {
      this.LogData = null;
    }
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case SUCCESSFUL:
      if (value == null) {
        unsetSuccessful();
      } else {
        setSuccessful((java.lang.Boolean)value);
      }
      break;

    case LOG_DATA:
      if (value == null) {
        unsetLogData();
      } else {
        setLogData((java.util.List<java.lang.String>)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case SUCCESSFUL:
      return isSuccessful();

    case LOG_DATA:
      return getLogData();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case SUCCESSFUL:
      return isSetSuccessful();
    case LOG_DATA:
      return isSetLogData();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MBoolResponse)
      return this.equals((MBoolResponse)that);
    return false;
  }

  public boolean equals(MBoolResponse that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_Successful = true;
    boolean that_present_Successful = true;
    if (this_present_Successful || that_present_Successful) {
      if (!(this_present_Successful && that_present_Successful))
        return false;
      if (this.Successful != that.Successful)
        return false;
    }

    boolean this_present_LogData = true && this.isSetLogData();
    boolean that_present_LogData = true && that.isSetLogData();
    if (this_present_LogData || that_present_LogData) {
      if (!(this_present_LogData && that_present_LogData))
        return false;
      if (!this.LogData.equals(that.LogData))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((Successful) ? 131071 : 524287);

    hashCode = hashCode * 8191 + ((isSetLogData()) ? 131071 : 524287);
    if (isSetLogData())
      hashCode = hashCode * 8191 + LogData.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(MBoolResponse other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetSuccessful()).compareTo(other.isSetSuccessful());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSuccessful()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Successful, other.Successful);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetLogData()).compareTo(other.isSetLogData());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLogData()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.LogData, other.LogData);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MBoolResponse(");
    boolean first = true;

    sb.append("Successful:");
    sb.append(this.Successful);
    first = false;
    if (isSetLogData()) {
      if (!first) sb.append(", ");
      sb.append("LogData:");
      if (this.LogData == null) {
        sb.append("null");
      } else {
        sb.append(this.LogData);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'Successful' because it's a primitive and you chose the non-beans generator.
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

  private static class MBoolResponseStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MBoolResponseStandardScheme getScheme() {
      return new MBoolResponseStandardScheme();
    }
  }

  private static class MBoolResponseStandardScheme extends org.apache.thrift.scheme.StandardScheme<MBoolResponse> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MBoolResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // SUCCESSFUL
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.Successful = iprot.readBool();
              struct.setSuccessfulIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // LOG_DATA
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                struct.LogData = new java.util.ArrayList<java.lang.String>(_list0.size);
                @org.apache.thrift.annotation.Nullable java.lang.String _elem1;
                for (int _i2 = 0; _i2 < _list0.size; ++_i2)
                {
                  _elem1 = iprot.readString();
                  struct.LogData.add(_elem1);
                }
                iprot.readListEnd();
              }
              struct.setLogDataIsSet(true);
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
      if (!struct.isSetSuccessful()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'Successful' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, MBoolResponse struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(SUCCESSFUL_FIELD_DESC);
      oprot.writeBool(struct.Successful);
      oprot.writeFieldEnd();
      if (struct.LogData != null) {
        if (struct.isSetLogData()) {
          oprot.writeFieldBegin(LOG_DATA_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.LogData.size()));
            for (java.lang.String _iter3 : struct.LogData)
            {
              oprot.writeString(_iter3);
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

  private static class MBoolResponseTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MBoolResponseTupleScheme getScheme() {
      return new MBoolResponseTupleScheme();
    }
  }

  private static class MBoolResponseTupleScheme extends org.apache.thrift.scheme.TupleScheme<MBoolResponse> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MBoolResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeBool(struct.Successful);
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetLogData()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetLogData()) {
        {
          oprot.writeI32(struct.LogData.size());
          for (java.lang.String _iter4 : struct.LogData)
          {
            oprot.writeString(_iter4);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MBoolResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.Successful = iprot.readBool();
      struct.setSuccessfulIsSet(true);
      java.util.BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.LogData = new java.util.ArrayList<java.lang.String>(_list5.size);
          @org.apache.thrift.annotation.Nullable java.lang.String _elem6;
          for (int _i7 = 0; _i7 < _list5.size; ++_i7)
          {
            _elem6 = iprot.readString();
            struct.LogData.add(_elem6);
          }
        }
        struct.setLogDataIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

