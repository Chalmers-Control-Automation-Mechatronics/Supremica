package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IKinModeler Declaration
public interface IKinModeler extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xE0F5F2EA,(short)0xA72A,(short)0x11D3,new char[]{0x80,0xBA,0x00,0xC0,0x4F,0x68,0x8A,0x8C});
  public void createMechanism(String ShortMechanismName,String LongMechanismName,int pRole) throws com.inzoom.comjni.ComJniException;
  public void createMechanism(String ShortMechanismName,String LongMechanismName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 compileMechanism() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ILink addLink(String LinkName,com.inzoom.comjni.IDispatch PartsCollection) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IJoint2 addJoint(String JointName,String ParentLinkName,String ChildLinkName,com.inzoom.comjni.Variant AxisPoint1,com.inzoom.comjni.Variant AxisPoint2,int JointType,boolean IsJointActive) throws com.inzoom.comjni.ComJniException;
  public void setJointLimits(String JointName,int LimitType,com.inzoom.comjni.Variant LimitList) throws com.inzoom.comjni.ComJniException;
  public void setJointLimits(String JointName,int LimitType) throws com.inzoom.comjni.ComJniException;
  public void setJointDependency(String JointName,String JointFunction) throws com.inzoom.comjni.ComJniException;
  public void setBaseLink(String LinkName) throws com.inzoom.comjni.ComJniException;
  public void addWristFrame(String LinkName,String WristFrameName,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform WristFrameTransform) throws com.inzoom.comjni.ComJniException;
  public void setActiveTCP(String LinkName,String WristFrameName,String ActiveTCPName) throws com.inzoom.comjni.ComJniException;
  public void setActiveTCP(String LinkName,String WristFrameName) throws com.inzoom.comjni.ComJniException;
  public void setHomePosition(com.inzoom.comjni.Variant JointPositionList) throws com.inzoom.comjni.ComJniException;
  public void setWorkingRange(com.inzoom.comjni.Variant LowerBoundingBoxCorner,com.inzoom.comjni.Variant UpperBoundingBoxCorner) throws com.inzoom.comjni.ComJniException;
  public void setLeadingJoint(String JointName,String LeadingJointName,double DependencyFactor) throws com.inzoom.comjni.ComJniException;
  public void setDefaultController(String ControllerName,String ControllerVersion) throws com.inzoom.comjni.ComJniException;
  public void setJointMask(com.inzoom.comjni.Variant JointMask) throws com.inzoom.comjni.ComJniException;
}
