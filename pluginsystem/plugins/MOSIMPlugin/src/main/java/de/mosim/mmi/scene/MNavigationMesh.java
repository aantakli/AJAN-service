/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.scene;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2023-06-29")
public class MNavigationMesh implements org.apache.thrift.TBase<MNavigationMesh, MNavigationMesh._Fields>, java.io.Serializable, Cloneable, Comparable<MNavigationMesh> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MNavigationMesh");

  private static final org.apache.thrift.protocol.TField VERTICES_FIELD_DESC = new org.apache.thrift.protocol.TField("Vertices", org.apache.thrift.protocol.TType.LIST, (short)1);
  private static final org.apache.thrift.protocol.TField TRIANGLES_FIELD_DESC = new org.apache.thrift.protocol.TField("Triangles", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField PROPERTIES_FIELD_DESC = new org.apache.thrift.protocol.TField("Properties", org.apache.thrift.protocol.TType.MAP, (short)3);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MNavigationMeshStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MNavigationMeshTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.util.List<de.mosim.mmi.math.MVector3> Vertices; // required
  public @org.apache.thrift.annotation.Nullable java.util.List<java.lang.Integer> Triangles; // required
  public @org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,java.lang.String> Properties; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    VERTICES((short)1, "Vertices"),
    TRIANGLES((short)2, "Triangles"),
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
        case 1: // VERTICES
          return VERTICES;
        case 2: // TRIANGLES
          return TRIANGLES;
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
    tmpMap.put(_Fields.VERTICES, new org.apache.thrift.meta_data.FieldMetaData("Vertices", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.math.MVector3.class))));
    tmpMap.put(_Fields.TRIANGLES, new org.apache.thrift.meta_data.FieldMetaData("Triangles", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32))));
    tmpMap.put(_Fields.PROPERTIES, new org.apache.thrift.meta_data.FieldMetaData("Properties", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MNavigationMesh.class, metaDataMap);
  }

  public MNavigationMesh() {
  }

  public MNavigationMesh(
    java.util.List<de.mosim.mmi.math.MVector3> Vertices,
    java.util.List<java.lang.Integer> Triangles)
  {
    this();
    this.Vertices = Vertices;
    this.Triangles = Triangles;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MNavigationMesh(MNavigationMesh other) {
    if (other.isSetVertices()) {
      java.util.List<de.mosim.mmi.math.MVector3> __this__Vertices = new java.util.ArrayList<de.mosim.mmi.math.MVector3>(other.Vertices.size());
      for (de.mosim.mmi.math.MVector3 other_element : other.Vertices) {
        __this__Vertices.add(new de.mosim.mmi.math.MVector3(other_element));
      }
      this.Vertices = __this__Vertices;
    }
    if (other.isSetTriangles()) {
      java.util.List<java.lang.Integer> __this__Triangles = new java.util.ArrayList<java.lang.Integer>(other.Triangles);
      this.Triangles = __this__Triangles;
    }
    if (other.isSetProperties()) {
      java.util.Map<java.lang.String,java.lang.String> __this__Properties = new java.util.HashMap<java.lang.String,java.lang.String>(other.Properties);
      this.Properties = __this__Properties;
    }
  }

  public MNavigationMesh deepCopy() {
    return new MNavigationMesh(this);
  }

  @Override
  public void clear() {
    this.Vertices = null;
    this.Triangles = null;
    this.Properties = null;
  }

  public int getVerticesSize() {
    return (this.Vertices == null) ? 0 : this.Vertices.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<de.mosim.mmi.math.MVector3> getVerticesIterator() {
    return (this.Vertices == null) ? null : this.Vertices.iterator();
  }

  public void addToVertices(de.mosim.mmi.math.MVector3 elem) {
    if (this.Vertices == null) {
      this.Vertices = new java.util.ArrayList<de.mosim.mmi.math.MVector3>();
    }
    this.Vertices.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<de.mosim.mmi.math.MVector3> getVertices() {
    return this.Vertices;
  }

  public MNavigationMesh setVertices(@org.apache.thrift.annotation.Nullable java.util.List<de.mosim.mmi.math.MVector3> Vertices) {
    this.Vertices = Vertices;
    return this;
  }

  public void unsetVertices() {
    this.Vertices = null;
  }

  /** Returns true if field Vertices is set (has been assigned a value) and false otherwise */
  public boolean isSetVertices() {
    return this.Vertices != null;
  }

  public void setVerticesIsSet(boolean value) {
    if (!value) {
      this.Vertices = null;
    }
  }

  public int getTrianglesSize() {
    return (this.Triangles == null) ? 0 : this.Triangles.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<java.lang.Integer> getTrianglesIterator() {
    return (this.Triangles == null) ? null : this.Triangles.iterator();
  }

  public void addToTriangles(int elem) {
    if (this.Triangles == null) {
      this.Triangles = new java.util.ArrayList<java.lang.Integer>();
    }
    this.Triangles.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<java.lang.Integer> getTriangles() {
    return this.Triangles;
  }

  public MNavigationMesh setTriangles(@org.apache.thrift.annotation.Nullable java.util.List<java.lang.Integer> Triangles) {
    this.Triangles = Triangles;
    return this;
  }

  public void unsetTriangles() {
    this.Triangles = null;
  }

  /** Returns true if field Triangles is set (has been assigned a value) and false otherwise */
  public boolean isSetTriangles() {
    return this.Triangles != null;
  }

  public void setTrianglesIsSet(boolean value) {
    if (!value) {
      this.Triangles = null;
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

  public MNavigationMesh setProperties(@org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,java.lang.String> Properties) {
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
    case VERTICES:
      if (value == null) {
        unsetVertices();
      } else {
        setVertices((java.util.List<de.mosim.mmi.math.MVector3>)value);
      }
      break;

    case TRIANGLES:
      if (value == null) {
        unsetTriangles();
      } else {
        setTriangles((java.util.List<java.lang.Integer>)value);
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
    case VERTICES:
      return getVertices();

    case TRIANGLES:
      return getTriangles();

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
    case VERTICES:
      return isSetVertices();
    case TRIANGLES:
      return isSetTriangles();
    case PROPERTIES:
      return isSetProperties();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MNavigationMesh)
      return this.equals((MNavigationMesh)that);
    return false;
  }

  public boolean equals(MNavigationMesh that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_Vertices = true && this.isSetVertices();
    boolean that_present_Vertices = true && that.isSetVertices();
    if (this_present_Vertices || that_present_Vertices) {
      if (!(this_present_Vertices && that_present_Vertices))
        return false;
      if (!this.Vertices.equals(that.Vertices))
        return false;
    }

    boolean this_present_Triangles = true && this.isSetTriangles();
    boolean that_present_Triangles = true && that.isSetTriangles();
    if (this_present_Triangles || that_present_Triangles) {
      if (!(this_present_Triangles && that_present_Triangles))
        return false;
      if (!this.Triangles.equals(that.Triangles))
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

    hashCode = hashCode * 8191 + ((isSetVertices()) ? 131071 : 524287);
    if (isSetVertices())
      hashCode = hashCode * 8191 + Vertices.hashCode();

    hashCode = hashCode * 8191 + ((isSetTriangles()) ? 131071 : 524287);
    if (isSetTriangles())
      hashCode = hashCode * 8191 + Triangles.hashCode();

    hashCode = hashCode * 8191 + ((isSetProperties()) ? 131071 : 524287);
    if (isSetProperties())
      hashCode = hashCode * 8191 + Properties.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(MNavigationMesh other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetVertices()).compareTo(other.isSetVertices());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetVertices()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Vertices, other.Vertices);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetTriangles()).compareTo(other.isSetTriangles());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTriangles()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Triangles, other.Triangles);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MNavigationMesh(");
    boolean first = true;

    sb.append("Vertices:");
    if (this.Vertices == null) {
      sb.append("null");
    } else {
      sb.append(this.Vertices);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("Triangles:");
    if (this.Triangles == null) {
      sb.append("null");
    } else {
      sb.append(this.Triangles);
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
    if (Vertices == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'Vertices' was not present! Struct: " + toString());
    }
    if (Triangles == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'Triangles' was not present! Struct: " + toString());
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

  private static class MNavigationMeshStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MNavigationMeshStandardScheme getScheme() {
      return new MNavigationMeshStandardScheme();
    }
  }

  private static class MNavigationMeshStandardScheme extends org.apache.thrift.scheme.StandardScheme<MNavigationMesh> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MNavigationMesh struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // VERTICES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list122 = iprot.readListBegin();
                struct.Vertices = new java.util.ArrayList<de.mosim.mmi.math.MVector3>(_list122.size);
                @org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MVector3 _elem123;
                for (int _i124 = 0; _i124 < _list122.size; ++_i124)
                {
                  _elem123 = new de.mosim.mmi.math.MVector3();
                  _elem123.read(iprot);
                  struct.Vertices.add(_elem123);
                }
                iprot.readListEnd();
              }
              struct.setVerticesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // TRIANGLES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list125 = iprot.readListBegin();
                struct.Triangles = new java.util.ArrayList<java.lang.Integer>(_list125.size);
                int _elem126;
                for (int _i127 = 0; _i127 < _list125.size; ++_i127)
                {
                  _elem126 = iprot.readI32();
                  struct.Triangles.add(_elem126);
                }
                iprot.readListEnd();
              }
              struct.setTrianglesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // PROPERTIES
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map128 = iprot.readMapBegin();
                struct.Properties = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map128.size);
                @org.apache.thrift.annotation.Nullable java.lang.String _key129;
                @org.apache.thrift.annotation.Nullable java.lang.String _val130;
                for (int _i131 = 0; _i131 < _map128.size; ++_i131)
                {
                  _key129 = iprot.readString();
                  _val130 = iprot.readString();
                  struct.Properties.put(_key129, _val130);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, MNavigationMesh struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.Vertices != null) {
        oprot.writeFieldBegin(VERTICES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.Vertices.size()));
          for (de.mosim.mmi.math.MVector3 _iter132 : struct.Vertices)
          {
            _iter132.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.Triangles != null) {
        oprot.writeFieldBegin(TRIANGLES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, struct.Triangles.size()));
          for (int _iter133 : struct.Triangles)
          {
            oprot.writeI32(_iter133);
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
            for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter134 : struct.Properties.entrySet())
            {
              oprot.writeString(_iter134.getKey());
              oprot.writeString(_iter134.getValue());
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

  private static class MNavigationMeshTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MNavigationMeshTupleScheme getScheme() {
      return new MNavigationMeshTupleScheme();
    }
  }

  private static class MNavigationMeshTupleScheme extends org.apache.thrift.scheme.TupleScheme<MNavigationMesh> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MNavigationMesh struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      {
        oprot.writeI32(struct.Vertices.size());
        for (de.mosim.mmi.math.MVector3 _iter135 : struct.Vertices)
        {
          _iter135.write(oprot);
        }
      }
      {
        oprot.writeI32(struct.Triangles.size());
        for (int _iter136 : struct.Triangles)
        {
          oprot.writeI32(_iter136);
        }
      }
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetProperties()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetProperties()) {
        {
          oprot.writeI32(struct.Properties.size());
          for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter137 : struct.Properties.entrySet())
          {
            oprot.writeString(_iter137.getKey());
            oprot.writeString(_iter137.getValue());
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MNavigationMesh struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      {
        org.apache.thrift.protocol.TList _list138 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
        struct.Vertices = new java.util.ArrayList<de.mosim.mmi.math.MVector3>(_list138.size);
        @org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MVector3 _elem139;
        for (int _i140 = 0; _i140 < _list138.size; ++_i140)
        {
          _elem139 = new de.mosim.mmi.math.MVector3();
          _elem139.read(iprot);
          struct.Vertices.add(_elem139);
        }
      }
      struct.setVerticesIsSet(true);
      {
        org.apache.thrift.protocol.TList _list141 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, iprot.readI32());
        struct.Triangles = new java.util.ArrayList<java.lang.Integer>(_list141.size);
        int _elem142;
        for (int _i143 = 0; _i143 < _list141.size; ++_i143)
        {
          _elem142 = iprot.readI32();
          struct.Triangles.add(_elem142);
        }
      }
      struct.setTrianglesIsSet(true);
      java.util.BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TMap _map144 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.Properties = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map144.size);
          @org.apache.thrift.annotation.Nullable java.lang.String _key145;
          @org.apache.thrift.annotation.Nullable java.lang.String _val146;
          for (int _i147 = 0; _i147 < _map144.size; ++_i147)
          {
            _key145 = iprot.readString();
            _val146 = iprot.readString();
            struct.Properties.put(_key145, _val146);
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

