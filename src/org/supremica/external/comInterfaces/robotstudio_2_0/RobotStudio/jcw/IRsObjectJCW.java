package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IRsObject Implementation
public class IRsObjectJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getIRsObjectFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IRsObjectJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getIRsObjectFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IRsObjectJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getIRsObjectFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IRsObjectJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject convertComPtrToIRsObject(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IRsObjectJCW(comPtr,true,releaseComPtr); }
  protected IRsObjectJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IRsObjectJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject.IID); }
  protected IRsObjectJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IRsObjectJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject.IID); }
  protected IRsObjectJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IRsObjectJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject.IID,releaseComPtr);}
  protected IRsObjectJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IApplication3JCW.getIApplication3FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes getAttributes() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IAttributesJCW.getIAttributesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObjects getChildren() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObjects rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRsObjectsJCW.getIRsObjectsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void delete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRsObjectJCW.getIRsObjectFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getObjectType() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
