package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface ITargetRef Implementation
public class ITargetRefJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef getITargetRefFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ITargetRefJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef getITargetRefFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ITargetRefJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef getITargetRefFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ITargetRefJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef convertComPtrToITargetRef(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ITargetRefJCW(comPtr,true,releaseComPtr); }
  protected ITargetRefJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ITargetRefJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID); }
  protected ITargetRefJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ITargetRefJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID); }
  protected ITargetRefJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ITargetRefJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID,releaseComPtr);}
  protected ITargetRefJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getUniqueName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 getTarget() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITarget2JCW.getITarget2FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void delete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes getAttributes() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IAttributesJCW.getIAttributesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getMotionType() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setMotionType(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IToolFrame2 getToolFrame() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IToolFrame2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IToolFrame2JCW.getIToolFrame2FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setToolFrame(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IToolFrame pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pVal,false)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 getPath() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPath2JCW.getIPath2FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IApplication3JCW.getIApplication3FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPath2JCW.getIPath2FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 getViaTarget() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITarget2JCW.getITarget2FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void convertToCircular(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef ViaTargetRef) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)ViaTargetRef,false)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IProcessType getProcessType() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IProcessType rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IProcessTypeJCW.getIProcessTypeFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setProcessType(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IProcessType ppVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)ppVal,false)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.Variant getColor() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    com.inzoom.comjni.Variant rv = _v[0].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setColor(com.inzoom.comjni.Variant RGBA) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(RGBA)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
