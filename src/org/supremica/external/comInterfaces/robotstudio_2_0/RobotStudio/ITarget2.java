package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface ITarget2 Declaration
public interface ITarget2 extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xB02320E8,(short)0x486D,(short)0x11D5,new char[]{0xBC,0x91,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public String getModuleName() throws com.inzoom.comjni.ComJniException;
  public void setModuleName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform getOffsetTransform() throws com.inzoom.comjni.ComJniException;
  public void setOffsetTransform(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform pVal) throws com.inzoom.comjni.ComJniException;
}
