/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.scene;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2023-06-29")
public class MAvatarUpdate implements org.apache.thrift.TBase<MAvatarUpdate, MAvatarUpdate._Fields>, java.io.Serializable, Cloneable, Comparable<MAvatarUpdate> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MAvatarUpdate");

  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("ID", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField POSTURE_VALUES_FIELD_DESC = new org.apache.thrift.protocol.TField("PostureValues", org.apache.thrift.protocol.TType.STRUCT, (short)2);
  private static final org.apache.thrift.protocol.TField SCENE_OBJECTS_FIELD_DESC = new org.apache.thrift.protocol.TField("SceneObjects", org.apache.thrift.protocol.TType.LIST, (short)3);
  private static final org.apache.thrift.protocol.TField DESCRIPTION_FIELD_DESC = new org.apache.thrift.protocol.TField("Description", org.apache.thrift.protocol.TType.STRUCT, (short)4);
  private static final org.apache.thrift.protocol.TField PROPERTIES_FIELD_DESC = new org.apache.thrift.protocol.TField("Properties", org.apache.thrift.protocol.TType.LIST, (short)5);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MAvatarUpdateStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MAvatarUpdateTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.lang.String ID; // required
  public @org.apache.thrift.annotation.Nullable de.mosim.mmi.avatar.MAvatarPostureValues PostureValues; // optional
  public @org.apache.thrift.annotation.Nullable java.util.List<java.lang.String> SceneObjects; // optional
  public @org.apache.thrift.annotation.Nullable de.mosim.mmi.avatar.MAvatarDescription Description; // optional
  public @org.apache.thrift.annotation.Nullable java.util.List<MPropertyUpdate> Properties; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ID((short)1, "ID"),
    POSTURE_VALUES((short)2, "PostureValues"),
    SCENE_OBJECTS((short)3, "SceneObjects"),
    DESCRIPTION((short)4, "Description"),
    PROPERTIES((short)5, "Properties");

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
        case 1: // ID
          return ID;
        case 2: // POSTURE_VALUES
          return POSTURE_VALUES;
        case 3: // SCENE_OBJECTS
          return SCENE_OBJECTS;
        case 4: // DESCRIPTION
          return DESCRIPTION;
        case 5: // PROPERTIES
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
  private static final _Fields optionals[] = {_Fields.POSTURE_VALUES,_Fields.SCENE_OBJECTS,_Fields.DESCRIPTION,_Fields.PROPERTIES};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("ID", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.POSTURE_VALUES, new org.apache.thrift.meta_data.FieldMetaData("PostureValues", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.avatar.MAvatarPostureValues.class)));
    tmpMap.put(_Fields.SCENE_OBJECTS, new org.apache.thrift.meta_data.FieldMetaData("SceneObjects", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.DESCRIPTION, new org.apache.thrift.meta_data.FieldMetaData("Description", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.avatar.MAvatarDescription.class)));
    tmpMap.put(_Fields.PROPERTIES, new org.apache.thrift.meta_data.FieldMetaData("Properties", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, MPropertyUpdate.class))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MAvatarUpdate.class, metaDataMap);
  }

  public MAvatarUpdate() {
  }

  public MAvatarUpdate(
    java.lang.String ID)
  {
    this();
    this.ID = ID;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MAvatarUpdate(MAvatarUpdate other) {
    if (other.isSetID()) {
      this.ID = other.ID;
    }
    if (other.isSetPostureValues()) {
      this.PostureValues = new de.mosim.mmi.avatar.MAvatarPostureValues(other.PostureValues);
    }
    if (other.isSetSceneObjects()) {
      java.util.List<java.lang.String> __this__SceneObjects = new java.util.ArrayList<java.lang.String>(other.SceneObjects);
      this.SceneObjects = __this__SceneObjects;
    }
    if (other.isSetDescription()) {
      this.Description = new de.mosim.mmi.avatar.MAvatarDescription(other.Description);
    }
    if (other.isSetProperties()) {
      java.util.List<MPropertyUpdate> __this__Properties = new java.util.ArrayList<MPropertyUpdate>(other.Properties.size());
      for (MPropertyUpdate other_element : other.Properties) {
        __this__Properties.add(new MPropertyUpdate(other_element));
      }
      this.Properties = __this__Properties;
    }
  }

  public MAvatarUpdate deepCopy() {
    return new MAvatarUpdate(this);
  }

  @Override
  public void clear() {
    this.ID = null;
    this.PostureValues = null;
    this.SceneObjects = null;
    this.Description = null;
    this.Properties = null;
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getID() {
    return this.ID;
  }

  public MAvatarUpdate setID(@org.apache.thrift.annotation.Nullable java.lang.String ID) {
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
  public de.mosim.mmi.avatar.MAvatarPostureValues getPostureValues() {
    return this.PostureValues;
  }

  public MAvatarUpdate setPostureValues(@org.apache.thrift.annotation.Nullable de.mosim.mmi.avatar.MAvatarPostureValues PostureValues) {
    this.PostureValues = PostureValues;
    return this;
  }

  public void unsetPostureValues() {
    this.PostureValues = null;
  }

  /** Returns true if field PostureValues is set (has been assigned a value) and false otherwise */
  public boolean isSetPostureValues() {
    return this.PostureValues != null;
  }

  public void setPostureValuesIsSet(boolean value) {
    if (!value) {
      this.PostureValues = null;
    }
  }

  public int getSceneObjectsSize() {
    return (this.SceneObjects == null) ? 0 : this.SceneObjects.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<java.lang.String> getSceneObjectsIterator() {
    return (this.SceneObjects == null) ? null : this.SceneObjects.iterator();
  }

  public void addToSceneObjects(java.lang.String elem) {
    if (this.SceneObjects == null) {
      this.SceneObjects = new java.util.ArrayList<java.lang.String>();
    }
    this.SceneObjects.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<java.lang.String> getSceneObjects() {
    return this.SceneObjects;
  }

  public MAvatarUpdate setSceneObjects(@org.apache.thrift.annotation.Nullable java.util.List<java.lang.String> SceneObjects) {
    this.SceneObjects = SceneObjects;
    return this;
  }

  public void unsetSceneObjects() {
    this.SceneObjects = null;
  }

  /** Returns true if field SceneObjects is set (has been assigned a value) and false otherwise */
  public boolean isSetSceneObjects() {
    return this.SceneObjects != null;
  }

  public void setSceneObjectsIsSet(boolean value) {
    if (!value) {
      this.SceneObjects = null;
    }
  }

  @org.apache.thrift.annotation.Nullable
  public de.mosim.mmi.avatar.MAvatarDescription getDescription() {
    return this.Description;
  }

  public MAvatarUpdate setDescription(@org.apache.thrift.annotation.Nullable de.mosim.mmi.avatar.MAvatarDescription Description) {
    this.Description = Description;
    return this;
  }

  public void unsetDescription() {
    this.Description = null;
  }

  /** Returns true if field Description is set (has been assigned a value) and false otherwise */
  public boolean isSetDescription() {
    return this.Description != null;
  }

  public void setDescriptionIsSet(boolean value) {
    if (!value) {
      this.Description = null;
    }
  }

  public int getPropertiesSize() {
    return (this.Properties == null) ? 0 : this.Properties.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<MPropertyUpdate> getPropertiesIterator() {
    return (this.Properties == null) ? null : this.Properties.iterator();
  }

  public void addToProperties(MPropertyUpdate elem) {
    if (this.Properties == null) {
      this.Properties = new java.util.ArrayList<MPropertyUpdate>();
    }
    this.Properties.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<MPropertyUpdate> getProperties() {
    return this.Properties;
  }

  public MAvatarUpdate setProperties(@org.apache.thrift.annotation.Nullable java.util.List<MPropertyUpdate> Properties) {
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
    case ID:
      if (value == null) {
        unsetID();
      } else {
        setID((java.lang.String)value);
      }
      break;

    case POSTURE_VALUES:
      if (value == null) {
        unsetPostureValues();
      } else {
        setPostureValues((de.mosim.mmi.avatar.MAvatarPostureValues)value);
      }
      break;

    case SCENE_OBJECTS:
      if (value == null) {
        unsetSceneObjects();
      } else {
        setSceneObjects((java.util.List<java.lang.String>)value);
      }
      break;

    case DESCRIPTION:
      if (value == null) {
        unsetDescription();
      } else {
        setDescription((de.mosim.mmi.avatar.MAvatarDescription)value);
      }
      break;

    case PROPERTIES:
      if (value == null) {
        unsetProperties();
      } else {
        setProperties((java.util.List<MPropertyUpdate>)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case ID:
      return getID();

    case POSTURE_VALUES:
      return getPostureValues();

    case SCENE_OBJECTS:
      return getSceneObjects();

    case DESCRIPTION:
      return getDescription();

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
    case ID:
      return isSetID();
    case POSTURE_VALUES:
      return isSetPostureValues();
    case SCENE_OBJECTS:
      return isSetSceneObjects();
    case DESCRIPTION:
      return isSetDescription();
    case PROPERTIES:
      return isSetProperties();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MAvatarUpdate)
      return this.equals((MAvatarUpdate)that);
    return false;
  }

  public boolean equals(MAvatarUpdate that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_ID = true && this.isSetID();
    boolean that_present_ID = true && that.isSetID();
    if (this_present_ID || that_present_ID) {
      if (!(this_present_ID && that_present_ID))
        return false;
      if (!this.ID.equals(that.ID))
        return false;
    }

    boolean this_present_PostureValues = true && this.isSetPostureValues();
    boolean that_present_PostureValues = true && that.isSetPostureValues();
    if (this_present_PostureValues || that_present_PostureValues) {
      if (!(this_present_PostureValues && that_present_PostureValues))
        return false;
      if (!this.PostureValues.equals(that.PostureValues))
        return false;
    }

    boolean this_present_SceneObjects = true && this.isSetSceneObjects();
    boolean that_present_SceneObjects = true && that.isSetSceneObjects();
    if (this_present_SceneObjects || that_present_SceneObjects) {
      if (!(this_present_SceneObjects && that_present_SceneObjects))
        return false;
      if (!this.SceneObjects.equals(that.SceneObjects))
        return false;
    }

    boolean this_present_Description = true && this.isSetDescription();
    boolean that_present_Description = true && that.isSetDescription();
    if (this_present_Description || that_present_Description) {
      if (!(this_present_Description && that_present_Description))
        return false;
      if (!this.Description.equals(that.Description))
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

    hashCode = hashCode * 8191 + ((isSetID()) ? 131071 : 524287);
    if (isSetID())
      hashCode = hashCode * 8191 + ID.hashCode();

    hashCode = hashCode * 8191 + ((isSetPostureValues()) ? 131071 : 524287);
    if (isSetPostureValues())
      hashCode = hashCode * 8191 + PostureValues.hashCode();

    hashCode = hashCode * 8191 + ((isSetSceneObjects()) ? 131071 : 524287);
    if (isSetSceneObjects())
      hashCode = hashCode * 8191 + SceneObjects.hashCode();

    hashCode = hashCode * 8191 + ((isSetDescription()) ? 131071 : 524287);
    if (isSetDescription())
      hashCode = hashCode * 8191 + Description.hashCode();

    hashCode = hashCode * 8191 + ((isSetProperties()) ? 131071 : 524287);
    if (isSetProperties())
      hashCode = hashCode * 8191 + Properties.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(MAvatarUpdate other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

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
    lastComparison = java.lang.Boolean.valueOf(isSetPostureValues()).compareTo(other.isSetPostureValues());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPostureValues()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.PostureValues, other.PostureValues);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetSceneObjects()).compareTo(other.isSetSceneObjects());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSceneObjects()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.SceneObjects, other.SceneObjects);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetDescription()).compareTo(other.isSetDescription());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDescription()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Description, other.Description);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MAvatarUpdate(");
    boolean first = true;

    sb.append("ID:");
    if (this.ID == null) {
      sb.append("null");
    } else {
      sb.append(this.ID);
    }
    first = false;
    if (isSetPostureValues()) {
      if (!first) sb.append(", ");
      sb.append("PostureValues:");
      if (this.PostureValues == null) {
        sb.append("null");
      } else {
        sb.append(this.PostureValues);
      }
      first = false;
    }
    if (isSetSceneObjects()) {
      if (!first) sb.append(", ");
      sb.append("SceneObjects:");
      if (this.SceneObjects == null) {
        sb.append("null");
      } else {
        sb.append(this.SceneObjects);
      }
      first = false;
    }
    if (isSetDescription()) {
      if (!first) sb.append(", ");
      sb.append("Description:");
      if (this.Description == null) {
        sb.append("null");
      } else {
        sb.append(this.Description);
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
    if (ID == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'ID' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
    if (PostureValues != null) {
      PostureValues.validate();
    }
    if (Description != null) {
      Description.validate();
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

  private static class MAvatarUpdateStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MAvatarUpdateStandardScheme getScheme() {
      return new MAvatarUpdateStandardScheme();
    }
  }

  private static class MAvatarUpdateStandardScheme extends org.apache.thrift.scheme.StandardScheme<MAvatarUpdate> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MAvatarUpdate struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.ID = iprot.readString();
              struct.setIDIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // POSTURE_VALUES
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.PostureValues = new de.mosim.mmi.avatar.MAvatarPostureValues();
              struct.PostureValues.read(iprot);
              struct.setPostureValuesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // SCENE_OBJECTS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list340 = iprot.readListBegin();
                struct.SceneObjects = new java.util.ArrayList<java.lang.String>(_list340.size);
                @org.apache.thrift.annotation.Nullable java.lang.String _elem341;
                for (int _i342 = 0; _i342 < _list340.size; ++_i342)
                {
                  _elem341 = iprot.readString();
                  struct.SceneObjects.add(_elem341);
                }
                iprot.readListEnd();
              }
              struct.setSceneObjectsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // DESCRIPTION
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.Description = new de.mosim.mmi.avatar.MAvatarDescription();
              struct.Description.read(iprot);
              struct.setDescriptionIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // PROPERTIES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list343 = iprot.readListBegin();
                struct.Properties = new java.util.ArrayList<MPropertyUpdate>(_list343.size);
                @org.apache.thrift.annotation.Nullable MPropertyUpdate _elem344;
                for (int _i345 = 0; _i345 < _list343.size; ++_i345)
                {
                  _elem344 = new MPropertyUpdate();
                  _elem344.read(iprot);
                  struct.Properties.add(_elem344);
                }
                iprot.readListEnd();
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, MAvatarUpdate struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.ID != null) {
        oprot.writeFieldBegin(ID_FIELD_DESC);
        oprot.writeString(struct.ID);
        oprot.writeFieldEnd();
      }
      if (struct.PostureValues != null) {
        if (struct.isSetPostureValues()) {
          oprot.writeFieldBegin(POSTURE_VALUES_FIELD_DESC);
          struct.PostureValues.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      if (struct.SceneObjects != null) {
        if (struct.isSetSceneObjects()) {
          oprot.writeFieldBegin(SCENE_OBJECTS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.SceneObjects.size()));
            for (java.lang.String _iter346 : struct.SceneObjects)
            {
              oprot.writeString(_iter346);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.Description != null) {
        if (struct.isSetDescription()) {
          oprot.writeFieldBegin(DESCRIPTION_FIELD_DESC);
          struct.Description.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      if (struct.Properties != null) {
        if (struct.isSetProperties()) {
          oprot.writeFieldBegin(PROPERTIES_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.Properties.size()));
            for (MPropertyUpdate _iter347 : struct.Properties)
            {
              _iter347.write(oprot);
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

  private static class MAvatarUpdateTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MAvatarUpdateTupleScheme getScheme() {
      return new MAvatarUpdateTupleScheme();
    }
  }

  private static class MAvatarUpdateTupleScheme extends org.apache.thrift.scheme.TupleScheme<MAvatarUpdate> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MAvatarUpdate struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeString(struct.ID);
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetPostureValues()) {
        optionals.set(0);
      }
      if (struct.isSetSceneObjects()) {
        optionals.set(1);
      }
      if (struct.isSetDescription()) {
        optionals.set(2);
      }
      if (struct.isSetProperties()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetPostureValues()) {
        struct.PostureValues.write(oprot);
      }
      if (struct.isSetSceneObjects()) {
        {
          oprot.writeI32(struct.SceneObjects.size());
          for (java.lang.String _iter348 : struct.SceneObjects)
          {
            oprot.writeString(_iter348);
          }
        }
      }
      if (struct.isSetDescription()) {
        struct.Description.write(oprot);
      }
      if (struct.isSetProperties()) {
        {
          oprot.writeI32(struct.Properties.size());
          for (MPropertyUpdate _iter349 : struct.Properties)
          {
            _iter349.write(oprot);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MAvatarUpdate struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.ID = iprot.readString();
      struct.setIDIsSet(true);
      java.util.BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.PostureValues = new de.mosim.mmi.avatar.MAvatarPostureValues();
        struct.PostureValues.read(iprot);
        struct.setPostureValuesIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list350 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.SceneObjects = new java.util.ArrayList<java.lang.String>(_list350.size);
          @org.apache.thrift.annotation.Nullable java.lang.String _elem351;
          for (int _i352 = 0; _i352 < _list350.size; ++_i352)
          {
            _elem351 = iprot.readString();
            struct.SceneObjects.add(_elem351);
          }
        }
        struct.setSceneObjectsIsSet(true);
      }
      if (incoming.get(2)) {
        struct.Description = new de.mosim.mmi.avatar.MAvatarDescription();
        struct.Description.read(iprot);
        struct.setDescriptionIsSet(true);
      }
      if (incoming.get(3)) {
        {
          org.apache.thrift.protocol.TList _list353 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.Properties = new java.util.ArrayList<MPropertyUpdate>(_list353.size);
          @org.apache.thrift.annotation.Nullable MPropertyUpdate _elem354;
          for (int _i355 = 0; _i355 < _list353.size; ++_i355)
          {
            _elem354 = new MPropertyUpdate();
            _elem354.read(iprot);
            struct.Properties.add(_elem354);
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

