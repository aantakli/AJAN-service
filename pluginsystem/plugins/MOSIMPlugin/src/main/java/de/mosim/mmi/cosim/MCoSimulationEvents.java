/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package de.mosim.mmi.cosim;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2023-06-29")
public class MCoSimulationEvents implements org.apache.thrift.TBase<MCoSimulationEvents, MCoSimulationEvents._Fields>, java.io.Serializable, Cloneable, Comparable<MCoSimulationEvents> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MCoSimulationEvents");

  private static final org.apache.thrift.protocol.TField EVENTS_FIELD_DESC = new org.apache.thrift.protocol.TField("Events", org.apache.thrift.protocol.TType.LIST, (short)1);
  private static final org.apache.thrift.protocol.TField SIMULATION_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("SimulationTime", org.apache.thrift.protocol.TType.DOUBLE, (short)2);
  private static final org.apache.thrift.protocol.TField FRAME_NUMBER_FIELD_DESC = new org.apache.thrift.protocol.TField("FrameNumber", org.apache.thrift.protocol.TType.I32, (short)3);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MCoSimulationEventsStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MCoSimulationEventsTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable java.util.List<de.mosim.mmi.mmu.MSimulationEvent> Events; // required
  public double SimulationTime; // required
  public int FrameNumber; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    EVENTS((short)1, "Events"),
    SIMULATION_TIME((short)2, "SimulationTime"),
    FRAME_NUMBER((short)3, "FrameNumber");

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
        case 1: // EVENTS
          return EVENTS;
        case 2: // SIMULATION_TIME
          return SIMULATION_TIME;
        case 3: // FRAME_NUMBER
          return FRAME_NUMBER;
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
  private static final int __SIMULATIONTIME_ISSET_ID = 0;
  private static final int __FRAMENUMBER_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.EVENTS, new org.apache.thrift.meta_data.FieldMetaData("Events", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.mmu.MSimulationEvent.class))));
    tmpMap.put(_Fields.SIMULATION_TIME, new org.apache.thrift.meta_data.FieldMetaData("SimulationTime", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    tmpMap.put(_Fields.FRAME_NUMBER, new org.apache.thrift.meta_data.FieldMetaData("FrameNumber", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MCoSimulationEvents.class, metaDataMap);
  }

  public MCoSimulationEvents() {
  }

  public MCoSimulationEvents(
    java.util.List<de.mosim.mmi.mmu.MSimulationEvent> Events,
    double SimulationTime,
    int FrameNumber)
  {
    this();
    this.Events = Events;
    this.SimulationTime = SimulationTime;
    setSimulationTimeIsSet(true);
    this.FrameNumber = FrameNumber;
    setFrameNumberIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MCoSimulationEvents(MCoSimulationEvents other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetEvents()) {
      java.util.List<de.mosim.mmi.mmu.MSimulationEvent> __this__Events = new java.util.ArrayList<de.mosim.mmi.mmu.MSimulationEvent>(other.Events.size());
      for (de.mosim.mmi.mmu.MSimulationEvent other_element : other.Events) {
        __this__Events.add(new de.mosim.mmi.mmu.MSimulationEvent(other_element));
      }
      this.Events = __this__Events;
    }
    this.SimulationTime = other.SimulationTime;
    this.FrameNumber = other.FrameNumber;
  }

  public MCoSimulationEvents deepCopy() {
    return new MCoSimulationEvents(this);
  }

  @Override
  public void clear() {
    this.Events = null;
    setSimulationTimeIsSet(false);
    this.SimulationTime = 0.0;
    setFrameNumberIsSet(false);
    this.FrameNumber = 0;
  }

  public int getEventsSize() {
    return (this.Events == null) ? 0 : this.Events.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<de.mosim.mmi.mmu.MSimulationEvent> getEventsIterator() {
    return (this.Events == null) ? null : this.Events.iterator();
  }

  public void addToEvents(de.mosim.mmi.mmu.MSimulationEvent elem) {
    if (this.Events == null) {
      this.Events = new java.util.ArrayList<de.mosim.mmi.mmu.MSimulationEvent>();
    }
    this.Events.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<de.mosim.mmi.mmu.MSimulationEvent> getEvents() {
    return this.Events;
  }

  public MCoSimulationEvents setEvents(@org.apache.thrift.annotation.Nullable java.util.List<de.mosim.mmi.mmu.MSimulationEvent> Events) {
    this.Events = Events;
    return this;
  }

  public void unsetEvents() {
    this.Events = null;
  }

  /** Returns true if field Events is set (has been assigned a value) and false otherwise */
  public boolean isSetEvents() {
    return this.Events != null;
  }

  public void setEventsIsSet(boolean value) {
    if (!value) {
      this.Events = null;
    }
  }

  public double getSimulationTime() {
    return this.SimulationTime;
  }

  public MCoSimulationEvents setSimulationTime(double SimulationTime) {
    this.SimulationTime = SimulationTime;
    setSimulationTimeIsSet(true);
    return this;
  }

  public void unsetSimulationTime() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __SIMULATIONTIME_ISSET_ID);
  }

  /** Returns true if field SimulationTime is set (has been assigned a value) and false otherwise */
  public boolean isSetSimulationTime() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __SIMULATIONTIME_ISSET_ID);
  }

  public void setSimulationTimeIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __SIMULATIONTIME_ISSET_ID, value);
  }

  public int getFrameNumber() {
    return this.FrameNumber;
  }

  public MCoSimulationEvents setFrameNumber(int FrameNumber) {
    this.FrameNumber = FrameNumber;
    setFrameNumberIsSet(true);
    return this;
  }

  public void unsetFrameNumber() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __FRAMENUMBER_ISSET_ID);
  }

  /** Returns true if field FrameNumber is set (has been assigned a value) and false otherwise */
  public boolean isSetFrameNumber() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __FRAMENUMBER_ISSET_ID);
  }

  public void setFrameNumberIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __FRAMENUMBER_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case EVENTS:
      if (value == null) {
        unsetEvents();
      } else {
        setEvents((java.util.List<de.mosim.mmi.mmu.MSimulationEvent>)value);
      }
      break;

    case SIMULATION_TIME:
      if (value == null) {
        unsetSimulationTime();
      } else {
        setSimulationTime((java.lang.Double)value);
      }
      break;

    case FRAME_NUMBER:
      if (value == null) {
        unsetFrameNumber();
      } else {
        setFrameNumber((java.lang.Integer)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case EVENTS:
      return getEvents();

    case SIMULATION_TIME:
      return getSimulationTime();

    case FRAME_NUMBER:
      return getFrameNumber();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case EVENTS:
      return isSetEvents();
    case SIMULATION_TIME:
      return isSetSimulationTime();
    case FRAME_NUMBER:
      return isSetFrameNumber();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MCoSimulationEvents)
      return this.equals((MCoSimulationEvents)that);
    return false;
  }

  public boolean equals(MCoSimulationEvents that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_Events = true && this.isSetEvents();
    boolean that_present_Events = true && that.isSetEvents();
    if (this_present_Events || that_present_Events) {
      if (!(this_present_Events && that_present_Events))
        return false;
      if (!this.Events.equals(that.Events))
        return false;
    }

    boolean this_present_SimulationTime = true;
    boolean that_present_SimulationTime = true;
    if (this_present_SimulationTime || that_present_SimulationTime) {
      if (!(this_present_SimulationTime && that_present_SimulationTime))
        return false;
      if (this.SimulationTime != that.SimulationTime)
        return false;
    }

    boolean this_present_FrameNumber = true;
    boolean that_present_FrameNumber = true;
    if (this_present_FrameNumber || that_present_FrameNumber) {
      if (!(this_present_FrameNumber && that_present_FrameNumber))
        return false;
      if (this.FrameNumber != that.FrameNumber)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetEvents()) ? 131071 : 524287);
    if (isSetEvents())
      hashCode = hashCode * 8191 + Events.hashCode();

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(SimulationTime);

    hashCode = hashCode * 8191 + FrameNumber;

    return hashCode;
  }

  @Override
  public int compareTo(MCoSimulationEvents other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetEvents()).compareTo(other.isSetEvents());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetEvents()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Events, other.Events);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetSimulationTime()).compareTo(other.isSetSimulationTime());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSimulationTime()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.SimulationTime, other.SimulationTime);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetFrameNumber()).compareTo(other.isSetFrameNumber());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFrameNumber()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.FrameNumber, other.FrameNumber);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MCoSimulationEvents(");
    boolean first = true;

    sb.append("Events:");
    if (this.Events == null) {
      sb.append("null");
    } else {
      sb.append(this.Events);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("SimulationTime:");
    sb.append(this.SimulationTime);
    first = false;
    if (!first) sb.append(", ");
    sb.append("FrameNumber:");
    sb.append(this.FrameNumber);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (Events == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'Events' was not present! Struct: " + toString());
    }
    // alas, we cannot check 'SimulationTime' because it's a primitive and you chose the non-beans generator.
    // alas, we cannot check 'FrameNumber' because it's a primitive and you chose the non-beans generator.
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

  private static class MCoSimulationEventsStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MCoSimulationEventsStandardScheme getScheme() {
      return new MCoSimulationEventsStandardScheme();
    }
  }

  private static class MCoSimulationEventsStandardScheme extends org.apache.thrift.scheme.StandardScheme<MCoSimulationEvents> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MCoSimulationEvents struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // EVENTS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                struct.Events = new java.util.ArrayList<de.mosim.mmi.mmu.MSimulationEvent>(_list0.size);
                @org.apache.thrift.annotation.Nullable de.mosim.mmi.mmu.MSimulationEvent _elem1;
                for (int _i2 = 0; _i2 < _list0.size; ++_i2)
                {
                  _elem1 = new de.mosim.mmi.mmu.MSimulationEvent();
                  _elem1.read(iprot);
                  struct.Events.add(_elem1);
                }
                iprot.readListEnd();
              }
              struct.setEventsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // SIMULATION_TIME
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.SimulationTime = iprot.readDouble();
              struct.setSimulationTimeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // FRAME_NUMBER
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.FrameNumber = iprot.readI32();
              struct.setFrameNumberIsSet(true);
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
      if (!struct.isSetSimulationTime()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'SimulationTime' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetFrameNumber()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'FrameNumber' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, MCoSimulationEvents struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.Events != null) {
        oprot.writeFieldBegin(EVENTS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.Events.size()));
          for (de.mosim.mmi.mmu.MSimulationEvent _iter3 : struct.Events)
          {
            _iter3.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(SIMULATION_TIME_FIELD_DESC);
      oprot.writeDouble(struct.SimulationTime);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(FRAME_NUMBER_FIELD_DESC);
      oprot.writeI32(struct.FrameNumber);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MCoSimulationEventsTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MCoSimulationEventsTupleScheme getScheme() {
      return new MCoSimulationEventsTupleScheme();
    }
  }

  private static class MCoSimulationEventsTupleScheme extends org.apache.thrift.scheme.TupleScheme<MCoSimulationEvents> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MCoSimulationEvents struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      {
        oprot.writeI32(struct.Events.size());
        for (de.mosim.mmi.mmu.MSimulationEvent _iter4 : struct.Events)
        {
          _iter4.write(oprot);
        }
      }
      oprot.writeDouble(struct.SimulationTime);
      oprot.writeI32(struct.FrameNumber);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MCoSimulationEvents struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      {
        org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
        struct.Events = new java.util.ArrayList<de.mosim.mmi.mmu.MSimulationEvent>(_list5.size);
        @org.apache.thrift.annotation.Nullable de.mosim.mmi.mmu.MSimulationEvent _elem6;
        for (int _i7 = 0; _i7 < _list5.size; ++_i7)
        {
          _elem6 = new de.mosim.mmi.mmu.MSimulationEvent();
          _elem6.read(iprot);
          struct.Events.add(_elem6);
        }
      }
      struct.setEventsIsSet(true);
      struct.SimulationTime = iprot.readDouble();
      struct.setSimulationTimeIsSet(true);
      struct.FrameNumber = iprot.readI32();
      struct.setFrameNumberIsSet(true);
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

