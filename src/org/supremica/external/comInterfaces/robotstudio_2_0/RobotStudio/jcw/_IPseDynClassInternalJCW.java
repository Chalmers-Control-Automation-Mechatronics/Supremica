package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface _IPseDynClassInternal Implementation
public class _IPseDynClassInternalJCW extends com.inzoom.comjni.jcw.IUnknownJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal, com.inzoom.comjni.IUnknown {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal get_IPseDynClassInternalFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _IPseDynClassInternalJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal get_IPseDynClassInternalFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _IPseDynClassInternalJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal get_IPseDynClassInternalFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _IPseDynClassInternalJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal convertComPtrTo_IPseDynClassInternal(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _IPseDynClassInternalJCW(comPtr,true,releaseComPtr); }
  protected _IPseDynClassInternalJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _IPseDynClassInternalJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal.IID); }
  protected _IPseDynClassInternalJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _IPseDynClassInternalJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal.IID); }
  protected _IPseDynClassInternalJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _IPseDynClassInternalJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal.IID,releaseComPtr);}
  protected _IPseDynClassInternalJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void deleteInstance(com.inzoom.comjni.IDispatch pDispatch) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pDispatch,false)
    };
    vtblCall(12,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void addEvent(String bstrName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(bstrName,false)
    };
    vtblCall(16,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void removeEvent(String bstrName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(bstrName,false)
    };
    vtblCall(20,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void fireEvent(String bstrName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(bstrName,false)
    };
    vtblCall(24,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.IDispatch createInstance() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getTypeID() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.UI4,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void initNew(int pProj,String sClassName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pProj,false),
      new com.inzoom.comjni.Variant(sClassName,false)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void register(int pProj,com.inzoom.comjni.IDispatch pHostObject,String sClassName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pProj,false),
      new com.inzoom.comjni.Variant(pHostObject,false),
      new com.inzoom.comjni.Variant(sClassName,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void deleteClassObject() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IPseDynClassInternal.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
