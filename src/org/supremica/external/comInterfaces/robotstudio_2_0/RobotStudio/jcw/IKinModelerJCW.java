package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IKinModeler Implementation
public class IKinModelerJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler getIKinModelerFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IKinModelerJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler getIKinModelerFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IKinModelerJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler getIKinModelerFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IKinModelerJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler convertComPtrToIKinModeler(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IKinModelerJCW(comPtr,true,releaseComPtr); }
  protected IKinModelerJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IKinModelerJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID); }
  protected IKinModelerJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IKinModelerJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID); }
  protected IKinModelerJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IKinModelerJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID,releaseComPtr);}
  protected IKinModelerJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void createMechanism(String ShortMechanismName,String LongMechanismName,int pRole) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ShortMechanismName,false),
      new com.inzoom.comjni.Variant(LongMechanismName,false),
      new com.inzoom.comjni.Variant(pRole,false)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void createMechanism(String ShortMechanismName,String LongMechanismName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ShortMechanismName,false),
      new com.inzoom.comjni.Variant(LongMechanismName,false),
      new com.inzoom.comjni.Variant((int)0,false)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 compileMechanism() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IMechanism2JCW.getIMechanism2FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ILink addLink(String LinkName,com.inzoom.comjni.IDispatch PartsCollection) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(LinkName,false),
      new com.inzoom.comjni.Variant(PartsCollection,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ILink rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ILinkJCW.getILinkFromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IJoint2 addJoint(String JointName,String ParentLinkName,String ChildLinkName,com.inzoom.comjni.Variant AxisPoint1,com.inzoom.comjni.Variant AxisPoint2,int JointType,boolean IsJointActive) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(JointName,false),
      new com.inzoom.comjni.Variant(ParentLinkName,false),
      new com.inzoom.comjni.Variant(ChildLinkName,false),
      createVTblVArg(AxisPoint1),
      createVTblVArg(AxisPoint2),
      new com.inzoom.comjni.Variant(JointType,false),
      new com.inzoom.comjni.Variant(IsJointActive,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IJoint2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IJoint2JCW.getIJoint2FromComPtr(_v[7].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setJointLimits(String JointName,int LimitType,com.inzoom.comjni.Variant LimitList) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(JointName,false),
      new com.inzoom.comjni.Variant(LimitType,false),
      createVTblVArg(LimitList)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setJointLimits(String JointName,int LimitType) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(JointName,false),
      new com.inzoom.comjni.Variant(LimitType,false),
      createVTblVArg(noParam)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setJointDependency(String JointName,String JointFunction) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(JointName,false),
      new com.inzoom.comjni.Variant(JointFunction,false)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setBaseLink(String LinkName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(LinkName,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void addWristFrame(String LinkName,String WristFrameName,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform WristFrameTransform) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(LinkName,false),
      new com.inzoom.comjni.Variant(WristFrameName,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)WristFrameTransform,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setActiveTCP(String LinkName,String WristFrameName,String ActiveTCPName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(LinkName,false),
      new com.inzoom.comjni.Variant(WristFrameName,false),
      new com.inzoom.comjni.Variant(ActiveTCPName,false)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setActiveTCP(String LinkName,String WristFrameName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(LinkName,false),
      new com.inzoom.comjni.Variant(WristFrameName,false),
      new com.inzoom.comjni.Variant((String)"Tool0",false)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setHomePosition(com.inzoom.comjni.Variant JointPositionList) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(JointPositionList)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setWorkingRange(com.inzoom.comjni.Variant LowerBoundingBoxCorner,com.inzoom.comjni.Variant UpperBoundingBoxCorner) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(LowerBoundingBoxCorner),
      createVTblVArg(UpperBoundingBoxCorner)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setLeadingJoint(String JointName,String LeadingJointName,double DependencyFactor) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(JointName,false),
      new com.inzoom.comjni.Variant(LeadingJointName,false),
      new com.inzoom.comjni.Variant(DependencyFactor,com.inzoom.comjni.enum.VarType.R8,false)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setDefaultController(String ControllerName,String ControllerVersion) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ControllerName,false),
      new com.inzoom.comjni.Variant(ControllerVersion,false)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setJointMask(com.inzoom.comjni.Variant JointMask) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(JointMask)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
