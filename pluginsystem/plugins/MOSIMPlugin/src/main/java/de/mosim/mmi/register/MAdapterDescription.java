/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.register;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2023-06-29")
public class MAdapterDescription implements org.apache.thrift.TBase<MAdapterDescription, MAdapterDescription._Fields>, java.io.Serializable, Cloneable, Comparable<MAdapterDescription> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MAdapterDescription");

  private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("Name", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("ID", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField LANGUAGE_FIELD_DESC = new org.apache.thrift.protocol.TField("Language", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField ADDRESSES_FIELD_DESC = new org.apache.thrift.protocol.TField("Addresses", org.apache.thrift.protocol.TType.LIST, (short)4);
  private static final org.apache.thrift.protocol.TField PROPERTIES_FIELD_DESC = new org.apache.thrift.protocol.TField("Properties", org.apache.thrift.protocol.TType.MAP, (short)5);
  private static final org.apache.thrift.protocol.TField PARAMETERS_FIELD_DESC = new org.apache.thrift.protocol.TField("Parameters", org.apache.thrift.protocol.TType.LIST, (short)6);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MAdapterDescriptionStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MAdapterDescriptionTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.lang.String Name; // required
  public @org.apache.thrift.annotation.Nullable java.lang.String ID; // required
  public @org.apache.thrift.annotation.Nullable java.lang.String Language; // required
  public @org.apache.thrift.annotation.Nullable java.util.List<de.mosim.mmi.core.MIPAddress> Addresses; // required
  public @org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,java.lang.String> Properties; // optional
  public @org.apache.thrift.annotation.Nullable java.util.List<de.mosim.mmi.core.MParameter> Parameters; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    NAME((short)1, "Name"),
    ID((short)2, "ID"),
    LANGUAGE((short)3, "Language"),
    ADDRESSES((short)4, "Addresses"),
    PROPERTIES((short)5, "Properties"),
    PARAMETERS((short)6, "Parameters");

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
        case 1: // NAME
          return NAME;
        case 2: // ID
          return ID;
        case 3: // LANGUAGE
          return LANGUAGE;
        case 4: // ADDRESSES
          return ADDRESSES;
        case 5: // PROPERTIES
          return PROPERTIES;
        case 6: // PARAMETERS
          return PARAMETERS;
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
  private static final _Fields optionals[] = {_Fields.PROPERTIES,_Fields.PARAMETERS};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("Name", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("ID", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.LANGUAGE, new org.apache.thrift.meta_data.FieldMetaData("Language", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.ADDRESSES, new org.apache.thrift.meta_data.FieldMetaData("Addresses", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.core.MIPAddress.class))));
    tmpMap.put(_Fields.PROPERTIES, new org.apache.thrift.meta_data.FieldMetaData("Properties", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.PARAMETERS, new org.apache.thrift.meta_data.FieldMetaData("Parameters", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.core.MParameter.class))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MAdapterDescription.class, metaDataMap);
  }

  public MAdapterDescription() {
  }

  public MAdapterDescription(
    java.lang.String Name,
    java.lang.String ID,
    java.lang.String Language,
    java.util.List<de.mosim.mmi.core.MIPAddress> Addresses)
  {
    this();
    this.Name = Name;
    this.ID = ID;
    this.Language = Language;
    this.Addresses = Addresses;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MAdapterDescription(MAdapterDescription other) {
    if (other.isSetName()) {
      this.Name = other.Name;
    }
    if (other.isSetID()) {
      this.ID = other.ID;
    }
    if (other.isSetLanguage()) {
      this.Language = other.Language;
    }
    if (other.isSetAddresses()) {
      java.util.List<de.mosim.mmi.core.MIPAddress> __this__Addresses = new java.util.ArrayList<de.mosim.mmi.core.MIPAddress>(other.Addresses.size());
      for (de.mosim.mmi.core.MIPAddress other_element : other.Addresses) {
        __this__Addresses.add(new de.mosim.mmi.core.MIPAddress(other_element));
      }
      this.Addresses = __this__Addresses;
    }
    if (other.isSetProperties()) {
      java.util.Map<java.lang.String,java.lang.String> __this__Properties = new java.util.HashMap<java.lang.String,java.lang.String>(other.Properties);
      this.Properties = __this__Properties;
    }
    if (other.isSetParameters()) {
      java.util.List<de.mosim.mmi.core.MParameter> __this__Parameters = new java.util.ArrayList<de.mosim.mmi.core.MParameter>(other.Parameters.size());
      for (de.mosim.mmi.core.MParameter other_element : other.Parameters) {
        __this__Parameters.add(new de.mosim.mmi.core.MParameter(other_element));
      }
      this.Parameters = __this__Parameters;
    }
  }

  public MAdapterDescription deepCopy() {
    return new MAdapterDescription(this);
  }

  @Override
  public void clear() {
    this.Name = null;
    this.ID = null;
    this.Language = null;
    this.Addresses = null;
    this.Properties = null;
    this.Parameters = null;
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getName() {
    return this.Name;
  }

  public MAdapterDescription setName(@org.apache.thrift.annotation.Nullable java.lang.String Name) {
    this.Name = Name;
    return this;
  }

  public void unsetName() {
    this.Name = null;
  }

  /** Returns true if field Name is set (has been assigned a value) and false otherwise */
  public boolean isSetName() {
    return this.Name != null;
  }

  public void setNameIsSet(boolean value) {
    if (!value) {
      this.Name = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getID() {
    return this.ID;
  }

  public MAdapterDescription setID(@org.apache.thrift.annotation.Nullable java.lang.String ID) {
    this.ID = ID;
    return this;
  }

  public void unsetID() {
    this.ID = null;
  }

  /** Returns true if field ID is set (has been assigned a value) and false otherwise */
  public boolean isSetID() {
    return this.ID != null;
  }

  public void setIDIsSet(boolean value) {
    if (!value) {
      this.ID = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getLanguage() {
    return this.Language;
  }

  public MAdapterDescription setLanguage(@org.apache.thrift.annotation.Nullable java.lang.String Language) {
    this.Language = Language;
    return this;
  }

  public void unsetLanguage() {
    this.Language = null;
  }

  /** Returns true if field Language is set (has been assigned a value) and false otherwise */
  public boolean isSetLanguage() {
    return this.Language != null;
  }

  public void setLanguageIsSet(boolean value) {
    if (!value) {
      this.Language = null;
    }
  }

  public int getAddressesSize() {
    return (this.Addresses == null) ? 0 : this.Addresses.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<de.mosim.mmi.core.MIPAddress> getAddressesIterator() {
    return (this.Addresses == null) ? null : this.Addresses.iterator();
  }

  public void addToAddresses(de.mosim.mmi.core.MIPAddress elem) {
    if (this.Addresses == null) {
      this.Addresses = new java.util.ArrayList<de.mosim.mmi.core.MIPAddress>();
    }
    this.Addresses.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<de.mosim.mmi.core.MIPAddress> getAddresses() {
    return this.Addresses;
  }

  public MAdapterDescription setAddresses(@org.apache.thrift.annotation.Nullable java.util.List<de.mosim.mmi.core.MIPAddress> Addresses) {
    this.Addresses = Addresses;
    return this;
  }

  public void unsetAddresses() {
    this.Addresses = null;
  }

  /** Returns true if field Addresses is set (has been assigned a value) and false otherwise */
  public boolean isSetAddresses() {
    return this.Addresses != null;
  }

  public void setAddressesIsSet(boolean value) {
    if (!value) {
      this.Addresses = null;
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

  public MAdapterDescription setProperties(@org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,java.lang.String> Properties) {
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

  public int getParametersSize() {
    return (this.Parameters == null) ? 0 : this.Parameters.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<de.mosim.mmi.core.MParameter> getParametersIterator() {
    return (this.Parameters == null) ? null : this.Parameters.iterator();
  }

  public void addToParameters(de.mosim.mmi.core.MParameter elem) {
    if (this.Parameters == null) {
      this.Parameters = new java.util.ArrayList<de.mosim.mmi.core.MParameter>();
    }
    this.Parameters.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<de.mosim.mmi.core.MParameter> getParameters() {
    return this.Parameters;
  }

  public MAdapterDescription setParameters(@org.apache.thrift.annotation.Nullable java.util.List<de.mosim.mmi.core.MParameter> Parameters) {
    this.Parameters = Parameters;
    return this;
  }

  public void unsetParameters() {
    this.Parameters = null;
  }

  /** Returns true if field Parameters is set (has been assigned a value) and false otherwise */
  public boolean isSetParameters() {
    return this.Parameters != null;
  }

  public void setParametersIsSet(boolean value) {
    if (!value) {
      this.Parameters = null;
    }
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case NAME:
      if (value == null) {
        unsetName();
      } else {
        setName((java.lang.String)value);
      }
      break;

    case ID:
      if (value == null) {
        unsetID();
      } else {
        setID((java.lang.String)value);
      }
      break;

    case LANGUAGE:
      if (value == null) {
        unsetLanguage();
      } else {
        setLanguage((java.lang.String)value);
      }
      break;

    case ADDRESSES:
      if (value == null) {
        unsetAddresses();
      } else {
        setAddresses((java.util.List<de.mosim.mmi.core.MIPAddress>)value);
      }
      break;

    case PROPERTIES:
      if (value == null) {
        unsetProperties();
      } else {
        setProperties((java.util.Map<java.lang.String,java.lang.String>)value);
      }
      break;

    case PARAMETERS:
      if (value == null) {
        unsetParameters();
      } else {
        setParameters((java.util.List<de.mosim.mmi.core.MParameter>)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case NAME:
      return getName();

    case ID:
      return getID();

    case LANGUAGE:
      return getLanguage();

    case ADDRESSES:
      return getAddresses();

    case PROPERTIES:
      return getProperties();

    case PARAMETERS:
      return getParameters();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case NAME:
      return isSetName();
    case ID:
      return isSetID();
    case LANGUAGE:
      return isSetLanguage();
    case ADDRESSES:
      return isSetAddresses();
    case PROPERTIES:
      return isSetProperties();
    case PARAMETERS:
      return isSetParameters();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MAdapterDescription)
      return this.equals((MAdapterDescription)that);
    return false;
  }

  public boolean equals(MAdapterDescription that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_Name = true && this.isSetName();
    boolean that_present_Name = true && that.isSetName();
    if (this_present_Name || that_present_Name) {
      if (!(this_present_Name && that_present_Name))
        return false;
      if (!this.Name.equals(that.Name))
        return false;
    }

    boolean this_present_ID = true && this.isSetID();
    boolean that_present_ID = true && that.isSetID();
    if (this_present_ID || that_present_ID) {
      if (!(this_present_ID && that_present_ID))
        return false;
      if (!this.ID.equals(that.ID))
        return false;
    }

    boolean this_present_Language = true && this.isSetLanguage();
    boolean that_present_Language = true && that.isSetLanguage();
    if (this_present_Language || that_present_Language) {
      if (!(this_present_Language && that_present_Language))
        return false;
      if (!this.Language.equals(that.Language))
        return false;
    }

    boolean this_present_Addresses = true && this.isSetAddresses();
    boolean that_present_Addresses = true && that.isSetAddresses();
    if (this_present_Addresses || that_present_Addresses) {
      if (!(this_present_Addresses && that_present_Addresses))
        return false;
      if (!this.Addresses.equals(that.Addresses))
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

    boolean this_present_Parameters = true && this.isSetParameters();
    boolean that_present_Parameters = true && that.isSetParameters();
    if (this_present_Parameters || that_present_Parameters) {
      if (!(this_present_Parameters && that_present_Parameters))
        return false;
      if (!this.Parameters.equals(that.Parameters))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetName()) ? 131071 : 524287);
    if (isSetName())
      hashCode = hashCode * 8191 + Name.hashCode();

    hashCode = hashCode * 8191 + ((isSetID()) ? 131071 : 524287);
    if (isSetID())
      hashCode = hashCode * 8191 + ID.hashCode();

    hashCode = hashCode * 8191 + ((isSetLanguage()) ? 131071 : 524287);
    if (isSetLanguage())
      hashCode = hashCode * 8191 + Language.hashCode();

    hashCode = hashCode * 8191 + ((isSetAddresses()) ? 131071 : 524287);
    if (isSetAddresses())
      hashCode = hashCode * 8191 + Addresses.hashCode();

    hashCode = hashCode * 8191 + ((isSetProperties()) ? 131071 : 524287);
    if (isSetProperties())
      hashCode = hashCode * 8191 + Properties.hashCode();

    hashCode = hashCode * 8191 + ((isSetParameters()) ? 131071 : 524287);
    if (isSetParameters())
      hashCode = hashCode * 8191 + Parameters.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(MAdapterDescription other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetName()).compareTo(other.isSetName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Name, other.Name);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetID()).compareTo(other.isSetID());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetID()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ID, other.ID);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetLanguage()).compareTo(other.isSetLanguage());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLanguage()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Language, other.Language);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetAddresses()).compareTo(other.isSetAddresses());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAddresses()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Addresses, other.Addresses);
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
    lastComparison = java.lang.Boolean.valueOf(isSetParameters()).compareTo(other.isSetParameters());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetParameters()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Parameters, other.Parameters);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MAdapterDescription(");
    boolean first = true;

    sb.append("Name:");
    if (this.Name == null) {
      sb.append("null");
    } else {
      sb.append(this.Name);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("ID:");
    if (this.ID == null) {
      sb.append("null");
    } else {
      sb.append(this.ID);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("Language:");
    if (this.Language == null) {
      sb.append("null");
    } else {
      sb.append(this.Language);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("Addresses:");
    if (this.Addresses == null) {
      sb.append("null");
    } else {
      sb.append(this.Addresses);
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
    if (isSetParameters()) {
      if (!first) sb.append(", ");
      sb.append("Parameters:");
      if (this.Parameters == null) {
        sb.append("null");
      } else {
        sb.append(this.Parameters);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (Name == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'Name' was not present! Struct: " + toString());
    }
    if (ID == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'ID' was not present! Struct: " + toString());
    }
    if (Language == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'Language' was not present! Struct: " + toString());
    }
    if (Addresses == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'Addresses' was not present! Struct: " + toString());
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

  private static class MAdapterDescriptionStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MAdapterDescriptionStandardScheme getScheme() {
      return new MAdapterDescriptionStandardScheme();
    }
  }

  private static class MAdapterDescriptionStandardScheme extends org.apache.thrift.scheme.StandardScheme<MAdapterDescription> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MAdapterDescription struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.Name = iprot.readString();
              struct.setNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.ID = iprot.readString();
              struct.setIDIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // LANGUAGE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.Language = iprot.readString();
              struct.setLanguageIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // ADDRESSES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                struct.Addresses = new java.util.ArrayList<de.mosim.mmi.core.MIPAddress>(_list0.size);
                @org.apache.thrift.annotation.Nullable de.mosim.mmi.core.MIPAddress _elem1;
                for (int _i2 = 0; _i2 < _list0.size; ++_i2)
                {
                  _elem1 = new de.mosim.mmi.core.MIPAddress();
                  _elem1.read(iprot);
                  struct.Addresses.add(_elem1);
                }
                iprot.readListEnd();
              }
              struct.setAddressesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // PROPERTIES
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map3 = iprot.readMapBegin();
                struct.Properties = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map3.size);
                @org.apache.thrift.annotation.Nullable java.lang.String _key4;
                @org.apache.thrift.annotation.Nullable java.lang.String _val5;
                for (int _i6 = 0; _i6 < _map3.size; ++_i6)
                {
                  _key4 = iprot.readString();
                  _val5 = iprot.readString();
                  struct.Properties.put(_key4, _val5);
                }
                iprot.readMapEnd();
              }
              struct.setPropertiesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // PARAMETERS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list7 = iprot.readListBegin();
                struct.Parameters = new java.util.ArrayList<de.mosim.mmi.core.MParameter>(_list7.size);
                @org.apache.thrift.annotation.Nullable de.mosim.mmi.core.MParameter _elem8;
                for (int _i9 = 0; _i9 < _list7.size; ++_i9)
                {
                  _elem8 = new de.mosim.mmi.core.MParameter();
                  _elem8.read(iprot);
                  struct.Parameters.add(_elem8);
                }
                iprot.readListEnd();
              }
              struct.setParametersIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, MAdapterDescription struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.Name != null) {
        oprot.writeFieldBegin(NAME_FIELD_DESC);
        oprot.writeString(struct.Name);
        oprot.writeFieldEnd();
      }
      if (struct.ID != null) {
        oprot.writeFieldBegin(ID_FIELD_DESC);
        oprot.writeString(struct.ID);
        oprot.writeFieldEnd();
      }
      if (struct.Language != null) {
        oprot.writeFieldBegin(LANGUAGE_FIELD_DESC);
        oprot.writeString(struct.Language);
        oprot.writeFieldEnd();
      }
      if (struct.Addresses != null) {
        oprot.writeFieldBegin(ADDRESSES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.Addresses.size()));
          for (de.mosim.mmi.core.MIPAddress _iter10 : struct.Addresses)
          {
            _iter10.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.Properties != null) {
        if (struct.isSetProperties()) {
          oprot.writeFieldBegin(PROPERTIES_FIELD_DESC);
          {
            oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.Properties.size()));
            for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter11 : struct.Properties.entrySet())
            {
              oprot.writeString(_iter11.getKey());
              oprot.writeString(_iter11.getValue());
            }
            oprot.writeMapEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.Parameters != null) {
        if (struct.isSetParameters()) {
          oprot.writeFieldBegin(PARAMETERS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.Parameters.size()));
            for (de.mosim.mmi.core.MParameter _iter12 : struct.Parameters)
            {
              _iter12.write(oprot);
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

  private static class MAdapterDescriptionTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MAdapterDescriptionTupleScheme getScheme() {
      return new MAdapterDescriptionTupleScheme();
    }
  }

  private static class MAdapterDescriptionTupleScheme extends org.apache.thrift.scheme.TupleScheme<MAdapterDescription> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MAdapterDescription struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeString(struct.Name);
      oprot.writeString(struct.ID);
      oprot.writeString(struct.Language);
      {
        oprot.writeI32(struct.Addresses.size());
        for (de.mosim.mmi.core.MIPAddress _iter13 : struct.Addresses)
        {
          _iter13.write(oprot);
        }
      }
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetProperties()) {
        optionals.set(0);
      }
      if (struct.isSetParameters()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetProperties()) {
        {
          oprot.writeI32(struct.Properties.size());
          for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter14 : struct.Properties.entrySet())
          {
            oprot.writeString(_iter14.getKey());
            oprot.writeString(_iter14.getValue());
          }
        }
      }
      if (struct.isSetParameters()) {
        {
          oprot.writeI32(struct.Parameters.size());
          for (de.mosim.mmi.core.MParameter _iter15 : struct.Parameters)
          {
            _iter15.write(oprot);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MAdapterDescription struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.Name = iprot.readString();
      struct.setNameIsSet(true);
      struct.ID = iprot.readString();
      struct.setIDIsSet(true);
      struct.Language = iprot.readString();
      struct.setLanguageIsSet(true);
      {
        org.apache.thrift.protocol.TList _list16 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
        struct.Addresses = new java.util.ArrayList<de.mosim.mmi.core.MIPAddress>(_list16.size);
        @org.apache.thrift.annotation.Nullable de.mosim.mmi.core.MIPAddress _elem17;
        for (int _i18 = 0; _i18 < _list16.size; ++_i18)
        {
          _elem17 = new de.mosim.mmi.core.MIPAddress();
          _elem17.read(iprot);
          struct.Addresses.add(_elem17);
        }
      }
      struct.setAddressesIsSet(true);
      java.util.BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TMap _map19 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.Properties = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map19.size);
          @org.apache.thrift.annotation.Nullable java.lang.String _key20;
          @org.apache.thrift.annotation.Nullable java.lang.String _val21;
          for (int _i22 = 0; _i22 < _map19.size; ++_i22)
          {
            _key20 = iprot.readString();
            _val21 = iprot.readString();
            struct.Properties.put(_key20, _val21);
          }
        }
        struct.setPropertiesIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list23 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.Parameters = new java.util.ArrayList<de.mosim.mmi.core.MParameter>(_list23.size);
          @org.apache.thrift.annotation.Nullable de.mosim.mmi.core.MParameter _elem24;
          for (int _i25 = 0; _i25 < _list23.size; ++_i25)
          {
            _elem24 = new de.mosim.mmi.core.MParameter();
            _elem24.read(iprot);
            struct.Parameters.add(_elem24);
          }
        }
        struct.setParametersIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

