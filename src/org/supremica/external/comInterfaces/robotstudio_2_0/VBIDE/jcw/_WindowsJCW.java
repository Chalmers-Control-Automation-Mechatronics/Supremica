package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw;

// interface _Windows Implementation
public class _WindowsJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._Windows_oldJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Windows {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Windows get_WindowsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _WindowsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Windows get_WindowsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _WindowsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Windows get_WindowsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _WindowsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Windows convertComPtrTo_Windows(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _WindowsJCW(comPtr,true,releaseComPtr); }
  protected _WindowsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _WindowsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Windows.IID); }
  protected _WindowsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _WindowsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Windows.IID); }
  protected _WindowsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _WindowsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Windows.IID,releaseComPtr);}
  protected _WindowsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window createToolWindow(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.AddIn AddInInst,String ProgId,String Caption,String GuidPosition,com.inzoom.comjni.IDispatch[] DocObj) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)AddInInst,false),
      new com.inzoom.comjni.Variant(ProgId,false),
      new com.inzoom.comjni.Variant(Caption,false),
      new com.inzoom.comjni.Variant(GuidPosition,false),
      new com.inzoom.comjni.Variant(DocObj[0],true),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Windows.IID);
    DocObj[0] = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[4].detachUnknownPtr(),false);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw.WindowJCW.getWindowFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
