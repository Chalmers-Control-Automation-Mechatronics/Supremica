package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw;

// interface Property Implementation
public class PropertyJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property getPropertyFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PropertyJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property getPropertyFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PropertyJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property getPropertyFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new PropertyJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property convertComPtrToProperty(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PropertyJCW(comPtr,true,releaseComPtr); }
  protected PropertyJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected PropertyJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID); }
  protected PropertyJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected PropertyJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID); }
  protected PropertyJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected PropertyJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID,releaseComPtr);}
  protected PropertyJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.Variant getValue() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    com.inzoom.comjni.Variant rv = _v[0].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setValue(com.inzoom.comjni.Variant lppvReturn) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(lppvReturn)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.Variant getIndexedValue(com.inzoom.comjni.Variant Index1,com.inzoom.comjni.Variant Index2,com.inzoom.comjni.Variant Index3,com.inzoom.comjni.Variant Index4) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index1),
      createVTblVArg(Index2),
      createVTblVArg(Index3),
      createVTblVArg(Index4),
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    com.inzoom.comjni.Variant rv = _v[4].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant getIndexedValue(com.inzoom.comjni.Variant Index1,com.inzoom.comjni.Variant Index2,com.inzoom.comjni.Variant Index3) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index1),
      createVTblVArg(Index2),
      createVTblVArg(Index3),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    com.inzoom.comjni.Variant rv = _v[4].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant getIndexedValue(com.inzoom.comjni.Variant Index1,com.inzoom.comjni.Variant Index2) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index1),
      createVTblVArg(Index2),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    com.inzoom.comjni.Variant rv = _v[4].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant getIndexedValue(com.inzoom.comjni.Variant Index1) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index1),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    com.inzoom.comjni.Variant rv = _v[4].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setIndexedValue(com.inzoom.comjni.Variant Index1,com.inzoom.comjni.Variant Index2,com.inzoom.comjni.Variant Index3,com.inzoom.comjni.Variant Index4,com.inzoom.comjni.Variant lppvReturn) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index1),
      createVTblVArg(Index2),
      createVTblVArg(Index3),
      createVTblVArg(Index4),
      createVTblVArg(lppvReturn)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public short getNumIndices() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((short)0,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    short rv = _v[0].getShort();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Application getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Application rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw.ApplicationJCW.getApplicationFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Properties getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Properties rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Properties.getPropertiesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw.VBEJCW.getVBEFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Properties getCollection() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Properties rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Properties.getPropertiesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IUnknown getObject() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    com.inzoom.comjni.IUnknown rv = com.inzoom.comjni.jcw.IUnknownJCW.getIUnknownFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setObject(com.inzoom.comjni.IUnknown lppunk) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(lppunk,false)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Property.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
