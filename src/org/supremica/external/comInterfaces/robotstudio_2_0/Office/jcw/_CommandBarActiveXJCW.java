package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface _CommandBarActiveX Implementation
public class _CommandBarActiveXJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX get_CommandBarActiveXFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CommandBarActiveXJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX get_CommandBarActiveXFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CommandBarActiveXJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX get_CommandBarActiveXFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _CommandBarActiveXJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX convertComPtrTo_CommandBarActiveX(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CommandBarActiveXJCW(comPtr,true,releaseComPtr); }
  protected _CommandBarActiveXJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _CommandBarActiveXJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX.IID); }
  protected _CommandBarActiveXJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _CommandBarActiveXJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX.IID); }
  protected _CommandBarActiveXJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _CommandBarActiveXJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX.IID,releaseComPtr);}
  protected _CommandBarActiveXJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public String getControlCLSID() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(332,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setControlCLSID(String pbstrClsid) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrClsid,false)
    };
    vtblCall(336,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.IUnknown getQueryControlInterface(String bstrIid) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(bstrIid,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(340,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX.IID);
    com.inzoom.comjni.IUnknown rv = com.inzoom.comjni.jcw.IUnknownJCW.getIUnknownFromPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setInnerObjectFactory(com.inzoom.comjni.IUnknown pUnk) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pUnk,false)
    };
    vtblCall(344,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void ensureControl() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(348,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setInitWith(com.inzoom.comjni.IUnknown rhs) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(rhs,false)
    };
    vtblCall(352,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarActiveX.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
