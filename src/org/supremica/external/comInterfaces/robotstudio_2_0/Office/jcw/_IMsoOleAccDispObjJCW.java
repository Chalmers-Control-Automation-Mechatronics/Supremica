package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface _IMsoOleAccDispObj Implementation
public class _IMsoOleAccDispObjJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.IAccessibleJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoOleAccDispObj {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoOleAccDispObj get_IMsoOleAccDispObjFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _IMsoOleAccDispObjJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoOleAccDispObj get_IMsoOleAccDispObjFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _IMsoOleAccDispObjJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoOleAccDispObj get_IMsoOleAccDispObjFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _IMsoOleAccDispObjJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoOleAccDispObj convertComPtrTo_IMsoOleAccDispObj(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _IMsoOleAccDispObjJCW(comPtr,true,releaseComPtr); }
  protected _IMsoOleAccDispObjJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _IMsoOleAccDispObjJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoOleAccDispObj.IID); }
  protected _IMsoOleAccDispObjJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _IMsoOleAccDispObjJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoOleAccDispObj.IID); }
  protected _IMsoOleAccDispObjJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _IMsoOleAccDispObjJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoOleAccDispObj.IID,releaseComPtr);}
  protected _IMsoOleAccDispObjJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoOleAccDispObj.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getCreator() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoOleAccDispObj.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
