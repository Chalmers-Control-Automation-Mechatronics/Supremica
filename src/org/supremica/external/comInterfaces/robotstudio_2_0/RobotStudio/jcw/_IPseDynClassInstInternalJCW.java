package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface _IPseDynClassInstInternal Implementation
public class _IPseDynClassInstInternalJCW extends com.inzoom.comjni.jcw.IUnknownJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInstInternal, com.inzoom.comjni.IUnknown {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInstInternal get_IPseDynClassInstInternalFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _IPseDynClassInstInternalJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInstInternal get_IPseDynClassInstInternalFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _IPseDynClassInstInternalJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInstInternal get_IPseDynClassInstInternalFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _IPseDynClassInstInternalJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInstInternal convertComPtrTo_IPseDynClassInstInternal(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _IPseDynClassInstInternalJCW(comPtr,true,releaseComPtr); }
  protected _IPseDynClassInstInternalJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _IPseDynClassInstInternalJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInstInternal.IID); }
  protected _IPseDynClassInstInternalJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _IPseDynClassInstInternalJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInstInternal.IID); }
  protected _IPseDynClassInstInternalJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _IPseDynClassInstInternalJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInstInternal.IID,releaseComPtr);}
  protected _IPseDynClassInstInternalJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void releaseResources() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(12,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInstInternal.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void releaseProxyMappings() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(16,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInstInternal.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setCompositeObject(com.inzoom.comjni.IDispatch pDisp) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pDisp,false)
    };
    vtblCall(20,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInstInternal.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void getCompositeObject(com.inzoom.comjni.IDispatch[] ppDisp) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ppDisp[0],true)
    };
    vtblCall(24,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInstInternal.IID);
    ppDisp[0] = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
