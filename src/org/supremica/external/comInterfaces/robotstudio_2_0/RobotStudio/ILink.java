package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface ILink Declaration
public interface ILink extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x4D33EBDB,(short)0xC1BA,(short)0x11D3,new char[]{0x80,0xD0,0x00,0xC0,0x4F,0x68,0x8A,0x8C});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes getAttributes() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IJoint2 getParentJoint() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IJoints getChildJoints() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IParts getParts() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 getParent() throws com.inzoom.comjni.ComJniException;
}
