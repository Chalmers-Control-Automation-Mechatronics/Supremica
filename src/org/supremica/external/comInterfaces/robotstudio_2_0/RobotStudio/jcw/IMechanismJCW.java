package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IMechanism Implementation
public class IMechanismJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism getIMechanismFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IMechanismJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism getIMechanismFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IMechanismJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism getIMechanismFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IMechanismJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism convertComPtrToIMechanism(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IMechanismJCW(comPtr,true,releaseComPtr); }
  protected IMechanismJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IMechanismJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID); }
  protected IMechanismJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IMechanismJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID); }
  protected IMechanismJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IMechanismJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID,releaseComPtr);}
  protected IMechanismJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IApplication3JCW.getIApplication3FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IStation2JCW.getIStation2FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform getTransform() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITransformJCW.getITransformFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTransform(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pVal,false)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getUniqueName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getVisible() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVisible(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IJoints getJoints() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IJoints rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IJointsJCW.getIJointsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void delete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes getAttributes() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IAttributesJCW.getIAttributesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant getInverseKinematic(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Transform,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRobotConfiguration Configuration) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Transform,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Configuration,false),
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    com.inzoom.comjni.Variant rv = _v[2].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant getInverseKinematic(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Transform) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Transform,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,false),
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    com.inzoom.comjni.Variant rv = _v[2].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void moveAlongPath(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath Path) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Path,false)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void moveToTargetRef(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef TargetRef) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)TargetRef,false)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.Variant getJointValues() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    com.inzoom.comjni.Variant rv = _v[0].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setJointValues(com.inzoom.comjni.Variant pJointValues) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(pJointValues)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFrame getWorld() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFrame rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IFrameJCW.getIFrameFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ILinks getLinks() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ILinks rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ILinksJCW.getILinksFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObjects getWorkObjects() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObjects rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IWorkObjectsJCW.getIWorkObjectsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFrame getWristFrame() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFrame rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IFrameJCW.getIFrameFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IJoints getExternalAxes() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IJoints rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IJointsJCW.getIJointsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection getAllSolutions(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget Target) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Target,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRsCollectionJCW.getIRsCollectionFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void jumpToTarget(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget Target,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRobotConfiguration Configuration) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Target,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Configuration,false)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void jumpToTarget(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget Target) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Target,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,false)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void examine() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void unexamine() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(128,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void saveToLibrary(String FileName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false)
    };
    vtblCall(132,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void saveToLibrary() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,false)
    };
    vtblCall(132,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller2 getController() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(136,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IABBS4Controller2JCW.getIABBS4Controller2FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttachments getAttachments() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(140,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttachments rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IAttachmentsJCW.getIAttachmentsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRobotConfiguration getRobotConfiguration() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(144,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRobotConfiguration rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRobotConfigurationJCW.getIRobotConfigurationFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getInLimits(com.inzoom.comjni.Variant JointValues) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(JointValues),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    boolean rv = _v[1].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getInLimits() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    boolean rv = _v[1].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant getHomeJointValues() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(152,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    com.inzoom.comjni.Variant rv = _v[0].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IExternalAxesValues getExternalAxesValues() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(156,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IExternalAxesValues rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IExternalAxesValuesJCW.getIExternalAxesValuesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setExternalAxesValues(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IExternalAxesValues pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pVal,false)
    };
    vtblCall(160,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getKinematicRole() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(164,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setKinematicRole(int pRole) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pRole,false)
    };
    vtblCall(168,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IToolFrames getToolFrames() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(172,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IToolFrames rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IToolFramesJCW.getIToolFramesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getUseLimits() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(176,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setUseLimits(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(180,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void disconnectFromLibrary() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(184,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IToolFrame2 getActiveToolFrame() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(188,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IToolFrame2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IToolFrame2JCW.getIToolFrame2FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setActiveToolFrame(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IToolFrame pToolFrame) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pToolFrame,false)
    };
    vtblCall(192,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void fireOpen() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(196,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller2 startABBS4Controller(boolean ForceRestart) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ForceRestart,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(200,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IABBS4Controller2JCW.getIABBS4Controller2FromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller2 startABBS4Controller() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(200,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IABBS4Controller2JCW.getIABBS4Controller2FromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMarkUps getMarkUps() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(204,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMarkUps rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IMarkUpsJCW.getIMarkUpsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getCurrentControllerName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(208,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setCurrentControllerName(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(212,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getCurrentControllerVersion() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(216,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setCurrentControllerVersion(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(220,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPaths getPaths() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(224,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPaths rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPathsJCW.getIPathsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
